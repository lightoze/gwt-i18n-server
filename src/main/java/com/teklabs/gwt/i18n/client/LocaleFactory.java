package com.teklabs.gwt.i18n.client;

import com.google.gwt.i18n.client.LocalizableResource;
import com.teklabs.gwt.i18n.server.LocaleProxy;

import java.util.HashMap;

/**
 * @author Vladimir Kulev
 */
public class LocaleFactory {
    private static LocaleProvider factory;
    private static HashMap<Class, LocalizableResource> cache = new HashMap<Class, LocalizableResource>();

    public static void setFactory(LocaleProvider factory) {
        LocaleFactory.factory = factory;
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends LocalizableResource> T get(Class<T> cls) {
        T m = (T) cache.get(cls);
        if (m != null) {
            return m;
        }
        synchronized (LocaleFactory.class) {
            m = (T) cache.get(cls);
            if (m != null) {
                return m;
            }

            if (factory == null) {
                //uses default factory if dev do not pick one
                LocaleProxy.initialize();
            }
            m = factory.create(cls);
            put(cls, m);
            return m;
        }
    }

    public static <T extends LocalizableResource> void put(Class<T> cls, T m) {
        cache.put(cls, m);
    }

    public static interface LocaleProvider {
        <T extends LocalizableResource> T create(Class<T> cls);
    }
}
