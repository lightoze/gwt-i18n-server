package com.teklabs.gwt.i18n.server;

import java.util.Locale;

/**
 * @author David Parish
 */
public class ThreadLocalLocaleProvider implements LocaleProvider {
    private static final ThreadLocal<Locale> locale = new ThreadLocal<Locale>();

    @Override
    public Locale getLocale() {
        return locale.get() == null ? Locale.ROOT : locale.get();
    }

    public static void setLocale(Locale l) {
        locale.set(l);
    }

    public static void clear() {
        locale.remove();
    }
}
