package com.teklabs.gwt.i18n.server;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

import java.util.Date;

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

    @DefaultMessage("One {0} selected")
    @AlternateMessage({
            "one|select two", "Two selected",
            "one|other", "Not two selected",
            "other|select three", "Three selected",
            "=4|select four", "Four selected"
    })
    String select(@PluralCount @Offset(1) int n, @Select String select);

    @DefaultMessage("Today is {0,date,long}")
    String today(Date today);

    String inheritance1();

    String inheritance2();

    String inheritance3();

}
