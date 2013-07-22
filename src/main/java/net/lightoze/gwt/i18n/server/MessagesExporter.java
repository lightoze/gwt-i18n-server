package net.lightoze.gwt.i18n.server;

import org.apache.commons.lang.LocaleUtils;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Vladimir Kulev
 */
public class MessagesExporter {
    public static void main(String[] args) throws Exception {
        Class cls = Class.forName(args[0]);
        if (args.length > 1) {
            ThreadLocalLocaleProvider.pushLocale(LocaleUtils.toLocale(args[1]));
        }
        MessagesProxy proxy = new MessagesProxy(cls, LoggerFactory.getLogger(MessagesExporter.class), null);
        for (Method method : cls.getDeclaredMethods()) {
            MessagesProxy.MessageDescriptor descriptor = proxy.getDescriptor(method);
            if (descriptor.defaults.isEmpty()) {
                descriptor.defaults.put("", "");
            }
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : descriptor.defaults.entrySet()) {
                builder.setLength(0);
                String key = descriptor.key;
                if (!entry.getKey().isEmpty()) {
                    key += '[' + entry.getKey() + ']';
                }
                builder.append(key);

                String value = proxy.getProperties().getProperty(key, entry.getValue())
                        .replaceAll("\\\\", "\\\\\\\\")
                        .replaceAll("\\n", "\\\\n")
                        .replaceAll("\\r", "\\\\r")
                        .replaceAll("\\t", "\\\\t");
                builder.append('=').append(value).append('\n');
                System.out.print(builder.toString());
            }
        }
    }
}
