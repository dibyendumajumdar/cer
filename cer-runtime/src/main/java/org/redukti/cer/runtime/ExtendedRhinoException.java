package org.redukti.cer.runtime;

import org.redukti.cer.exception.RhinoException;

public class ExtendedRhinoException extends RhinoException {

    public ExtendedRhinoException() {
        super();
        Evaluator e = Context.createInterpreter();
        if (e != null) e.captureStackInfo(this);
    }

    public ExtendedRhinoException(String details) {
        super(details);
        Evaluator e = Context.createInterpreter();
        if (e != null) e.captureStackInfo(this);
    }
}
