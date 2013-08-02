package net.lightoze.gwt.i18n.server;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.server.GwtLocaleFactoryImpl;
import com.google.gwt.i18n.server.impl.ReflectionMessage;
import com.google.gwt.i18n.server.impl.ReflectionMessageInterface;
import com.google.gwt.i18n.shared.GwtLocale;
import com.google.gwt.i18n.shared.GwtLocaleFactory;
import net.lightoze.gwt.i18n.client.LocaleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author Vladimir Kulev
 */
public abstract class LocaleProxy implements InvocationHandler {
    private static final GwtLocaleFactory gwtLocaleFactory = new GwtLocaleFactoryImpl();
    private static final Class<LocalizableResource> LOCALIZABLE_RESOURCE = LocalizableResource.class;

    private static LocaleProvider localeProvider;

    /**
     * The locale to use if we are not using a LocaleProvider.
     */
    protected Locale locale;

    static {
        LocaleFactory.setFactory(new LocaleFactory.LocalizedResourceProvider() {
            @Override
            public <T extends LocalizableResource> T create(Class<T> cls, String locale) {
                return LocaleProxy.create(cls, locale == null ? null : new Locale(locale));
            }
        });
        setLocaleProvider(new ThreadLocalLocaleProvider());
    }

    /**
     * Call this before using LocaleFactory on the server.
     */
    public static void initialize() {
        // no op
    }

    /**
     * Sets a locale provider. This can be null. If it is, we assume you are passing in the locale to the factory
     * method.
     *
     * @param localeProvider locale provider
     */
    public static void setLocaleProvider(LocaleProvider localeProvider) {
        LocaleProxy.localeProvider = localeProvider;
    }

    protected Class<? extends LocalizableResource> cls;
    protected Logger log;
    private final ReflectionMessageInterface messageInterface;
    private final Map<Locale, Properties> properties = new HashMap<Locale, Properties>();

    protected LocaleProxy(Class<? extends LocalizableResource> cls, Logger log, Locale locale) {
        this.cls = cls;
        this.log = log;
        this.locale = locale;
        messageInterface = new ReflectionMessageInterface(gwtLocaleFactory, cls);
    }

    protected static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public synchronized static <T extends LocalizableResource> T create(Class<T> cls, Locale locale) {
        InvocationHandler handler;
        boolean isMessages = Messages.class.isAssignableFrom(cls);
        boolean isConstants = Constants.class.isAssignableFrom(cls);
        if (isMessages && isConstants) {
            throw new IllegalArgumentException(cls.getCanonicalName() + " should not extend both Messages and Constants");
        }
        if (isMessages) {
            handler = new MessagesProxy(cls, LoggerFactory.getLogger(cls), locale);
            if (MessagesWithLookup.class.isAssignableFrom(cls)) {
                handler = new MessagesWithLookupProxy(cls, handler);
            }
        } else if (isConstants) {
            handler = new ConstantsProxy(cls, LoggerFactory.getLogger(cls), locale);
            if (ConstantsWithLookup.class.isAssignableFrom(cls)) {
                handler = new ConstantsWithLookupProxy(cls, handler);
            }
        } else {
            throw new IllegalArgumentException("Unknown LocalizableResource type of " + cls.getCanonicalName());
        }
        return cls.cast(Proxy.newProxyInstance(getClassLoader(), new Class<?>[]{cls}, handler));
    }

    public Locale getLocale() {
        if (locale != null) {
            return locale;
        } else {
            return localeProvider.getLocale();
        }
    }

    private static List<String> getLocaleSearchList(Locale locale) {
        GwtLocale gwtLocale = gwtLocaleFactory.fromComponents(locale.getLanguage(), null, locale.getCountry(), locale.getVariant());
        List<String> locales = new ArrayList<String>();
        for (GwtLocale loc : gwtLocale.getCompleteSearchList()) {
            locales.add(loc.isDefault() ? null : loc.toString());
        }
        return locales;
    }

    private static List<Class<?>> getClassSearchList(Class<?> mainClass) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(mainClass);
        for (int i = 0; i < classes.size(); i++) {
            for (Class<?> cls : classes.get(i).getInterfaces()) {
                if (LOCALIZABLE_RESOURCE.isAssignableFrom(cls) && !classes.contains(cls)) {
                    classes.add(cls);
                }
            }
        }
        return classes;
    }

    private Properties loadBundle(Class clazz, String locale) {
        InputStream stream = getClassLoader().getResourceAsStream(clazz.getCanonicalName().replace('.', '/') + (locale == null ? "" : "_" + locale) + ".properties");
        Properties props = new Properties();

        if (stream != null) {
            try {
                props.load(new InputStreamReader(stream, "UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return props;
    }

    private void loadBundles(Locale locale) {
        List<String> locales = getLocaleSearchList(locale);
        List<Class<?>> classes = getClassSearchList(cls);

        Collections.reverse(classes);
        Collections.reverse(locales);

        Properties props = new Properties();
        for (String loc : locales) {
            for (Class clazz : classes) {
                props.putAll(loadBundle(clazz, loc));
            }
        }
        properties.put(locale, props);
    }

    protected Properties getProperties(Locale locale) {
        if (properties.containsKey(locale)) {
            return properties.get(locale);
        }
        synchronized (properties) {
            if (properties.containsKey(locale)) {
                return properties.get(locale);
            }
            loadBundles(locale);
            return properties.get(locale);
        }
    }

    protected Properties getProperties() {
        return getProperties(getLocale());
    }

    protected String getKey(Method method) {
        return new ReflectionMessage(gwtLocaleFactory, messageInterface, method).getKey();
    }
}
