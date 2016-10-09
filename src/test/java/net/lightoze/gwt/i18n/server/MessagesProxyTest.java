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
public class MessagesProxyTest {
    private TestMessagesLookup getMessages() {
        return LocaleFactory.get(TestMessagesLookup.class);
    }

    @BeforeClass
    public static void init() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void simple() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("fi", "FI", "var"));
        assertEquals("Simple текст", getMessages().simple());
        assertEquals("Str: <b>{0}</b>", getMessages().substring("{0}"));
        assertEquals("Str: <b>{1}</b>", getMessages().substring("{1}"));
        assertEquals("Str: <b>{\\|test}</b>", getMessages().substring("{\\|test}"));
    }

    @Test
    public void lookup() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("fi", "FI", "var"));
        assertEquals("Simple текст", getMessages().getString("simple"));
    }

    @Test
    public void plural() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("it"));
        assertEquals("Zero apples", getMessages().apples(0));
        assertEquals("An 1 apple", getMessages().apples(1));

        ThreadLocalLocaleProvider.pushLocale(new Locale("en"));
        assertEquals("No apples", getMessages().apples(0));
        assertEquals("One apple", getMessages().apples(1));
        assertEquals("2 apples", getMessages().apples(2));
    }

    @Test
    public void select() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("en"));
        assertEquals("One 0 selected", getMessages().select(1, "ONE"));
        assertEquals("Two selected", getMessages().select(2, "TWO"));
        assertEquals("Not two selected", getMessages().select(2, "XXX"));
        assertEquals("Three selected", getMessages().select(3, "THREE"));
        assertEquals("Four selected", getMessages().select(4, "FOUR"));
    }

    @Test
    public void selectLocalized() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("fi"));
        assertEquals("Three selected localized", getMessages().select(3, "THREE"));
    }

    @Test
    public void dates() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("en_US"));
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("Today is January 1, 1970", getMessages().today(cal.getTime()));
    }

    @Test
    public void datesWithProvidedLocale() {
        TestMessages testMessages = LocaleFactory.get(TestMessages.class, "fi");
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("Today is 1. tammikuuta 1970", testMessages.today(cal.getTime()));
    }

    @Test
    public void inheritance() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("fi"));
        assertEquals("Perintö 1", getMessages().inheritance1());
        assertEquals("Perintö 2", getMessages().inheritance2());
        assertEquals("Inheritance 3", getMessages().inheritance3());
    }
}
