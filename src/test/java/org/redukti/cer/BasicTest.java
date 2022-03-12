package org.redukti.cer;

import org.junit.Test;
import org.redukti.cer.runtime.Context;
import org.redukti.cer.runtime.ContextFactory;
import org.redukti.cer.runtime.Script;
import org.redukti.cer.runtime.ScriptableObject;

public class BasicTest {

    @Test
    public void simpleTest() {
        String script = "var s = 42;";

        ContextFactory contextFactory = new ContextFactory();
        try (Context cx = contextFactory.enterContext()) {
            Script s = cx.compileString(script, "", 1, null);
            ScriptableObject topLevelScope = cx.initSafeStandardObjects();
            s.exec(cx, topLevelScope);
        }
    }
}
