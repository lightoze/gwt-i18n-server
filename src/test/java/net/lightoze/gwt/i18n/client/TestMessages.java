package net.lightoze.gwt.i18n.client;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

import java.util.Date;

/**
 * @author Vladimir Kulev
 */
@LocalizableResource.Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface TestMessages extends Messages {
    @DefaultMessage("simple")
    String simple();

    @PluralText({
            "=0", "Zero apples",
            "one", "An {0} apple"
    })
    String apples(@PluralCount int n);

    @DefaultMessage("One {0} selected")
    @AlternateMessage({
            "one|TWO", "Two selected",
            "one|other", "Not two selected",
            "other|THREE", "Three selected",
            "=4|FOUR", "Four selected"
    })
    String select(@PluralCount @Offset(1) int n, @Select String select);

    @DefaultMessage("Today is {0,date,long}")
    String today(Date today);

    @DefaultMessage("Str: {startBold,<b>}{0}{endBold,</b>}")
    String substring(String str);

    String inheritance1();

    String inheritance2();

    String inheritance3();

}
