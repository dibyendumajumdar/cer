package org.redukti.cer.utils;

public class Limits {
    /** Default stack limit is set to "Infinity", here represented as a negative int */
    public static final int DEFAULT_STACK_LIMIT = -1;
    /** @see <a href="https://www.ecma-international.org/ecma-262/6.0/#sec-number.max_safe_integer">sec-number.max_safe_integer</a> */
    public static final double MAX_SAFE_INTEGER = 9007199254740991.0; // Math.pow(2, 53) - 1
}
