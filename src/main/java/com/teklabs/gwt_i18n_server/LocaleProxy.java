package com.teklabs.gwt_i18n_server;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.rebind.keygen.KeyGenerator;
import com.google.gwt.i18n.rebind.keygen.MethodNameKeyGenerator;
import com.teklabs.gwt_i18n_server.client.LocaleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Vladimir Kulev
 */
public abstract class LocaleProxy implements InvocationHandler {
    private static ThreadLocal<Locale> locale = new ThreadLocal<Locale>();

    static {
        LocaleFactory.setFactory(new LocaleProxyProvider());
    }

    public static void initialize() {
        // call this before using LocaleFactory
    }

    protected Class<? extends LocalizableResource> cls;
    protected Logger log;
    private KeyGenerator generator;
    private Map<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();

    protected LocaleProxy(Class<? extends LocalizableResource> cls) {
        this.cls = cls;
        try {
            LocalizableResource.GenerateKeys annotation = cls.getAnnotation(LocalizableResource.GenerateKeys.class);
            if (annotation != null) {
                generator = (KeyGenerator) Class.forName(annotation.value()).newInstance();
            } else {
                generator = new MethodNameKeyGenerator();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public synchronized static <T extends LocalizableResource> T create(Class<T> cls) {
        LocaleProxy handler;
        if (Messages.class.isAssignableFrom(cls)) {
            handler = new MessagesProxy(cls);
        } else if(ConstantsWithLookup.class.isAssignableFrom(cls)) {
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

    protected synchronized ResourceBundle getBundle() {
        ResourceBundle bundle = bundles.get(getLocale());
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(cls.getCanonicalName(), getLocale(), getClassLoader());
            bundles.put(getLocale(), bundle);
        }
        return bundle;
    }

    protected String getKey(Method method, String text) {
        String meaning = null;
        {
            LocalizableResource.Meaning annotation = method.getAnnotation(LocalizableResource.Meaning.class);
            if (annotation != null) {
                meaning = annotation.value();
            }
        }
        {
            LocalizableResource.Key annotation = method.getAnnotation(LocalizableResource.Key.class);
            if (annotation != null) {
                return annotation.value();
            } else {
                return generator.generateKey(cls.getCanonicalName(), method.getName(), text, meaning);
            }
        }
    }

    public static class LocaleProxyProvider implements LocaleFactory.LocaleProvider {
        @Override
        public <T extends LocalizableResource> T create(Class<T> cls) {
            return LocaleProxy.create(cls);
        }
    }
}
