package net.lightoze.gwt.i18n.server;

import java.util.Locale;

/**
 * Interface abstracting out how the system can get Locales. This way end users of the API can hook in their own
 * method of retrieving locales and not rely on the default {@link ThreadLocal} implementation.
 *
 * @author David Parish
 */
public interface LocaleProvider {

    /**
     * Gets the locale that is currently being used.
     *
     * @return The Locale being used.
     */
    public Locale getLocale();
}
