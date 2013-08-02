package net.lightoze.gwt.i18n.server;

import com.google.gwt.i18n.client.LocalizableResource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.MissingResourceException;

/**
 * @author Vladimir Kulev
 */
public class MessagesWithLookupProxy implements InvocationHandler {
    private final Method method;
    private final Class<? extends LocalizableResource> cls;
    private final InvocationHandler handler;

    protected MessagesWithLookupProxy(Class<? extends LocalizableResource> cls, InvocationHandler handler) {
        this.cls = cls;
        this.handler = handler;
        method = MessagesWithLookup.class.getDeclaredMethods()[0];
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (this.method.equals(method)) {
            try {
                method = cls.getMethod((String) args[0]);
            } catch (NoSuchMethodException e) {
                throw new MissingResourceException(e.getMessage(), cls.getCanonicalName(), method.getName());
            }
            args = null;
        }
        return handler.invoke(proxy, method, args);
    }
}
