package com.teklabs.gwt.i18n.server;

import org.apache.commons.lang.LocaleUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Vladimir Kulev
 */
public class I18nFilter implements Filter {
    static {
        LocaleProxy.initialize();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
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

        LocaleProxy.setLocale(locale);
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            LocaleProxy.clear();
        }
    }

    @Override
    public void destroy() {
    }
}
