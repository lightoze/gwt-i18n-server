package net.lightoze.gwt.i18n.server;

import net.lightoze.gwt.i18n.client.LocaleFactory;
import net.lightoze.gwt.i18n.client.TestMessages;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;

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
        LocaleProxy.initialize();
    }

    @Test
    public void simple() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("fi", "FI", "var"));
        Assert.assertEquals("Simple текст", getMessages().simple());
    }

    @Test
    public void lookup() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("fi", "FI", "var"));
        Assert.assertEquals("Simple текст", getMessages().getString("simple"));
    }

    @Test
    public void plural() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("it"));
        Assert.assertEquals("Zero apples", getMessages().apples(0));
        Assert.assertEquals("An 1 apple", getMessages().apples(1));

        ThreadLocalLocaleProvider.pushLocale(new Locale("en"));
        Assert.assertEquals("No apples", getMessages().apples(0));
        Assert.assertEquals("One apple", getMessages().apples(1));
        Assert.assertEquals("2 apples", getMessages().apples(2));
    }

    @Test
    public void select() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("en"));
        Assert.assertEquals("One 0 selected", getMessages().select(1, "ONE"));
        Assert.assertEquals("Two selected", getMessages().select(2, "TWO"));
        Assert.assertEquals("Not two selected", getMessages().select(2, "XXX"));
        Assert.assertEquals("Three selected", getMessages().select(3, "THREE"));
        Assert.assertEquals("Four selected", getMessages().select(4, "FOUR"));
    }

    @Test
    public void selectLocalized() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("fi"));
        Assert.assertEquals("Three selected localized", getMessages().select(3, "THREE"));
    }

    @Test
    public void dates() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("en_US"));
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        Assert.assertEquals("Today is January 1, 1970", getMessages().today(cal.getTime()));
    }

    @Test
    public void datesWithProvidedLocale() {
        TestMessages testMessages = LocaleFactory.get(TestMessages.class, "en");
        Calendar cal = Calendar.getInstance();
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        Assert.assertEquals("Today is January 1, 1970", testMessages.today(cal.getTime()));
    }

    @Test
    public void inheritance() {
        ThreadLocalLocaleProvider.pushLocale(new Locale("fi"));
        Assert.assertEquals("Perintö 1", getMessages().inheritance1());
        Assert.assertEquals("Perintö 2", getMessages().inheritance2());
        Assert.assertEquals("Inheritance 3", getMessages().inheritance3());
    }
}
