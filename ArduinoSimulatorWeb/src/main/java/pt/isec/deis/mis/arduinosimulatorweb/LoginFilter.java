/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author pafgoncalves@ipc.pt
 */
@WebFilter("/*")
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {    
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        
        boolean loggedIn = session != null && session.getAttribute("user") != null;
        
        String loginURI = request.getContextPath() + "/login";

        boolean privatePath = request.getRequestURI().startsWith(request.getContextPath() + "/app/")
                || request.getRequestURI().startsWith(request.getContextPath() + "/admin/")
                || request.getRequestURI().startsWith(request.getContextPath() + "/ws/client");
        
        boolean adminPath = request.getRequestURI().startsWith(request.getContextPath() + "/admin/");
        
        boolean isAdmin = session != null 
                && session.getAttribute("is_admin")!=null 
                && (int)session.getAttribute("is_admin")==1;
        
        if( privatePath && !loggedIn ) {
            response.sendRedirect(loginURI);
        } else if( adminPath && !isAdmin ) {
            response.sendRedirect(request.getContextPath());
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
