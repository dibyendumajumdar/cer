package org.redukti.cer;

public interface Versions {
    /**
     * Language versions.
     *
     * <p>All integral values are reserved for future version numbers.
     */

    /**
     * The unknown version.
     *
     * <p>Be aware, this version will not support many of the newer language features and will not
     * change in the future.
     *
     * <p>Please use one of the other constants like VERSION_ES6 to get support for recent language
     * features.
     */
    public static final int VERSION_UNKNOWN = -1;

    /** The default version. */
    public static final int VERSION_DEFAULT = 0;

    /** JavaScript 1.0 */
    public static final int VERSION_1_0 = 100;

    /** JavaScript 1.1 */
    public static final int VERSION_1_1 = 110;

    /** JavaScript 1.2 */
    public static final int VERSION_1_2 = 120;

    /** JavaScript 1.3 */
    public static final int VERSION_1_3 = 130;

    /** JavaScript 1.4 */
    public static final int VERSION_1_4 = 140;

    /** JavaScript 1.5 */
    public static final int VERSION_1_5 = 150;

    /** JavaScript 1.6 */
    public static final int VERSION_1_6 = 160;

    /** JavaScript 1.7 */
    public static final int VERSION_1_7 = 170;

    /** JavaScript 1.8 */
    public static final int VERSION_1_8 = 180;

    /** ECMAScript 6. */
    public static final int VERSION_ES6 = 200;
}
