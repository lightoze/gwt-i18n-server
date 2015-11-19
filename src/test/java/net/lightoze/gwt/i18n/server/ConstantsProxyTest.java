package net.lightoze.gwt.i18n.server;

import net.lightoze.gwt.i18n.client.LocaleFactory;
import net.lightoze.gwt.i18n.client.TestConstants;
import org.junit.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Vladimir Kulev
 */
public class ConstantsProxyTest {
    private TestConstants getConstants() {
        return LocaleFactory.get(TestConstants.class);
    }

    @Test
    public void test() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("en"));
        assertEquals(1, getConstants().getInt("count"));
        assertEquals(1, getConstants().count());
        assertEquals(1.0, getConstants().getDouble("pi"), 0);
        assertEquals(1.0, getConstants().pi(), 0);
        {
            Map<String, String> map = getConstants().map();
            assertEquals(3, map.size());
            assertEquals("b", map.get("a"));
            assertEquals("value1", map.get("key1"));
            assertEquals("value2", map.get("key2"));
        }
        assertEquals("value1", getConstants().key1());

        ThreadLocalLocaleProvider.pushLocale(new Locale("fi", "FI"));
        assertEquals(2, getConstants().count());
        assertEquals(3.14, getConstants().getDouble("pi"), 0);
        assertEquals(3.14, getConstants().pi(), 0);

        {
            Map<String, String> map = getConstants().map();
            assertEquals(2, map.size());
            assertEquals("x", map.get("a"));
            assertEquals("d", map.get("c"));
        }
        assertEquals("key1fi", getConstants().key1());
    }
}
