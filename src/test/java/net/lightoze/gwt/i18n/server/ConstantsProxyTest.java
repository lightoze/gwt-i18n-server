package net.lightoze.gwt.i18n.server;

import net.lightoze.gwt.i18n.client.LocaleFactory;
import net.lightoze.gwt.i18n.client.TestConstants;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;

/**
 * @author Vladimir Kulev
 */
public class ConstantsProxyTest {
    private TestConstants getConstants() {
        return LocaleFactory.get(TestConstants.class);
    }

    @BeforeClass
    public static void init() {
        LocaleProxy.initialize();
    }

    @Test
    public void test() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("en"));
        Assert.assertEquals(1, getConstants().getInt("count"));
        Assert.assertEquals(1, getConstants().count());
        Assert.assertEquals(1.0, getConstants().getDouble("pi"), 0);
        Assert.assertEquals(1.0, getConstants().pi(), 0);
        {
            Map<String, String> map = getConstants().map();
            Assert.assertEquals(3, map.size());
            Assert.assertEquals("b", map.get("a"));
            Assert.assertEquals("value1", map.get("key1"));
            Assert.assertEquals("value2", map.get("key2"));
        }
        Assert.assertEquals("value1", getConstants().key1());

        ThreadLocalLocaleProvider.pushLocale(new Locale("fi", "FI"));
        Assert.assertEquals(2, getConstants().count());
        Assert.assertEquals(3.14, getConstants().getDouble("pi"), 0);
        Assert.assertEquals(3.14, getConstants().pi(), 0);

        {
            Map<String, String> map = getConstants().map();
            Assert.assertEquals(2, map.size());
            Assert.assertEquals("x", map.get("a"));
            Assert.assertEquals("d", map.get("c"));
        }
        Assert.assertEquals("key1fi", getConstants().key1());
    }
}
