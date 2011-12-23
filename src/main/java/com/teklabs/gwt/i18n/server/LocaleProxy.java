package com.teklabs.gwt.i18n.server;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.server.GwtLocaleFactoryImpl;
import com.google.gwt.i18n.server.impl.ReflectionMessage;
import com.google.gwt.i18n.server.impl.ReflectionMessageInterface;
import com.google.gwt.i18n.shared.GwtLocaleFactory;
import com.teklabs.gwt.i18n.client.LocaleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * @author Vladimir Kulev
 */
public abstract class LocaleProxy implements InvocationHandler {
    private static ThreadLocal<Locale> locale = new ThreadLocal<Locale>();
    private static GwtLocaleFactory gwtLocaleFactory = new GwtLocaleFactoryImpl();

    static {
        LocaleFactory.setFactory(new LocaleProxyProvider());
    }

    public static void initialize() {
        // call this before using LocaleFactory
    }

    protected Class<? extends LocalizableResource> cls;
    protected Logger log;
    private final ReflectionMessageInterface messageInterface;
    private final Map<Locale, Properties> properties = new HashMap<Locale, Properties>();

    protected LocaleProxy(Class<? extends LocalizableResource> cls) {
        this.cls = cls;
        messageInterface = new ReflectionMessageInterface(gwtLocaleFactory, cls);
    }

    protected static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public synchronized static <T extends LocalizableResource> T create(Class<T> cls) {
        LocaleProxy handler;
        if (Messages.class.isAssignableFrom(cls)) {
            handler = new MessagesProxy(cls);
        } else if (ConstantsWithLookup.class.isAssignableFrom(cls)) {
            handler = new ConstantsWithLookupProxy(cls);
        } else if (Constants.class.isAssignableFrom(cls)) {
            handler = new ConstantsProxy(cls);
        } else {
            throw new IllegalArgumentException("Unknown LocalizableResource type of " + cls.getCanonicalName());
        }
        handler.log = LoggerFactory.getLogger(cls);
        return cls.cast(Proxy.newProxyInstance(getClassLoader(), new Class<?>[]{cls}, handler));
    }

    public static Locale getLocale() {
        return locale.get() == null ? Locale.ROOT : locale.get();
    }

    public static void setLocale(Locale l) {
        locale.set(l);
    }

    public static void clear() {
        locale.remove();
    }

    private static Locale getParentLocale(Locale locale) {
        if (!locale.getVariant().isEmpty()) {
            return new Locale(locale.getLanguage(), locale.getCountry());
        } else if (!locale.getCountry().isEmpty()) {
            return new Locale(locale.getLanguage());
        } else {
            return null;
        }
    }

    private void loadBundle(Locale locale) {
        InputStream stream = getClassLoader().getResourceAsStream(cls.getCanonicalName().replace('.', '/') + (locale == null ? "" : "_" + locale.toString()) + ".properties");
        Properties props;
        if (stream != null) {
            try {
                props = new Properties(locale == null ? null : getProperties(getParentLocale(locale)));
                props.load(new InputStreamReader(stream, "UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (locale != null) {
                props = getProperties(getParentLocale(locale));
            } else {
                props = new Properties();
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
            loadBundle(locale);
            return properties.get(locale);
        }
    }

    protected Properties getProperties() {
        return getProperties(getLocale());
    }

    protected String getKey(Method method) {
        return new ReflectionMessage(gwtLocaleFactory, messageInterface, method).getKey();
    }

    public static class LocaleProxyProvider implements LocaleFactory.LocaleProvider {
        @Override
        public <T extends LocalizableResource> T create(Class<T> cls) {
            return LocaleProxy.create(cls);
        }
    }
}
