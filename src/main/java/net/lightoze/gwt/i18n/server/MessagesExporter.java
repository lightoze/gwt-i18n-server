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
        StringBuilder builder = new StringBuilder();
        for (Method method : cls.getDeclaredMethods()) {
            MessagesProxy.MessageDescriptor descriptor = proxy.getDescriptor(method);
            if (descriptor.defaults.isEmpty()) {
                descriptor.defaults.put("", "");
            }
            for (Map.Entry<String, String> entry : descriptor.defaults.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    builder.append("# ").append(entry.getValue()).append('\n');
                }

                String key = descriptor.key;
                if (!entry.getKey().isEmpty()) {
                    key += '[' + entry.getKey() + ']';
                }
                builder.append(key);

                String value = proxy.getProperties().getProperty(key);
                if (value == null) {
                    System.err.println("Key not localized: " + key);
                    value = "";
                }
                value = value
                        .replaceAll("\\\\", "\\\\\\\\")
                        .replaceAll("\\n", "\\\\n")
                        .replaceAll("\\r", "\\\\r")
                        .replaceAll("\\t", "\\\\t");
                builder.append('=').append(value).append("\n\n");
            }
        }
        System.out.print(builder.toString());
    }
}
