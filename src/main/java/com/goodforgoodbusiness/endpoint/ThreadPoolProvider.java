package com.goodforgoodbusiness.endpoint;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Create the Thread Pool for queries.
 */
@Singleton
public class ThreadPoolProvider implements Provider<ExecutorService> {
	private final int threadPoolSize;

	@Inject
	public ThreadPoolProvider(@Named("threadpool.size") int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	@Override @Singleton
	public ExecutorService get() {
		return Executors.newFixedThreadPool( threadPoolSize );
	}
}
