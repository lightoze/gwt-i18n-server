package com.teklabs.gwt_i18n_server;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.PluralRule;
import com.google.gwt.i18n.client.impl.plurals.DefaultRule;
import com.google.gwt.i18n.rebind.keygen.KeyGenerator;
import com.google.gwt.i18n.rebind.keygen.MethodNameKeyGenerator;
import com.teklabs.gwt_i18n_server.client.LocaleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.regex.Matcher;

/**
 * @author Vladimir Kulev
 */
public class MessagesProxy implements InvocationHandler {
    private static ThreadLocal<Locale> locale = new ThreadLocal<Locale>();

    static {
        LocaleFactory.setFactory(new MessagesFactory());
    }

    public static void initialize() {
        // call this before using LocaleFactory
    }

    private Class<? extends Messages> cls;
    private Logger log;
    private KeyGenerator generator;
    private Map<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();
    private Map<Object, PluralRule> rules = new HashMap<Object, PluralRule>();
    private Map<Method, MessageDescriptor> descriptors = new HashMap<Method, MessageDescriptor>();

    private MessagesProxy() {
    }

    private static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public synchronized static <T extends Messages> T create(Class<T> cls) {
        try {
            MessagesProxy handler = new MessagesProxy();
            handler.cls = cls;
            handler.log = LoggerFactory.getLogger(cls);
            {
                LocalizableResource.GenerateKeys annotation = cls.getAnnotation(LocalizableResource.GenerateKeys.class);
                if (annotation != null) {
                    handler.generator = (KeyGenerator) Class.forName(annotation.value()).newInstance();
                } else {
                    handler.generator = new MethodNameKeyGenerator();
                }
            }
            //noinspection unchecked
            return (T) Proxy.newProxyInstance(getClassLoader(), new Class<?>[]{cls}, handler);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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

    private synchronized ResourceBundle getBundle() {
        ResourceBundle bundle = bundles.get(getLocale());
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(cls.getCanonicalName(), getLocale(), getClassLoader());
            bundles.put(getLocale(), bundle);
        }
        return bundle;
    }

    private synchronized PluralRule getRule(Class<? extends PluralRule> cls) {
        try {
        if (cls.isAssignableFrom(PluralRule.class)) {
            PluralRule rule = rules.get(getLocale());
            if (rule == null) {
                rule = (PluralRule) getClassLoader().loadClass(
                        DefaultRule.class.getCanonicalName() + '_' + getLocale().getLanguage()
                ).newInstance();
                rules.put(getLocale(), rule);
            }
            return rule;
        } else {
            PluralRule rule = rules.get(cls);
            if (rule == null) {
                rule = cls.newInstance();
                rules.put(cls, rule);
            }
            return rule;
        }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MessageDescriptor desc = getDescriptor(method);
        String key = desc.key;
        String message = desc.defaultMessage;
        if (desc.pluralRule != null) {
            PluralRule rule = getRule(desc.pluralRule);
            int n = ((Number) args[desc.pluralArgIndex]).intValue();
            String form;
            if (desc.defaultPlural.containsKey("=" + n)) {
                // we have special plural form
                form = "=" + n;
                n -= desc.pluralOffset;
            } else {
                n -= desc.pluralOffset;
                form = rule.pluralForms()[rule.select(n)].getName();
            }
            if (!form.equals("other")) {
                key += '[' + form + ']';
                if (desc.defaultPlural.containsKey(form)) {
                    message = desc.defaultPlural.get(form);
                }
            }
        }
        try {
            message = getBundle().getString(key);
        } catch (MissingResourceException e) {
            log.error(String.format("Unlocalized key '%s' for locale '%s'", key, getLocale()));
        }
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String value = args[i] == null ? "null" : args[i].toString();
                message = message.replaceAll("\\{" + i + "(\\,[^\\}]+)?\\}", value == null ? "" : Matcher.quoteReplacement(value));
            }
        }
        return message;
    }

    private synchronized MessageDescriptor getDescriptor(Method method) {
        MessageDescriptor desc = descriptors.get(method);
        if (desc == null) {
            desc = new MessageDescriptor();
            {
                Messages.DefaultMessage annotation = method.getAnnotation(Messages.DefaultMessage.class);
                if (annotation != null) {
                    desc.defaultMessage = annotation.value();
                }
            }
            {
                LocalizableResource.Meaning annotation = method.getAnnotation(LocalizableResource.Meaning.class);
                if (annotation != null) {
                    desc.meaning = annotation.value();
                }
            }
            {
                LocalizableResource.Key annotation = method.getAnnotation(LocalizableResource.Key.class);
                if (annotation != null) {
                    desc.key = annotation.value();
                } else {
                    desc.key = generator.generateKey(cls.getCanonicalName(), method.getName(), desc.defaultMessage, desc.meaning);
                }
            }
            {
                Messages.PluralText annotation = method.getAnnotation(Messages.PluralText.class);
                if (annotation != null) {
                    String[] pairs = annotation.value();
                    for (int i = 0; (i + 1) < pairs.length; i += 2) {
                        desc.defaultPlural.put(pairs[i], pairs[i + 1]);
                    }
                }
            }
            {
                Messages.Offset annotation = method.getAnnotation(Messages.Offset.class);
                if (annotation != null) {
                    desc.pluralOffset = annotation.value();
                }
            }
            for (int i = 0; i < method.getParameterAnnotations().length; i++) {
                for (Annotation annotation : method.getParameterAnnotations()[i]) {
                    if (annotation instanceof Messages.PluralCount) {
                        desc.pluralRule = ((Messages.PluralCount) annotation).value();
                        desc.pluralArgIndex = i;
                    }
                }
            }
            descriptors.put(method, desc);
        }
        return desc;
    }

    private class MessageDescriptor {
        String key;
        String meaning;
        String defaultMessage;
        Map<String, String> defaultPlural = new HashMap<String, String>();
        Class<? extends PluralRule> pluralRule;
        int pluralArgIndex;
        int pluralOffset;
    }

    public static class MessagesFactory implements LocaleFactory.MessagesFactory {
        @Override
        public <T extends Messages> T create(Class<T> cls) {
            return MessagesProxy.create(cls);
        }
    }
}
