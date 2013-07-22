package net.lightoze.gwt.i18n.server;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;

/**
 * @author David Parish
 */
public class ThreadLocalLocaleProvider implements LocaleProvider {
    private static final ThreadLocal<Deque<Locale>> locale = new ThreadLocal<Deque<Locale>>() {
        @Override
        protected Deque<Locale> initialValue() {
            return new LinkedList<Locale>();
        }
    };

    @Override
    public Locale getLocale() {
        Locale l = locale.get().peek();
        return l == null ? Locale.ROOT : l;
    }

    public static void pushLocale(Locale l) {
        locale.get().push(l);
    }

    public static void popLocale() {
        locale.get().pop();
    }
}
