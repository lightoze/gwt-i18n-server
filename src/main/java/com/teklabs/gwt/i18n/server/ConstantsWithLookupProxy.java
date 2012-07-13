package com.teklabs.gwt.i18n.server;

import com.google.gwt.i18n.client.LocalizableResource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Vladimir Kulev
 */
public class ConstantsWithLookupProxy implements InvocationHandler {
    private static final Set<String> methods = new HashSet<String>(Arrays.asList("getBoolean", "getDouble", "getFloat", "getInt", "getMap", "getString", "getStringArray"));
    private final Class<? extends LocalizableResource> cls;
    private final InvocationHandler handler;

    protected ConstantsWithLookupProxy(Class<? extends LocalizableResource> cls, InvocationHandler handler) {
        this.cls = cls;
        this.handler = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (methods.contains(method.getName()) && args.length == 1) {
            method = cls.getMethod((String) args[0]);
            args = null;
        }
        return handler.invoke(proxy, method, args);
    }
}
