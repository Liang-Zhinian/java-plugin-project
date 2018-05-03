package com.dove.web.listener;

import javax.servlet.ServletContextEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

import com.dove.core.SystemInfo;
import com.dove.core.security.dao.SessionDAO;
import com.dove.util.Context;

/**
 * Listener that initializes and destroys the Spring Context
 * 
 * @author Liang Zhinian - Dove
 * @since 1.0
 *
 */
public class ContextListener extends ContextLoaderListener {

	private static Logger log = LoggerFactory.getLogger(ContextListener.class);

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		try {
			log.info("Cleanup all the sessions of the current node ({})", SystemInfo.get().getInstallationId());
			SessionDAO sessionDAO = (SessionDAO) Context.get().getBean(SessionDAO.class);
			sessionDAO.deleteCurrentNodeSessions();
		} catch (Throwable e) {
			log.warn(e.getMessage(), e);
		}
		
		super.contextDestroyed(event);
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);
		
		try {
			log.info("Cleanup past sessions of the current node ({})", SystemInfo.get().getInstallationId());
			SessionDAO sessionDAO = (SessionDAO) Context.get().getBean(SessionDAO.class);
			sessionDAO.deleteCurrentNodeSessions();
		} catch (Throwable e) {
			log.warn(e.getMessage(), e);
		}
	}
}