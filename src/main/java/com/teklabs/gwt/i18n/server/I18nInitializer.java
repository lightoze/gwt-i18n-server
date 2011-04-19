package com.teklabs.gwt.i18n.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Vladimir Kulev
 */
public class I18nInitializer implements ServletContextListener {
    static {
        LocaleProxy.initialize();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
