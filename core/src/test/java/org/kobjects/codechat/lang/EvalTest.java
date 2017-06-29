package org.kobjects.codechat.lang;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EvalTest {
    TestShell shell;

    @Before
    public void setup() {
        shell = new TestShell();
    }

    @Test
    public void testPrint() {
        shell.eval("print \"Hello World\"");
        assertEquals("Hello World", shell.output.get(0));
    }

    @Test
    public void testArrays() {
        // assertEquals(Double.valueOf(3), shell.eval("size(array(1,2,3))"));
        assertEquals(Double.valueOf(2), shell.eval("List(1,2,3)[1]"));
    }

    @Test
    public void testSets() {
        assertEquals(Double.valueOf(2), shell.eval("Set(1,2,2).size"));
    }

    @Test
    public void testExpressions() {
        assertEquals(Double.valueOf(-4), shell.eval("4-4-4"));
    }


    @Test
    public void testCount() {
        shell.eval("count x to 3 : print x; end");
        assertEquals(3, shell.output.size());
        assertEquals("0.0", shell.output.get(0));
        assertEquals("2.0", shell.output.get(2));
    }
}
