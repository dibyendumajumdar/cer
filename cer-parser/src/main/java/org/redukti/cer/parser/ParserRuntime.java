package org.redukti.cer.parser;

public class ParserRuntime {

    // It is public so NativeRegExp can access it.
    public static boolean isJSLineTerminator(int c) {
        // Optimization for faster check for eol character:
        // they do not have 0xDFD0 bits set
        if ((c & 0xDFD0) != 0) {
            return false;
        }
        return c == '\n' || c == '\r' || c == 0x2028 || c == 0x2029;
    }
}
