package org.redukti.cer.ir;

public interface InterpreterConstants {
    static final int EXCEPTION_TRY_START_SLOT = 0;
    static final int EXCEPTION_TRY_END_SLOT = 1;
    static final int EXCEPTION_HANDLER_SLOT = 2;
    static final int EXCEPTION_TYPE_SLOT = 3;
    static final int EXCEPTION_LOCAL_SLOT = 4;
    static final int EXCEPTION_SCOPE_SLOT = 5;
    // SLOT_SIZE: space for try start/end, handler, start, handler type,
    //            exception local and scope local
    static final int EXCEPTION_SLOT_SIZE = 6;


    Object[] emptyArgs = new Object[0];
}
