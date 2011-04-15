package com.teklabs.gwt_i18n_server;

import com.teklabs.gwt_i18n_server.client.LocaleFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

/**
 * @author Vladimir Kulev
 */
public class MessagesProxyTest {
    private TestMessages getMessages() {
        return LocaleFactory.getMessages(TestMessages.class);
    }

    @Test
    public void simple() {
        MessagesProxy.setLocale(new Locale("fi", "FI"));
        Assert.assertEquals("Simple text", getMessages().simple());
    }

    @Test
    public void plural() {
        MessagesProxy.setLocale(new Locale("en"));
        Assert.assertEquals("No apples", getMessages().apples(0));
        Assert.assertEquals("An 1 apple", getMessages().apples(1));
        Assert.assertEquals("2 apples", getMessages().apples(2));
    }
}
