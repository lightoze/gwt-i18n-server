package com.teklabs.gwt_i18n_server;

import com.teklabs.gwt_i18n_server.client.LocaleFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

/**
 * @author Vladimir Kulev
 */
public class ConstantsProxyTest {
    private TestConstants getConstants() {
        return LocaleFactory.get(TestConstants.class);
    }

    @Test
    public void test() {
        MessagesProxy.setLocale(new Locale("en"));
        Assert.assertEquals(1, getConstants().getInt("primitive"));
        Assert.assertEquals(1, getConstants().primitive());
        Assert.assertEquals(1.0, (Object) getConstants().pi());
        Assert.assertEquals(2, getConstants().map().size());

        MessagesProxy.setLocale(new Locale("fi", "FI"));
        Assert.assertEquals(2, getConstants().primitive());
        Assert.assertEquals(3.14, (Object) getConstants().pi());
        Assert.assertEquals(1, getConstants().map().size());
    }
}
