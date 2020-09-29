package edu.umd.cs.eclipse.courseProjectManager;

import org.junit.*;

public class SubmitIgnoreFilterTest extends SubmitIgnoreFilter
{
    boolean deepMatches(final String filename) {
        final boolean shallow = this.matches(filename);
        final boolean deep = this.matches("foobar/" + filename);
        if (shallow != deep) {
            throw new AssertionError((Object)("Inconsistent results for " + filename));
        }
        return shallow;
    }
    
    @Test
    public void test() {
        Assert.assertFalse(this.deepMatches("Foo.java"));
        Assert.assertTrue(this.deepMatches("Foo.class"));
        Assert.assertFalse(this.deepMatches("Foo.c"));
        Assert.assertTrue(this.deepMatches("Foo.o"));
        Assert.assertTrue(this.deepMatches(".git"));
        Assert.assertTrue(this.deepMatches("core"));
        Assert.assertTrue(this.deepMatches("CVS"));
        Assert.assertFalse(this.deepMatches("notCVS"));
        Assert.assertTrue(this.deepMatches("Foo.exe"));
        Assert.assertTrue(this.deepMatches("Foo.BAK"));
        Assert.assertTrue(this.deepMatches("Foo.bak"));
        Assert.assertTrue(this.deepMatches("Foo.old"));
        Assert.assertTrue(this.deepMatches("Foo.java~"));
    }
}
