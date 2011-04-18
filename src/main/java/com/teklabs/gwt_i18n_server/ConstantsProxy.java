package com.teklabs.gwt_i18n_server;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Vladimir Kulev
 */
public class ConstantsProxy extends LocaleProxy {
    private Map<Method, ConstantDescriptor> descriptors = new HashMap<Method, ConstantDescriptor>();

    protected ConstantsProxy(Class<? extends LocalizableResource> cls) {
        super(cls);
    }

    protected static Object cast(String value, Class target) {
        if (target.isAssignableFrom(Boolean.TYPE) || target.isAssignableFrom(Boolean.class)) {
            return Boolean.valueOf(value);
        } else if (target.isAssignableFrom(Double.TYPE) || target.isAssignableFrom(Double.class)) {
            return Double.valueOf(value);
        } else if (target.isAssignableFrom(Float.TYPE) || target.isAssignableFrom(Float.class)) {
            return Float.valueOf(value);
        } else if (target.isAssignableFrom(Integer.TYPE) || target.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(value);
        } else if (target.isArray() && target.getComponentType().isAssignableFrom(String.class)) {
            return value.split("\\s*(?<!\\\\),\\s*");
        } else if (target.isAssignableFrom(Map.class)) {
            String[] values = value.split("\\s*(?<!\\\\),\\s*");
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; (i + 1) < values.length; i += 2) {
                map.put(values[i], values[i + 1]);
            }
            return map;
        } else {
            return value;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args != null) {
            throw new IllegalArgumentException();
        }
        ConstantDescriptor desc = getDescriptor(method);
        ResourceBundle bundle = getBundle();
        if (bundle.containsKey(desc.key)) {
            return cast(bundle.getString(desc.key), method.getReturnType());
        } else {
            if (desc.defaultValue == null) {
                log.error(String.format("Unlocalized key '%s' for locale '%s'", desc.key, getLocale()));
            }
            return desc.defaultValue;
        }
    }

    private synchronized ConstantDescriptor getDescriptor(Method method) {
        ConstantDescriptor desc = descriptors.get(method);
        if (desc == null) {
            desc = new ConstantDescriptor();
            desc.key = getKey(method, null);
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
