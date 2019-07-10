package com.goodforgoodbusiness.endpoint;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Create the Thread Pool for queries.
 */
@Singleton
public class ThreadPoolProvider implements Provider<ExecutorService> {
	private static final Logger log = Logger.getLogger(ThreadPoolProvider.class);
	
	private final int poolSize;

	@Inject
	public ThreadPoolProvider(@Named("threadpool.size") int threadPoolSize) {
		this.poolSize = threadPoolSize;
	}

	@Override @Singleton
	public ExecutorService get() {
		return new ThreadPoolExecutor(poolSize, poolSize, 0L, MILLISECONDS, new LinkedBlockingQueue<Runnable>()) {
			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				
				var cause = t;
		        if (t == null && r instanceof Future<?>) {
		            try {
		                Future<?> future = (Future<?>) r;
		                if (future.isDone()) {
		                    future.get();
		                }
		            }
		            catch (CancellationException ce) {
		                cause = ce;
		            }
		            catch (ExecutionException ee) {
		                cause = ee.getCause();
		            }
		            catch (InterruptedException ie) {
		                Thread.currentThread().interrupt();
		            }
		        }
		        
		        if (cause != null) {
		            log.error("Error from task", cause);
		        }
			}
		};
	}
}
