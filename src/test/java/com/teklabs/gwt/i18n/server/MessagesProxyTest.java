package com.teklabs.gwt.i18n.server;

import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import com.teklabs.gwt.i18n.client.LocaleFactory;

/**
 * @author Vladimir Kulev
 */
public class MessagesProxyTest {
    private TestMessages getMessages() {
        return LocaleFactory.get(TestMessages.class);
    }

    @Test
    public void simple() {
        MessagesProxy.setLocale(new Locale("fi", "FI", "001"));
        Assert.assertEquals("Simple текст", getMessages().simple());
    }

    @Test
    public void plural() {
        MessagesProxy.setLocale(new Locale("en"));
        Assert.assertEquals("No apples", getMessages().apples(0));
        Assert.assertEquals("An 1 apple", getMessages().apples(1));
        Assert.assertEquals("2 apples", getMessages().apples(2));
    }

    @Test
    public void select() {
        MessagesProxy.setLocale(new Locale("en"));
        Assert.assertEquals("One 0 selected", getMessages().select(1, "select one"));
        Assert.assertEquals("Two selected", getMessages().select(2, "select two"));
        Assert.assertEquals("Not two selected", getMessages().select(2, "not select"));
        Assert.assertEquals("Three selected", getMessages().select(3, "select three"));
        Assert.assertEquals("Four selected", getMessages().select(4, "select four"));
    }

	@Test
	public void dates() {
		MessagesProxy.setLocale(new Locale("en"));
		Assert.assertEquals("Today is January 1, 1970", getMessages().today(new Date(0)));
	}
}
