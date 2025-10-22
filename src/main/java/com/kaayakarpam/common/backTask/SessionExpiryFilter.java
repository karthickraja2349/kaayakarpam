package com.kaayakarpam.common.backTask;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;

import java.io.IOException;

@WebFilter("/*") 
public class SessionExpiryFilter implements Filter {


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false); 

        if (session != null && session.getAttribute("SESSION_EXPIRED") != null) {

            session.removeAttribute("SESSION_EXPIRED");
            session.invalidate();
            res.sendRedirect(req.getContextPath() + "/kaayakarpam/index.html?message=session_expired");
            return; 
        }
        chain.doFilter(request, response);
    }


    public void init(FilterConfig filterConfig) throws ServletException {}

    public void destroy() {}
}
