package com.teklabs.gwt.i18n.server;

import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Vladimir Kulev
 */
public class MessagesExporter {
    public static void main(String[] args) throws Exception {
        Class cls = Class.forName(args[0]);
        MessagesProxy proxy = new MessagesProxy(cls, LoggerFactory.getLogger(MessagesExporter.class));
        for (Method method : cls.getDeclaredMethods()) {
            MessagesProxy.MessageDescriptor descriptor = proxy.getDescriptor(method);
            if (descriptor.defaults.isEmpty()) {
                descriptor.defaults.put("", "");
            }
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, String> entry : descriptor.defaults.entrySet()) {
                builder.setLength(0);
                builder.append(descriptor.key);
                if (!entry.getKey().isEmpty()) {
                    builder.append('[').append(entry.getKey()).append(']');
                }
                String value = entry.getValue()
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
