package net.lightoze.gwt.i18n.server;

import net.lightoze.gwt.i18n.client.LocaleFactory;
import net.lightoze.gwt.i18n.client.TestMessages;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * @author Vladimir Kulev
 */
public class EncoderTest {
    @BeforeClass
    public static void init() {
        ThreadLocalLocaleProvider.pushLocale(Locale.ENGLISH);
    }

    private TestMessages getMessages() {
        return LocaleFactory.getEncoder(TestMessages.class);
    }

    @Test
    public void messagesNoParams() {
        String str = getMessages().simple();
        assertEquals("{net.lightoze.gwt.i18n.client.TestMessages#simple}", str);
        assertEquals("Simple текст-Simple текст", LocaleProxy.decode(str + "-" + str, new Locale("fi")));
    }

    @Test
    public void simpleParams() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("en"));

        String str = getMessages().apples(1);
        assertEquals("{net.lightoze.gwt.i18n.client.TestMessages#apples?i=1}", str);
        assertEquals("One apple", LocaleProxy.decode(str));
        assertEquals("An 1 apple", LocaleProxy.decode(str, new Locale("fi")));

        str = getMessages().select(2, "TWO");
        assertEquals("{net.lightoze.gwt.i18n.client.TestMessages#select?i=2?s=TWO}", str);
        assertEquals("Two selected", LocaleProxy.decode(str));
    }

    @Test
    public void escapedParams() {
        String str = getMessages().substring("{\\0?#&}");
        assertEquals("{net.lightoze.gwt.i18n.client.TestMessages#substring?s=%7B%5C0%3F%23%26%7D}", str);
        assertEquals("Str: {\\0?#&}", LocaleProxy.decode(str));
    }

    @Test
    public void dateParams() {
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        String str = getMessages().today(cal.getTime());
        assertEquals("{net.lightoze.gwt.i18n.client.TestMessages#today?t=" + cal.getTimeInMillis() + "}", str);

        ThreadLocalLocaleProvider.pushLocale(new Locale("fi", "FI", "var"));
        assertEquals("Today is 1. tammikuuta 1970", LocaleProxy.decode(str));
    }
}
