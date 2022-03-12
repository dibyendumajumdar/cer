/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.redukti.cer.runtime;

import org.redukti.cer.Scriptable;
import org.redukti.cer.exception.EvaluatorException;
import org.redukti.cer.exception.RhinoException;
import org.redukti.cer.exception.ScriptStackElement;

import java.io.Serializable;

/**
 * The class of error objects
 *
 * <p>ECMA 15.11
 */
final class NativeError extends IdScriptableObject {
    private static final long serialVersionUID = -5338413581437645187L;

    private static final Object ERROR_TAG = "Error";

    /** Default stack limit is set to "Infinity", here represented as a negative int */
    public static final int DEFAULT_STACK_LIMIT = -1;

    // This is used by "captureStackTrace"
    private static final String STACK_HIDE_KEY = "_stackHide";

    private RhinoException stackProvider;
    private Object stack;

    static void init(Scriptable scope, boolean sealed) {
        NativeError obj = new NativeError();
        ScriptableObject.putProperty(obj, "name", "Error");
        ScriptableObject.putProperty(obj, "message", "");
        ScriptableObject.putProperty(obj, "fileName", "");
        ScriptableObject.putProperty(obj, "lineNumber", 0);
        obj.setAttributes("name", ScriptableObject.DONTENUM);
        obj.setAttributes("message", ScriptableObject.DONTENUM);
        obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
        NativeCallSite.init(obj, sealed);
    }

    static NativeError make(Context cx, Scriptable scope, IdFunctionObject ctorObj, Object[] args) {
        Scriptable proto = (Scriptable) (ctorObj.get("prototype", ctorObj));

        NativeError obj = new NativeError();
        obj.setPrototype(proto);
        obj.setParentScope(scope);

        int arglen = args.length;
        if (arglen >= 1) {
            if (!Undefined.isUndefined(args[0])) {
                ScriptableObject.putProperty(obj, "message", ScriptRuntime.toString(args[0]));
                obj.setAttributes("message", ScriptableObject.DONTENUM);
            }
            if (arglen >= 2) {
                ScriptableObject.putProperty(obj, "fileName", args[1]);
                if (arglen >= 3) {
                    int line = ScriptRuntime.toInt32(args[2]);
                    ScriptableObject.putProperty(obj, "lineNumber", line);
                }
            }
        }
        return obj;
    }

    @Override
    protected void fillConstructorProperties(IdFunctionObject ctor) {
        addIdFunctionProperty(
                ctor, ERROR_TAG, ConstructorId_captureStackTrace, "captureStackTrace", 2);

        // Define a stack to be used even if this Error is never thrown.
        // The big cost is in turning this into an actual stack trace, which is set up to
        // be done lazily below.
        stackProvider = new EvaluatorException("");

        // This is running on the global "Error" object. Associate an object there that can store
        // default stack trace, etc.
        // This prevents us from having to add two additional fields to every Error object.
        ProtoProps protoProps = new ProtoProps();
        associateValue(ProtoProps.KEY, protoProps);

        ctor.defineProperty(
                "stackTraceLimit",
                protoProps::getStackTraceLimit,
                protoProps::setStackTraceLimit,
                0);
        ctor.defineProperty(
                "prepareStackTrace",
                protoProps::getPrepareStackTrace,
                protoProps::setPrepareStackTrace,
                0);

        super.fillConstructorProperties(ctor);
    }

    @Override
    public String getClassName() {
        return "Error";
    }

    @Override
    public String toString() {
        // According to spec, Error.prototype.toString() may return undefined.
        Object toString = js_toString(this);
        return toString instanceof String ? (String) toString : super.toString();
    }

    @Override
    protected void initPrototypeId(int id) {
        String s;
        int arity;
        switch (id) {
            case Id_constructor:
                arity = 1;
                s = "constructor";
                break;
            case Id_toString:
                arity = 0;
                s = "toString";
                break;
            case Id_toSource:
                arity = 0;
                s = "toSource";
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(id));
        }
        initPrototypeMethod(ERROR_TAG, id, s, arity);
    }

    @Override
    public Object execIdCall(
            IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        if (!f.hasTag(ERROR_TAG)) {
            return super.execIdCall(f, cx, scope, thisObj, args);
        }
        int id = f.methodId();
        switch (id) {
            case Id_constructor:
                NativeError err = make(cx, scope, f, args);
                // All new Errors have a default exception installed so that
                // there is a stack trace captured even if they are never thrown.
                err.setStackProvider(new EvaluatorException(""));
                return err;

            case Id_toString:
                return js_toString(thisObj);

            case Id_toSource:
                return js_toSource(cx, scope, thisObj);

            case ConstructorId_captureStackTrace:
                js_captureStackTrace(cx, scope, thisObj, args);
                return Undefined.instance;
        }
        throw new IllegalArgumentException(String.valueOf(id));
    }

    public void setStackProvider(RhinoException re) {
        // We go some extra miles to make sure the stack property is only
        // generated on demand, is cached after the first access, and is
        // overwritable like an ordinary property. Hence this setup with
        // the getter and setter below.
        if (stackProvider == null) {
            defineProperty("stack", this::getStackDelegated, this::setStackDelegated, DONTENUM);
        }
        stackProvider = re;
    }

    public Object getStackDelegated() {
        if (stack != null) {
            return stack;
        }
        if (stackProvider == null) {
            return NOT_FOUND;
        }

        // Get the object where prototype stuff is stored.
        int limit = DEFAULT_STACK_LIMIT;
        Function prepare = null;
        NativeError cons = (NativeError) getPrototype();
        ProtoProps pp = (ProtoProps) cons.getAssociatedValue(ProtoProps.KEY);

        if (pp != null) {
            limit = pp.stackTraceLimit;
            prepare = pp.prepareStackTrace;
        }

        // This key is only set by captureStackTrace
        String hideFunc = (String) getAssociatedValue(STACK_HIDE_KEY);
        ScriptStackElement[] stackTrace = stackProvider.getScriptStack(limit, hideFunc);

        // Determine whether to format the stack trace ourselves, or call the user's code to do it
        Object value;
        if (prepare == null) {
            value = RhinoException.formatStackTrace(stackTrace, stackProvider.details());
        } else {
            value = callPrepareStack(prepare, stackTrace);
        }
        stack = value;
        return value;
    }

    public void setStackDelegated(Object value) {
        stackProvider = null;
        stack = value;
    }

    private Object callPrepareStack(Function prepare, ScriptStackElement[] stack) {
        Context cx = Context.getCurrentContext();
        Object[] elts = new Object[stack.length];

        // The "prepareStackTrace" function takes an array of CallSite objects.
        for (int i = 0; i < stack.length; i++) {
            NativeCallSite site = (NativeCallSite) cx.newObject(this, "CallSite");
            site.setElement(stack[i]);
            elts[i] = site;
        }

        Scriptable eltArray = cx.newArray(this, elts);
        return prepare.call(cx, prepare, this, new Object[] {this, eltArray});
    }

    private static Object js_toString(Scriptable thisObj) {
        Object nameObj = ScriptableObject.getProperty(thisObj, "name");
        String name;
        if (nameObj == NOT_FOUND || Undefined.isUndefined(nameObj)) {
            name = "Error";
        } else {
            name = ScriptRuntime.toString(nameObj);
        }
        Object msgObj = ScriptableObject.getProperty(thisObj, "message");
        String msg;
        if (msgObj == NOT_FOUND || Undefined.isUndefined(msgObj)) {
            msg = "";
        } else {
            msg = ScriptRuntime.toString(msgObj);
        }
        if (name.isEmpty()) {
            return msg;
        } else if (msg.isEmpty()) {
            return name;
        } else {
            return name + ": " + msg;
        }
    }

    private static String js_toSource(Context cx, Scriptable scope, Scriptable thisObj) {
        // Emulation of SpiderMonkey behavior
        Object name = ScriptableObject.getProperty(thisObj, "name");
        Object message = ScriptableObject.getProperty(thisObj, "message");
        Object fileName = ScriptableObject.getProperty(thisObj, "fileName");
        Object lineNumber = ScriptableObject.getProperty(thisObj, "lineNumber");

        StringBuilder sb = new StringBuilder();
        sb.append("(new ");
        if (name == NOT_FOUND) {
            name = Undefined.instance;
        }
        sb.append(ScriptRuntime.toString(name));
        sb.append("(");
        if (message != NOT_FOUND || fileName != NOT_FOUND || lineNumber != NOT_FOUND) {
            if (message == NOT_FOUND) {
                message = "";
            }
            sb.append(ScriptRuntime.uneval(cx, scope, message));
            if (fileName != NOT_FOUND || lineNumber != NOT_FOUND) {
                sb.append(", ");
                if (fileName == NOT_FOUND) {
                    fileName = "";
                }
                sb.append(ScriptRuntime.uneval(cx, scope, fileName));
                if (lineNumber != NOT_FOUND) {
                    int line = ScriptRuntime.toInt32(lineNumber);
                    if (line != 0) {
                        sb.append(", ");
                        sb.append(ScriptRuntime.toString(line));
                    }
                }
            }
        }
        sb.append("))");
        return sb.toString();
    }

    private static void js_captureStackTrace(
            Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        ScriptableObject obj = (ScriptableObject) ScriptRuntime.toObject(cx, scope, args[0]);
        Function func = null;
        if (args.length > 1) {
            func = (Function) ScriptRuntime.toObjectOrNull(cx, args[1], scope);
        }

        // Create a new error that will have the correct prototype so we can re-use "getStackTrace"
        NativeError err = (NativeError) cx.newObject(thisObj, "Error");
        // Wire it up so that it will have an actual exception with a stack trace
        err.setStackProvider(new EvaluatorException("[object Object]"));

        // Figure out if they passed a function used to hide part of the stack
        if (func != null) {
            Object funcName = func.get("name", func);
            if ((funcName != null) && !Undefined.isUndefined(funcName)) {
                err.associateValue(STACK_HIDE_KEY, Context.toString(funcName));
            }
        }

        // from https://v8.dev/docs/stack-trace-api
        // Error.captureStackTrace(error, constructorOpt)
        // adds a stack property to the given error object that yields the stack trace
        // at the time captureStackTrace was called. Stack traces collected through
        // Error.captureStackTrace are immediately collected, formatted,
        // and attached to the given error object.
        obj.defineProperty("stack", err.get("stack"), ScriptableObject.DONTENUM);
    }

    @Override
    protected int findPrototypeId(String s) {
        int id;
        switch (s) {
            case "constructor":
                id = Id_constructor;
                break;
            case "toString":
                id = Id_toString;
                break;
            case "toSource":
                id = Id_toSource;
                break;
            default:
                id = 0;
                break;
        }
        return id;
    }

    private static final int Id_constructor = 1,
            Id_toString = 2,
            Id_toSource = 3,
            ConstructorId_captureStackTrace = -1,
            MAX_PROTOTYPE_ID = 3;

    /**
     * We will attch this object to the constructor and use it solely to store the constructor
     * properties that are "global." We can't make them static because there can be many contexts in
     * the same JVM.
     */
    private static final class ProtoProps implements Serializable {
        static final String KEY = "_ErrorPrototypeProps";

        private static final long serialVersionUID = 1907180507775337939L;

        int stackTraceLimit = DEFAULT_STACK_LIMIT;
        Function prepareStackTrace;

        public Object getStackTraceLimit() {
            if (stackTraceLimit >= 0) {
                return stackTraceLimit;
            }
            return Double.POSITIVE_INFINITY;
        }

        public void setStackTraceLimit(Object value) {
            double limit = Context.toNumber(value);
            if (Double.isNaN(limit) || Double.isInfinite(limit)) {
                stackTraceLimit = -1;
            } else {
                stackTraceLimit = (int) limit;
            }
        }

        public Object getPrepareStackTrace() {
            return (prepareStackTrace == null ? Undefined.instance : prepareStackTrace);
        }

        public void setPrepareStackTrace(Object value) {
            if ((value == null) || Undefined.isUndefined(value)) {
                prepareStackTrace = null;
            } else if (value instanceof Function) {
                prepareStackTrace = (Function) value;
            }
        }
    }
}
