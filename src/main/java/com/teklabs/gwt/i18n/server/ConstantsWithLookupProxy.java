package com.teklabs.gwt.i18n.server;

import com.google.gwt.i18n.client.LocalizableResource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Vladimir Kulev
 */
public class ConstantsWithLookupProxy extends ConstantsProxy {
    private static final Set<String> methods = new HashSet<String>(Arrays.asList("getBoolean", "getDouble", "getFloat", "getInt", "getMap", "getString", "getStringArray"));

    protected ConstantsWithLookupProxy(Class<? extends LocalizableResource> cls) {
        super(cls);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (methods.contains(method.getName()) && args.length == 1) {
            method = cls.getMethod((String) args[0]);
            args = null;
        }
        return super.invoke(proxy, method, args);
    }
}
