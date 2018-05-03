package com.dove.core;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.dove.core.task.TaskManager;
import com.dove.util.config.ContextProperties;
import com.dove.util.plugin.DovePlugin;

/**
 * Plugin class for the Core plugin
 * 
 * @author Marco Meschieri - Logical Objects
 * @since 3.5.0
 */
public class CorePlugin extends DovePlugin {

	protected static Logger log = LoggerFactory.getLogger(CorePlugin.class);

	@Override
	protected void start() throws Exception {
		ContextProperties pbean = new ContextProperties();
		if (StringUtils.isEmpty(pbean.getProperty("id")))
			pbean.setProperty("id", UUID.randomUUID().toString());
		pbean.write();

		try {
//			TaskManager manager = new TaskManager();
//			manager.registerTasks();
		} catch (Throwable t) {
			log.error("Unable to register some tasks", t);
		}
	}

	@Override
	protected void install() throws Exception {
		// Enable the aspects in the runlevels
		ContextProperties pbean = new ContextProperties();
		for (String aspect : RunLevel.getAspects()) {
			for (RunLevel level : RunLevel.values())
				pbean.setProperty("aspect." + aspect + "." + level.toString(), "true");
		}
		pbean.write();

		setRestartRequired();
	}
}