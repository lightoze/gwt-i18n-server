package net.lightoze.gwt.i18n;

import com.google.gwt.i18n.client.LocalizableResource;

import java.util.HashMap;
import java.util.Map;

public class LocaleFactoryProvider {

    public Map<Class, Map<String, LocalizableResource>> createClassCache() {
        return new HashMap<Class, Map<String, LocalizableResource>>();
    }

    public <T extends LocalizableResource> T create(Class<T> cls, String locale) {
        throw new UnsupportedOperationException("Dynamic class creation is not supported in GWT client mode");
    }
}
