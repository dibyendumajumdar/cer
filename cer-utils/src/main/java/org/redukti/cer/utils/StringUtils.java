package org.redukti.cer.utils;

import org.redukti.cer.runtime.support.DToA;
import org.redukti.cer.runtime.support.DoubleConversion;
import org.redukti.cer.runtime.support.FastDtoa;

public class StringUtils {
    public static String escapeString(String s) {
        return escapeString(s, '"');
    }

    /**
     * For escaping strings printed by object and array literals; not quite the same as 'escape.'
     */
    public static String escapeString(String s, char escapeQuote) {
        if (!(escapeQuote == '"' || escapeQuote == '\'')) Kit.codeBug();
        StringBuilder sb = null;

        for (int i = 0, L = s.length(); i != L; ++i) {
            int c = s.charAt(i);

            if (' ' <= c && c <= '~' && c != escapeQuote && c != '\\') {
                // an ordinary print character (like C isprint()) and not "
                // or \ .
                if (sb != null) {
                    sb.append((char) c);
                }
                continue;
            }
            if (sb == null) {
                sb = new StringBuilder(L + 3);
                sb.append(s);
                sb.setLength(i);
            }

            int escape = -1;
            switch (c) {
                case '\b':
                    escape = 'b';
                    break;
                case '\f':
                    escape = 'f';
                    break;
                case '\n':
                    escape = 'n';
                    break;
                case '\r':
                    escape = 'r';
                    break;
                case '\t':
                    escape = 't';
                    break;
                case 0xb:
                    escape = 'v';
                    break; // Java lacks \v.
                case ' ':
                    escape = ' ';
                    break;
                case '\\':
                    escape = '\\';
                    break;
            }
            if (escape >= 0) {
                // an \escaped sort of character
                sb.append('\\');
                sb.append((char) escape);
            } else if (c == escapeQuote) {
                sb.append('\\');
                sb.append(escapeQuote);
            } else {
                int hexSize;
                if (c < 256) {
                    // 2-digit hex
                    sb.append("\\x");
                    hexSize = 2;
                } else {
                    // Unicode.
                    sb.append("\\u");
                    hexSize = 4;
                }
                // append hexadecimal form of c left-padded with 0
                for (int shift = (hexSize - 1) * 4; shift >= 0; shift -= 4) {
                    int digit = 0xf & (c >> shift);
                    int hc = (digit < 10) ? '0' + digit : 'a' - 10 + digit;
                    sb.append((char) hc);
                }
            }
        }
        return (sb == null) ? s : sb.toString();
    }

    /**
     * Return -1L if str is not an index, or the index value as lower 32 bits of the result. Note
     * that the result needs to be cast to an int in order to produce the actual index, which may be
     * negative.
     */
    public static long indexFromString(String str) {
        // The length of the decimal string representation of
        //  Integer.MAX_VALUE, 2147483647
        final int MAX_VALUE_LENGTH = 10;

        int len = str.length();
        if (len > 0) {
            int i = 0;
            boolean negate = false;
            int c = str.charAt(0);
            if (c == '-') {
                if (len > 1) {
                    c = str.charAt(1);
                    if (c == '0') return -1L; // "-0" is not an index
                    i = 1;
                    negate = true;
                }
            }
            c -= '0';
            if (0 <= c && c <= 9 && len <= (negate ? MAX_VALUE_LENGTH + 1 : MAX_VALUE_LENGTH)) {
                // Use negative numbers to accumulate index to handle
                // Integer.MIN_VALUE that is greater by 1 in absolute value
                // then Integer.MAX_VALUE
                int index = -c;
                int oldIndex = 0;
                i++;
                if (index != 0) {
                    // Note that 00, 01, 000 etc. are not indexes
                    while (i != len && 0 <= (c = str.charAt(i) - '0') && c <= 9) {
                        oldIndex = index;
                        index = 10 * index - c;
                        i++;
                    }
                }
                // Make sure all characters were consumed and that it couldn't
                // have overflowed.
                if (i == len
                        && (oldIndex > (Integer.MIN_VALUE / 10)
                        || (oldIndex == (Integer.MIN_VALUE / 10)
                        && c
                        <= (negate
                        ? -(Integer.MIN_VALUE % 10)
                        : (Integer.MAX_VALUE % 10))))) {
                    return 0xFFFFFFFFL & (negate ? index : -index);
                }
            }
        }
        return -1L;
    }

    /** If s represents index, then return index value wrapped as Integer and othewise return s. */
    public static Object getIndexObject(String s) {
        long indexTest = indexFromString(s);
        if (indexTest >= 0) {
            return Integer.valueOf((int) indexTest);
        }
        return s;
    }

    /**
     * If d is exact int value, return its value wrapped as Integer and othewise return d converted
     * to String.
     */
    public static Object getIndexObject(double d) {
        int i = (int) d;
        if (i == d) {
            return Integer.valueOf(i);
        }
        return toString(d);
    }

    /** Optimized version of toString(Object) for numbers. */
    public static String toString(double val) {
        return numberToString(val, 10);
    }

    public static String numberToString(double d, int base) {
        if (Double.isNaN(d)) return "NaN";
        if (d == Double.POSITIVE_INFINITY) return "Infinity";
        if (d == Double.NEGATIVE_INFINITY) return "-Infinity";
        if (d == 0.0) return "0";

        if (base != 10) {
            return DToA.JS_dtobasestr(base, d);
        }
        // V8 FastDtoa can't convert all numbers, so try it first but
        // fall back to old DToA in case it fails
        String result = FastDtoa.numberToString(d);
        if (result != null) {
            return result;
        }
        StringBuilder buffer = new StringBuilder();
        DToA.JS_dtostr(buffer, DToA.DTOSTR_STANDARD, 0, d);
        return buffer.toString();
    }

    public static int toInt32(double d) {
        return DoubleConversion.doubleToInt32(d);
    }

}
