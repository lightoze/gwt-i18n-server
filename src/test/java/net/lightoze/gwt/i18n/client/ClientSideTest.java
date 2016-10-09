package net.lightoze.gwt.i18n.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import java.util.Date;
import java.util.Map;

/**
 * @author Vladimir Kulev
 */
public class ClientSideTest extends GWTTestCase {
    @Override
    public String getModuleName() {
        return getClass().getName();
    }

    private TestMessages getMessages() {
        return LocaleFactory.get(TestMessages.class);
    }

    private TestConstants getConstants() {
        return LocaleFactory.get(TestConstants.class);
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        LocaleFactory.put(TestMessages.class, GWT.<TestMessages>create(TestMessages.class));
        LocaleFactory.put(TestConstants.class, GWT.<TestConstants>create(TestConstants.class));
    }

    public void testScriptMode() {
        assertTrue("GWT not in JavaScript mode", GWT.isScript());
    }

    public void testSimple() {
        assertEquals("simple", getMessages().simple());
        assertEquals("Str: <b>{0}</b>", getMessages().substring("{0}"));
        assertEquals("Str: <b>{1}</b>", getMessages().substring("{1}"));
        assertEquals("Str: <b>{\\|test}</b>", getMessages().substring("{\\|test}"));
    }

    public void testPlural() {
        assertEquals("No apples", getMessages().apples(0));
        assertEquals("One apple", getMessages().apples(1));
        assertEquals("2 apples", getMessages().apples(2));
    }

    public void testSelect() {
        assertEquals("One 0 selected", getMessages().select(1, "ONE"));
        assertEquals("Two selected", getMessages().select(2, "TWO"));
        assertEquals("Not two selected", getMessages().select(2, "XXX"));
        assertEquals("Three selected", getMessages().select(3, "THREE"));
        assertEquals("Four selected", getMessages().select(4, "FOUR"));
    }

    public void testConstants() {
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
    }

    public void testDate() {
        Date date = new Date(0l);
        CalendarUtil.setToFirstDayOfMonth(date);
        assertEquals("Today is January 1, 1970", getMessages().today(date));
    }
}
