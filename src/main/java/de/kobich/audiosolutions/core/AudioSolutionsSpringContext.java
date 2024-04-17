package de.kobich.audiosolutions.core;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import de.kobich.commons.concurrent.StartupLock;

class AudioSolutionsSpringContext {
	private static final Logger logger = Logger.getLogger(AudioSolutionsSpringContext.class);
	private AnnotationConfigApplicationContext applicationContext;
	private StartupLock startupLock;
	
	public AudioSolutionsSpringContext() {
		this.startupLock = new StartupLock(1);
	}
	
	/**
	 * Returns a service implementation by class or null
	 * @param name
	 * @return
	 */
	public <T> T getService(Class<T> clazz) {
		startupLock.waitForInitialisation();
		
		if (applicationContext == null) {
			throw new IllegalStateException("Application context is null");
		}
		return applicationContext.getBean(clazz);
	}
	
	/**
	 * Returns a service implementation by name or null
	 * @param name
	 * @return
	 */
	public <T> T getService(String name, Class<T> clazz) {
		startupLock.waitForInitialisation();
		
		if (applicationContext == null) {
			throw new IllegalStateException("Application context is null");
		}
		return applicationContext.getBean(name, clazz);
	}
	
	public void startup() {
		try {
			// init spring
			if (applicationContext == null) {
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				this.applicationContext = new AnnotationConfigApplicationContext(AudioSolutionsSpringConfig.class);
				logger.info("Initalizing spring context takes %dms".formatted(stopWatch.getTime(TimeUnit.MILLISECONDS)));
			}
		}
		finally {
			this.startupLock.release();
		}
	}
	
	/**
	 * Closes the application context
	 */
	public void close() {
		if (applicationContext != null) {
			logger.info("Closing spring context");
			applicationContext.close();
			applicationContext = null;
		}
		this.startupLock = new StartupLock(1);
	}
}
