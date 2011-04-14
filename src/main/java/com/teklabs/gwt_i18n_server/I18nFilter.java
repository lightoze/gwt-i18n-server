package com.teklabs.gwt_i18n_server;

import org.apache.commons.lang.LocaleUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author Vladimir Kulev
 */
public class I18nFilter implements Filter {
    private boolean sync;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        sync = Arrays.asList("default", "hosted").contains(System.getProperty("config"));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        Locale locale = LocaleUtils.toLocale(servletRequest.getParameter("locale"));
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        if (locale != null) {
            req.getSession(true).setAttribute("locale", locale);
        } else {
            HttpSession session = req.getSession(false);
            if (session != null) {
                locale = (Locale) session.getAttribute("locale");
            }
        }
        if (locale == null) {
            locale = servletRequest.getLocale();
        }

        MessagesProxy.setLocale(locale);
        try {
            if (sync
                    && !req.getRequestURI().endsWith("gwtrpc")
                    && !req.getRequestURI().endsWith("gwteventservice")
                    && !req.getRequestURI().endsWith("gwtComet")
                    && !req.getRequestURI().endsWith("gwtPoll")) {
                synchronized (this) {
                    filterChain.doFilter(servletRequest, servletResponse);
                }
            } else {
                filterChain.doFilter(servletRequest, servletResponse);
            }
        } finally {
            MessagesProxy.clear();
        }
    }

    @Override
    public void destroy() {
    }
}
