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

    private static List<String> expand(List<String> list, String... variants) {
        if (list.isEmpty()) {
            return new ArrayList<String>(Arrays.asList(variants));
        }
        List<String> result = new ArrayList<String>(list.size() * variants.length);
        for (String str : list) {
            for (String variant : variants) {
                result.add(str + '|' + variant);
            }
        }
        return result;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MessageDescriptor desc = getDescriptor(method);
        List<String> forms = new ArrayList<String>();
        for (int i = 0; i < desc.args.length; i++) {
            MessageArgument arg = desc.args[i];
            if (arg.pluralCount) {
                PluralRule rule = getRule(arg.pluralRule);
                int n = ((Number) args[i]).intValue();
                forms = expand(forms, "=" + n, rule.pluralForms()[rule.select(n - arg.pluralOffset)].getName());
                args[i] = n - arg.pluralOffset;
            }
            if (arg.select) {
                forms = expand(forms, String.valueOf(args[i]), "other");
            }
        }
        String message = null;
        forms.add("");
        ResourceBundle bundle = getBundle();
        for (String form : forms) {
            if (desc.defaults.containsKey(form)) {
                message = desc.defaults.get(form);
            }
            String key = desc.key;
            if (form.length() > 0) {
                key += '[' + form + ']';
            }
            if (bundle.containsKey(key)) {
                message = bundle.getString(key);
            }
            if (message != null) {
                break;
            }
        }
        if (message == null) {
            log.error(String.format("Unlocalized key '%s(%s)' for locale '%s'", desc.key, forms.get(0), getLocale()));
            message = '@' + desc.key;
        }
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                message = message.replaceAll("\\{" + i + "(\\,[^\\}]+)?\\}", Matcher.quoteReplacement(String.valueOf(args[i])));
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
                    desc.defaults.put("", annotation.value());
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
                    desc.key = generator.generateKey(cls.getCanonicalName(), method.getName(), desc.defaults.get(""), desc.meaning);
                }
            }
            {
                String[] defaults = null;
                {
                    Messages.PluralText annotation = method.getAnnotation(Messages.PluralText.class);
                    if (annotation != null) {
                        defaults = annotation.value();
                    }
                }
                {
                    Messages.AlternateMessage annotation = method.getAnnotation(Messages.AlternateMessage.class);
                    if (annotation != null) {
                        defaults = annotation.value();
                    }
                }
                if (defaults != null) {
                    for (int i = 0; (i + 1) < defaults.length; i += 2) {
                        desc.defaults.put(defaults[i], defaults[i + 1]);
                    }
                }
            }
            {
                Annotation[][] args = method.getParameterAnnotations();
                desc.args = new MessageArgument[args.length];
                for (int i = 0; i < args.length; i++) {
                    desc.args[i] = new MessageArgument();
                    for (Annotation annotation : args[i]) {
                        if (annotation instanceof Messages.PluralCount) {
                            desc.args[i].pluralCount = true;
                            desc.args[i].pluralRule = ((Messages.PluralCount) annotation).value();
                        }
                        if (annotation instanceof Messages.Offset) {
                            desc.args[i].pluralOffset = ((Messages.Offset) annotation).value();
                        }
                        if (annotation instanceof Messages.Select) {
                            desc.args[i].select = true;
                        }
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
        Map<String, String> defaults = new HashMap<String, String>();
        MessageArgument[] args;
    }

    private class MessageArgument {
        boolean pluralCount;
        int pluralOffset;
        Class<? extends PluralRule> pluralRule;
        boolean select;
    }

    public static class MessagesFactory implements LocaleFactory.MessagesFactory {
        @Override
        public <T extends Messages> T create(Class<T> cls) {
            return MessagesProxy.create(cls);
        }
    }
}
