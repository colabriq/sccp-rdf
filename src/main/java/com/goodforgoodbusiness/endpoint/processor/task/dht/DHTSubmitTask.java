package com.goodforgoodbusiness.endpoint.processor.task.dht;

import com.goodforgoodbusiness.endpoint.crypto.EncryptionException;
import com.goodforgoodbusiness.endpoint.graph.persistent.container.ContainerBuilder;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.SubmittableContainer;

import io.vertx.core.Future;

/**
 * Prepares and submits a container
 */
public class DHTSubmitTask implements Runnable {
	private final ContainerBuilder builder;
	
	private final SubmittableContainer container;
	private final Future<StorableContainer> future;
	
	public DHTSubmitTask(ContainerBuilder builder, SubmittableContainer container, Future<StorableContainer> future) {
		this.builder = builder;
		this.container = container;
		this.future = future;
	}
	
	@Override
	public void run() {
		try {
			future.complete(builder.buildFrom(container));
		}
		catch (EncryptionException ee) {
			future.fail(ee);
		}
	}
}
