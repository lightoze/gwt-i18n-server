package com.teklabs.gwt_i18n_server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Vladimir Kulev
 */
public class I18nInitializer implements ServletContextListener {
    static {
        MessagesProxy.initialize();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
