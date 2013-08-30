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
import org.apache.commons.lang.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vladimir Kulev
 */
public abstract class LocaleProxy implements InvocationHandler {
    protected static final Locale ENCODER = new Locale("ENCODER");
    private static final Pattern encodedPattern = Pattern.compile("\\{([\\w\\.]+)#(\\w+)((\\?\\w=[^{}?=]*)*)\\}");
    private static final Pattern paramPattern = Pattern.compile("\\?(\\w)=([^?=]*)");
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
                Locale l = null;
                if (locale != null) {
                    String[] parts = locale.split("_", 3);
                    l = new Locale(
                            parts[0],
                            parts.length > 1 ? parts[1] : "",
                            parts.length > 2 ? parts[2] : ""
                    );
                }
                return LocaleProxy.create(cls, l);
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

    public static LocaleProvider getLocaleProvider() {
        return localeProvider;
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
            if (ENCODER.equals(locale)) {
                handler = new MessagesEncoderProxy(cls, LoggerFactory.getLogger(cls), locale);
            } else {
                handler = new MessagesProxy(cls, LoggerFactory.getLogger(cls), locale);
            }
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

    protected Locale getCurrentLocale() {
        if (locale != null) {
            return locale;
        } else {
            return getLocale();
        }
    }

    public static Locale getLocale() {
        Locale l = localeProvider.getLocale();
        return l == null ? Locale.ROOT : l;
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
        return getProperties(getCurrentLocale());
    }

    protected String getKey(Method method) {
        return new ReflectionMessage(gwtLocaleFactory, messageInterface, method).getKey();
    }

    public static String decode(String str) {
        return decode(str, getLocale());
    }

    public static String decode(String str, Locale locale) {
        StringBuffer buf = new StringBuffer(str.length());
        Matcher m = encodedPattern.matcher(str);
        while (m.find()) {
            String className = m.group(1);
            String method = m.group(2);
            Matcher p = paramPattern.matcher(m.group(3));
            LinkedList<Object> args = new LinkedList<Object>();
            while (p.find()) {
                char type = p.group(1).charAt(0);
                String s;
                try {
                    s = URLDecoder.decode(p.group(2), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                Object arg;
                switch (type) {
                    case 's':
                        arg = s;
                        break;
                    case 'i':
                        arg = Integer.parseInt(s);
                        break;
                    case 'd':
                        arg = Double.parseDouble(s);
                        break;
                    case 't':
                        arg = new Date(Long.parseLong(s));
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                args.add(arg);
            }
            try {
                Class<? extends Messages> cls = (Class<? extends Messages>) getClassLoader().loadClass(className);
                Object obj = LocaleFactory.get(cls, locale.toString());
                String s = (String) MethodUtils.invokeMethod(obj, method, args.toArray());
                m.appendReplacement(buf, Matcher.quoteReplacement(s));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        m.appendTail(buf);
        return buf.toString();
    }
}
