package net.lightoze.gwt.i18n.server;

import com.google.gwt.i18n.client.LocalizableResource;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;

/**
 * @author Vladimir Kulev
 */
public class MessagesEncoderProxy extends MessagesProxy {
    protected MessagesEncoderProxy(Class<? extends LocalizableResource> cls, Logger log, Locale locale) {
        super(cls, log, locale);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        StringBuilder builder = new StringBuilder();
        builder.append('{')
                .append(method.getDeclaringClass().getName())
                .append('#')
                .append(method.getName());
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            Class<?> argType = ClassUtils.primitiveToWrapper(method.getParameterTypes()[i]);
            Object arg = args[i];
            String value = String.valueOf(arg);
            builder.append('?');
            if (Integer.class.isAssignableFrom(argType)) {
                builder.append('i');
            } else if (Double.class.isAssignableFrom(argType)) {
                builder.append('d');
            } else if (Date.class.isAssignableFrom(argType)) {
                builder.append('t');
                if (arg != null) {
                    value = Long.toString(((Date) arg).getTime());
                }
            } else {
                builder.append('s');
            }
            builder.append('=').append(URLEncoder.encode(value, "UTF-8"));
        }

        return builder.append('}').toString();
    }
}
