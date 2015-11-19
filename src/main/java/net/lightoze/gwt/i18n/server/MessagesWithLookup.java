package net.lightoze.gwt.i18n.server;

import com.google.gwt.i18n.client.Messages;

import java.util.MissingResourceException;

/**
 * Like {@link com.google.gwt.i18n.client.ConstantsWithLookup}, but for {@link Messages} interface.
 * <p>
 * <strong>Supported only on server side.</strong>
 *
 * @author Vladimir Kulev
 */
public interface MessagesWithLookup extends Messages {

    /**
     * Look up <code>String</code> by method name.
     * <p>
     * <strong>Supported only on server side.</strong>
     *
     * @param methodName method name
     * @param args       method arguments
     * @return String returned by method
     * @throws java.util.MissingResourceException if methodName is not valid
     */
    String getString(String methodName, Object... args) throws MissingResourceException;
}
