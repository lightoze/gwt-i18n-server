package com.teklabs.gwt_i18n_server;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

/**
 * @author Vladimir Kulev
 */
@LocalizableResource.Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface TestMessages extends Messages {
    String simple();

    @PluralText({
            "=0", "Zero apples",
            "one", "An {0} apple"
    })
    String apples(@PluralCount int n);
}
