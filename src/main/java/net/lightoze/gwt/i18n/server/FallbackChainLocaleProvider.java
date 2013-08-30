package net.lightoze.gwt.i18n.server;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author Vladimir Kulev
 */
public class FallbackChainLocaleProvider implements LocaleProvider {
    private LinkedList<LocaleProvider> chain = new LinkedList<LocaleProvider>();

    public FallbackChainLocaleProvider(List<LocaleProvider> chain) {
        this.chain.addAll(chain);
    }

    public void addLast(LocaleProvider provider) {
        chain.addLast(provider);
    }

    public void addFirst(LocaleProvider provider) {
        chain.addFirst(provider);
    }

    public void remove(LocaleProvider provider) {
        chain.remove(provider);
    }

    public List<LocaleProvider> getChain() {
        return Collections.unmodifiableList(chain);
    }

    @Override
    public Locale getLocale() {
        for (LocaleProvider provider : chain) {
            Locale locale = provider.getLocale();
            if (locale != null) {
                return locale;
            }
        }
        return null;
    }
}
