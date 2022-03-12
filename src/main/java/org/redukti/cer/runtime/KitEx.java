package org.redukti.cer.runtime;

import org.redukti.cer.utils.Kit;

public class KitEx {

        /** Check that testClass is accessible from the given loader. */
    static boolean testIfCanLoadRhinoClasses(ClassLoader loader) {
        Class<?> testClass = ScriptRuntime.ContextFactoryClass;
        Class<?> x = Kit.classOrNull(loader, testClass.getName());
        if (x != testClass) {
            // The check covers the case when x == null =>
            // loader does not know about testClass or the case
            // when x != null && x != testClass =>
            // loader loads a class unrelated to testClass
            return false;
        }
        return true;
    }

}
