/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.redukti.cer.ir;

import org.redukti.cer.debug.DebuggableScript;
import org.redukti.cer.utils.UintMap;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public final class InterpreterData implements Serializable, DebuggableScript {
    private static final long serialVersionUID = 5067677351589230234L;

    static final int INITIAL_MAX_ICODE_LENGTH = 1024;
    static final int INITIAL_STRINGTABLE_SIZE = 64;
    static final int INITIAL_NUMBERTABLE_SIZE = 64;
    static final int INITIAL_BIGINTTABLE_SIZE = 64;

    InterpreterData(
            int languageVersion, String sourceFile, String encodedSource, boolean isStrict) {
        this.languageVersion = languageVersion;
        this.itsSourceFile = sourceFile;
        this.encodedSource = encodedSource;
        this.isStrict = isStrict;
        init();
    }

    InterpreterData(InterpreterData parent) {
        this.parentData = parent;
        this.languageVersion = parent.languageVersion;
        this.itsSourceFile = parent.itsSourceFile;
        this.encodedSource = parent.encodedSource;
        this.isStrict = parent.isStrict;
        init();
    }

    private void init() {
        itsICode = new byte[INITIAL_MAX_ICODE_LENGTH];
        itsStringTable = new String[INITIAL_STRINGTABLE_SIZE];
        itsBigIntTable = new BigInteger[INITIAL_BIGINTTABLE_SIZE];
    }

    public String itsName;
    public String itsSourceFile;
    public boolean itsNeedsActivation;
    public int itsFunctionType;

    public String[] itsStringTable;
    public double[] itsDoubleTable;
    public BigInteger[] itsBigIntTable;
    public InterpreterData[] itsNestedFunctions;
    public Object[] itsRegExpLiterals;
    public Object[] itsTemplateLiterals;

    public byte[] itsICode;

    public int[] itsExceptionTable;

    public int itsMaxVars;
    public int itsMaxLocals;
    public int itsMaxStack;
    public int itsMaxFrameArray;

    // see comments in NativeFuncion for definition of argNames and argCount
    public String[] argNames;
    public boolean[] argIsConst;
    public int argCount;

    int itsMaxCalleeArgs;

    public String encodedSource;
    public int encodedSourceStart;
    public int encodedSourceEnd;

    public int languageVersion;

    public boolean isStrict;
    boolean topLevel;
    public boolean isES6Generator;

    public Object[] literalIds;

    public UintMap longJumps;

    public int firstLinePC = -1; // PC for the first LINE icode

    InterpreterData parentData;

    public boolean evalScriptFlag; // true if script corresponds to eval() code

    private int icodeHashCode = 0;

    /** true if the function has been declared like "var foo = function() {...}" */
    boolean declaredAsVar;

    /** true if the function has been declared like "!function() {}". */
    public boolean declaredAsFunctionExpression;

    @Override
    public boolean isTopLevel() {
        return topLevel;
    }

    @Override
    public boolean isFunction() {
        return itsFunctionType != 0;
    }

    @Override
    public String getFunctionName() {
        return itsName;
    }

    @Override
    public int getParamCount() {
        return argCount;
    }

    @Override
    public int getParamAndVarCount() {
        return argNames.length;
    }

    @Override
    public String getParamOrVarName(int index) {
        return argNames[index];
    }

    public boolean getParamOrVarConst(int index) {
        return argIsConst[index];
    }

    @Override
    public String getSourceName() {
        return itsSourceFile;
    }

    // FIXME (dibyendu)
//    @Override
//    public boolean isGeneratedScript() {
//        return ScriptRuntime.isGeneratedScript(itsSourceFile);
//    }
//
//    @Override
//    public int[] getLineNumbers() {
//        return Interpreter.getLineNumbers(this);
//    }

    @Override
    public int getFunctionCount() {
        return (itsNestedFunctions == null) ? 0 : itsNestedFunctions.length;
    }

    @Override
    public DebuggableScript getFunction(int index) {
        return itsNestedFunctions[index];
    }

    @Override
    public DebuggableScript getParent() {
        return parentData;
    }

    public int icodeHashCode() {
        int h = icodeHashCode;
        if (h == 0) {
            icodeHashCode = h = Arrays.hashCode(itsICode);
        }
        return h;
    }
}
