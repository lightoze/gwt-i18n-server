package com.teklabs.gwt_i18n_server.client;

import com.google.gwt.i18n.client.Messages;

import java.util.HashMap;

/**
 * @author Vladimir Kulev
 */
public class LocaleFactory {
    private static MessagesFactory factory;
    private static HashMap<Class, Messages> messages = new HashMap<Class, Messages>();

    public static void setFactory(MessagesFactory factory) {
        LocaleFactory.factory = factory;
    }

    public static <T extends Messages> T getMessages(Class<T> cls) {
        //noinspection unchecked
        T m = (T) messages.get(cls);
        if (m == null) {
            if (factory != null) {
                m = factory.create(cls);
                putMessages(cls, m);
            } else {
                throw new RuntimeException("Messages not found: " + cls);
            }
        }
        return m;
    }

    public static <T extends Messages> void putMessages(Class<T> cls, T m) {
        messages.put(cls, m);
    }

    public static interface MessagesFactory {
        <T extends Messages> T create(Class<T> cls);
    }
}
