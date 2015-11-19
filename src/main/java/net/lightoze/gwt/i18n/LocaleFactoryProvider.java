package net.lightoze.gwt.i18n;

import com.google.gwt.i18n.client.LocalizableResource;
import net.lightoze.gwt.i18n.server.LocaleProxy;

import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

public class LocaleFactoryProvider {
    public Map<Class, Map<String, LocalizableResource>> createClassCache() {
        return new WeakHashMap<Class, Map<String, LocalizableResource>>();
    }

    /**
     * Create the resource using the Locale provided. If the locale is null, the locale is retrieved from the
     * LocaleProvider in LocaleProxy.
     */
    public <T extends LocalizableResource> T create(Class<T> cls, String locale) {
        Locale l = null;
        if (locale != null) {
            String[] parts = locale.split("_", 3);
            l = new Locale(
                    parts[0],
                    parts.length > 1 ? parts[1] : "",
                    parts.length > 2 ? parts[2] : ""
            );
        }
        return LocaleProxy.create(cls, l);
    }
}
