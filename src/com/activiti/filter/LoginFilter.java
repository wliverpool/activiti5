package com.activiti.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.identity.User;

import com.activiti.util.UserUtil;

public class LoginFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain) throws IOException, ServletException {
		HttpServletRequest rq = (HttpServletRequest) request;
		HttpServletResponse rp = (HttpServletResponse) response;
		HttpSession session = rq.getSession();
		String requestUrl = rq.getRequestURI();
		if(!isNotNeedCheckUrl(requestUrl)){
			User user = UserUtil.getUserFromSession(session);
			if(user==null){
				rp.sendRedirect(rq.getContextPath()+"/login.jsp");
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}
	
	private boolean isNotNeedCheckUrl(String url){
		return (url != null && (url.endsWith("login.jsp") || url.endsWith("/logon") || url.endsWith(".js") || url.endsWith(".ico")|| url.endsWith(".css") || url.endsWith(".ws")));
	}

}
