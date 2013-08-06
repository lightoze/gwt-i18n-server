package net.lightoze.gwt.i18n.client;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

import java.util.HashMap;

/**
 * Factory class for obtaining localization objects.
 *
 * @author Vladimir Kulev
 */
public class LocaleFactory {
    public static final String ENCODER = "ENCODER";

    private static LocalizedResourceProvider factory;
    private static HashMap<String, LocalizableResource> cache = new HashMap<String, LocalizableResource>();

    public static void setFactory(LocalizedResourceProvider factory) {
        LocaleFactory.factory = factory;
    }

    /**
     * Get localization object for <em>current</em> locale.
     * <p/>
     * On server side <em>current</em> locale is determined dynamically by {@link net.lightoze.gwt.i18n.server.LocaleProvider} and can change in runtime for the same object.
     *
     * @param cls localization interface class
     * @param <T> localization interface class
     * @return object implementing specified class
     */
    public static <T extends LocalizableResource> T get(Class<T> cls) {
        return get(cls, null);
    }

    /**
     * Get <em>encoding</em> localization object, which will encode all requests so that they can be decoded later by {@link net.lightoze.gwt.i18n.server.LocaleProxy#decode}.
     * <p/>
     * The purpose is to separate complex (e.g. template-based) text generation and its localization for particular locale into two separate phases.
     * <p/>
     * <strong>Supported only on server side.</strong>
     *
     * @param cls localization interface class
     * @param <T> localization interface class
     * @return object implementing specified class
     */
    public static <T extends Messages> T getEncoder(Class<T> cls) {
        return get(cls, ENCODER);
    }

    /**
     * Get localization object for the specified locale.
     *
     * @param cls    localization interface class
     * @param locale locale string
     * @param <T>    localization interface class
     * @return object implementing specified class
     */
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

    /**
     * Populate localization object cache for <em>current</em> locale.
     *
     * @param cls localization interface class
     * @param m   localization object
     * @param <T> localization interface class
     */
    public static <T extends LocalizableResource> void put(Class<T> cls, T m) {
        put(cls, null, m);
    }

    /**
     * Populate localization object cache for the specified locale.
     *
     * @param cls    localization interface class
     * @param locale locale string
     * @param m      localization object
     * @param <T>    localization interface class
     */
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
