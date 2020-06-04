package mykidong.raft.util;

import mykidong.raft.test.TestBase;
import org.junit.Assert;
import org.junit.Test;

public class StringUtilsTest extends TestBase {

    @Test
    public void removeSuffixSlash() throws Exception {
        Assert.assertTrue(StringUtils.removeSuffixSlash("/a/b/c").equals("/a/b/c"));
        Assert.assertTrue(StringUtils.removeSuffixSlash("/a/b/c/").equals("/a/b/c"));
    }
}
