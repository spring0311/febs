package cc.mrbird.febs.common.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import cc.mrbird.febs.system.entity.User;
import cc.mrbird.febs.system.service.impl.UserServiceImpl;


//@WebListener
public class SessionListener implements HttpSessionListener{

	private MySessionContext context=MySessionContext.getMySessionContext();

	@Override
	public void sessionCreated(HttpSessionEvent e) {
		context.addSession(e.getSession());
	}
	
	@Override
	public void sessionDestroyed(HttpSessionEvent e) {
		User user=(User)e.getSession().getAttribute("user");
		//获取service
		UserServiceImpl service=(UserServiceImpl) getObjectFromApplocation(e.getSession().getServletContext(), "UserServiceImpl"); 
		//获取用户的session和用户的状态
		//User tempUser=service.fin
		
	}
	
	
	/**
	 * 得到容器中的bean对象
	 * @param servletContext
	 * @param beanName
	 * @return
	 */
	private Object getObjectFromApplocation(ServletContext servletContext,String beanName) {
		//通过WebApplicationContextUtils 得到Spring容器的实例。  
		ApplicationContext application=WebApplicationContextUtils.getWebApplicationContext(servletContext);
		return application.getBean("beanName");
	}





}
