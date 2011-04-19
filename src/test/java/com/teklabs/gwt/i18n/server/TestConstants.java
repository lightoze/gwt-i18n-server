package com.teklabs.gwt.i18n.server;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.ConstantsWithLookup;

import java.util.Map;

/**
 * @author Vladimir Kulev
 */
public interface TestConstants extends ConstantsWithLookup {
    @DefaultIntValue(1)
    int primitive();

    @DefaultDoubleValue(1)
    Double pi();

    @Constants.DefaultStringMapValue({
            "a", "b"
    })
    Map<String, String> map();
}
