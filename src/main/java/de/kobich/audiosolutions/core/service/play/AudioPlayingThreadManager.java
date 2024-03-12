package de.kobich.audiosolutions.core.service.play;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Thread manager. Ensures that only one playing thread is active.
 * @author ckorn
 */
@Service
public class AudioPlayingThreadManager {
	private static final Logger logger = Logger.getLogger(AudioPlayingThreadManager.class);

	private ExecutorService executer;

	@PostConstruct
	public void init() {
		this.executer = Executors.newSingleThreadExecutor();
	}

	/**
	 * Start audio thread
	 * @param runnable
	 */
	public void startRunnable(Runnable runnable) {
		executer.execute(runnable);
	}
	
	public boolean isShutdown() {
		return executer.isShutdown();
	}

	@PreDestroy
	public void shutdown() {
		// Disable new tasks from being submitted
		executer.shutdown(); 
		try {
			// Wait a while for existing tasks to terminate
			if (!executer.awaitTermination(100, TimeUnit.MILLISECONDS)) {
				// Cancel currently executing tasks
				executer.shutdownNow(); 
				
				// Wait a while for tasks to respond to being cancelled
				if (!executer.awaitTermination(100, TimeUnit.MILLISECONDS)) {
					logger.error("Thread executor did not terminate");
				}
			}
		}
		catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			executer.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
}
