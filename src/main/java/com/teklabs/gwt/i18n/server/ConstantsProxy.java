package com.teklabs.gwt.i18n.server;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author Vladimir Kulev
 */
public class ConstantsProxy extends LocaleProxy {
    private Map<Method, ConstantDescriptor> descriptors = new HashMap<Method, ConstantDescriptor>();

    protected ConstantsProxy(Class<? extends LocalizableResource> cls, Logger log, Locale locale) {
        super(cls, log, locale);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T cast(String value, Class<T> target) {
        if (target.isAssignableFrom(Boolean.TYPE) || target.isAssignableFrom(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        } else if (target.isAssignableFrom(Double.TYPE) || target.isAssignableFrom(Double.class)) {
            return (T) Double.valueOf(value);
        } else if (target.isAssignableFrom(Float.TYPE) || target.isAssignableFrom(Float.class)) {
            return (T) Float.valueOf(value);
        } else if (target.isAssignableFrom(Integer.TYPE) || target.isAssignableFrom(Integer.class)) {
            return (T) Integer.valueOf(value);
        } else if (target.isArray() && target.getComponentType().isAssignableFrom(String.class)) {
            return (T) value.split("\\s*(?<!\\\\),\\s*");
        } else {
            return (T) value;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ConstantDescriptor desc = getDescriptor(method);
        Properties properties = getProperties();

        Object returnValue;
        if (method.getReturnType().isAssignableFrom(Map.class)) {
            Collection<String> keys;
            Map<String, String> defaultMap = (Map<String, String>) desc.defaultValue;
            if (properties.containsKey(desc.key)) {
                keys = Arrays.asList(cast(properties.getProperty(desc.key), String[].class));
            } else if (defaultMap != null) {
                keys = defaultMap.keySet();
            } else {
                logMissingKey(desc.key);
                return null;
            }
            Map<String, String> map = new HashMap<String, String>(keys.size());
            for (String key : keys) {
                if (properties.containsKey(key)) {
                    map.put(key, properties.getProperty(key));
                } else {
                    String value = defaultMap != null ? defaultMap.get(key) : null;
                    if (value == null) {
                        logMissingKey(key);
                    }
                    map.put(key, value);
                }
            }
            return map;
        }
        if (properties.containsKey(desc.key)) {
            returnValue = cast(properties.getProperty(desc.key), method.getReturnType());
        } else {
            if (desc.defaultValue == null) {
                logMissingKey(desc.key);
            }
            returnValue = desc.defaultValue;
        }

        if (returnValue instanceof String && args != null) {
            return MessageFormat.format(returnValue.toString(), args);
        }

        if (args != null) {
            throw new IllegalArgumentException();
        }

        return returnValue;
    }

    private void logMissingKey(String key) {
        log.error(String.format("Unlocalized key '%s' for locale '%s'", key, getLocale()));
    }

    private synchronized ConstantDescriptor getDescriptor(Method method) {
        ConstantDescriptor desc = descriptors.get(method);
        if (desc == null) {
            desc = new ConstantDescriptor();
            desc.key = getKey(method);
            {
                Constants.DefaultBooleanValue annotation = method.getAnnotation(Constants.DefaultBooleanValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultDoubleValue annotation = method.getAnnotation(Constants.DefaultDoubleValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultFloatValue annotation = method.getAnnotation(Constants.DefaultFloatValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultIntValue annotation = method.getAnnotation(Constants.DefaultIntValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultStringArrayValue annotation = method.getAnnotation(Constants.DefaultStringArrayValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            {
                Constants.DefaultStringMapValue annotation = method.getAnnotation(Constants.DefaultStringMapValue.class);
                if (annotation != null) {
                    String[] values = annotation.value();
                    Map<String, String> map = new HashMap<String, String>();
                    for (int i = 0; (i + 1) < values.length; i += 2) {
                        map.put(values[i], values[i + 1]);
                    }
                    desc.defaultValue = map;
                }
            }
            {
                Constants.DefaultStringValue annotation = method.getAnnotation(Constants.DefaultStringValue.class);
                if (annotation != null) {
                    desc.defaultValue = annotation.value();
                }
            }
            descriptors.put(method, desc);
        }
        return desc;
    }

    private class ConstantDescriptor {
        String key;
        Object defaultValue;
    }
}
