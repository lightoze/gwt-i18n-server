package net.lightoze.gwt.i18n.client;

import com.google.gwt.i18n.client.LocalizableResource;

import java.util.HashMap;

/**
 * @author Vladimir Kulev
 */
public class LocaleFactory {
    private static LocalizedResourceProvider factory;
    private static HashMap<String, LocalizableResource> cache = new HashMap<String, LocalizableResource>();

    public static void setFactory(LocalizedResourceProvider factory) {
        LocaleFactory.factory = factory;
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends LocalizableResource> T get(Class<T> cls) {
        return get(cls, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <T extends LocalizableResource> T get(Class<T> cls, String locale) {
        String key = cacheKey(cls, locale);
        T m = (T) cache.get(key);
        if (m != null) {
            return m;
        }
        synchronized (LocaleFactory.class) {
            m = (T) cache.get(key);
            if (m != null) {
                return m;
            }
            if (factory != null) {
                m = factory.create(cls, locale);
                put(cls, locale, m);
                return m;
            } else {
                throw new RuntimeException("Messages not found: " + cls);
            }
        }
    }

    public static <T extends LocalizableResource> void put(Class<T> cls, T m) {
        put(cls, null, m);
    }

    public static <T extends LocalizableResource> void put(Class<T> cls, String locale, T m) {
        cache.put(cacheKey(cls, locale), m);
    }

    private static <T extends LocalizableResource> String cacheKey(Class<T> cls, String locale) {
        return cls.getName() + '|' + locale;
    }

    /**
     * The interface for the class that provides the class that implements the passed in LocalizedResource
     */
    public static interface LocalizedResourceProvider {
        /**
         * Create the resource using the Locale provided. If the locale is null, the locale is retrieved from the
         * LocaleProvider in LocaleProxy.
         */
        <T extends LocalizableResource> T create(Class<T> cls, String locale);
    }
}
