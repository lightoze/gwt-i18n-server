package net.lightoze.gwt.i18n.server;

import com.google.gwt.i18n.client.LocalizableResource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * @author Vladimir Kulev
 */
public class MessagesWithLookupProxy implements InvocationHandler {
    private final Method method;
    private final Class<? extends LocalizableResource> cls;
    private final InvocationHandler handler;
    private final Map<String, Method> methods = new HashMap<>();

    protected MessagesWithLookupProxy(Class<? extends LocalizableResource> cls, InvocationHandler handler) {
        this.cls = cls;
        this.handler = handler;
        method = MessagesWithLookup.class.getDeclaredMethods()[0];
        for (Method m : cls.getMethods()) {
            methods.put(m.getName() + "$" + m.getParameterCount(), m);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (this.method.equals(method)) {
            String name = (String) args[0];
            args = (Object[]) args[1];
            method = methods.get(name + "$" + args.length);
            if (method == null) {
                throw new MissingResourceException("Could not find matching method", cls.getCanonicalName(), name);
            }
        }
        return handler.invoke(proxy, method, args);
    }
}
