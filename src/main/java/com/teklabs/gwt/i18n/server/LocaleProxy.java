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
import java.util.*;

/**
 * @author Vladimir Kulev
 */
public abstract class LocaleProxy implements InvocationHandler {
    private static final ThreadLocal<Locale> locale = new ThreadLocal<Locale>();
    private static final GwtLocaleFactory gwtLocaleFactory = new GwtLocaleFactoryImpl();
    private static final Class<LocalizableResource> LOCALIZABLE_RESOURCE = LocalizableResource.class;

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

    private List<Locale> getLocaleSearchList(Locale locale) {
        List<Locale> locales = new ArrayList<Locale>();
        locales.add(locale);

        while (locale != null) {
            locale = getParentLocale(locale);
            locales.add(locale);
        }

        return locales;
    }

    private Properties loadBundle(Class clazz, Locale locale) {
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
        List<Locale> locales = getLocaleSearchList(locale);

        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(cls);
        for (int i = 0; i < classes.size(); i++) {
            for (Class<?> cls : classes.get(i).getInterfaces()) {
                if (LOCALIZABLE_RESOURCE.isAssignableFrom(cls) && !classes.contains(cls)) {
                    classes.add(cls);
                }
            }
        }

        Collections.reverse(classes);
        Collections.reverse(locales);

        for (Locale loc : locales) {
            for (Class clazz : classes) {
                Properties props = loadBundle(clazz, loc);
                if (properties.containsKey(locale)) {
                    properties.get(locale).putAll(props);
                } else {
                    properties.put(locale, props);
                }
            }
        }
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

    public static class LocaleProxyProvider implements LocaleFactory.LocaleProvider {
        @Override
        public <T extends LocalizableResource> T create(Class<T> cls) {
            return LocaleProxy.create(cls);
        }
    }
}
