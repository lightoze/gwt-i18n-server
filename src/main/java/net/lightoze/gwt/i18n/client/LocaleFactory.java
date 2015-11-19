package net.lightoze.gwt.i18n.client;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;
import net.lightoze.gwt.i18n.LocaleFactoryProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for obtaining localization objects.
 *
 * @author Vladimir Kulev
 */
public class LocaleFactory {

    public static final String ENCODER = "ENCODER";

    private static final LocaleFactoryProvider provider = new LocaleFactoryProvider();
    private static final Map<Class, Map<String, LocalizableResource>> cache = provider.createClassCache();

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
        Map<String, LocalizableResource> localeCache = getLocaleCache(cls);
        T m = (T) localeCache.get(locale);
        if (m != null) {
            return m;
        }
        synchronized (cache) {
            m = (T) localeCache.get(locale);
            if (m != null) {
                return m;
            }
            m = provider.create(cls, locale);
            put(cls, locale, m);
            return m;
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
        Map<String, LocalizableResource> localeCache = getLocaleCache(cls);
        synchronized (cache) {
            localeCache.put(locale, m);
        }
    }

    private static Map<String, LocalizableResource> getLocaleCache(Class<?> cls) {
        Map<String, LocalizableResource> map = cache.get(cls);
        if (map != null) {
            return map;
        }
        synchronized (cache) {
            map = cache.get(cls);
            if (map != null) {
                return map;
            }
            map = new HashMap<String, LocalizableResource>();
            cache.put(cls, map);
            return map;
        }
    }
}
