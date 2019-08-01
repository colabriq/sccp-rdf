package com.goodforgoodbusiness.endpoint.processor.task.dht;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.crypto.EncryptionException;
import com.goodforgoodbusiness.endpoint.dht.DHT;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.SubmittableContainer.SubmitMode;
import com.goodforgoodbusiness.shared.executor.PrioritizedTask;

import io.vertx.core.Future;

/**
 * Prepares and submits a container to the DHT
 */
public class DHTPublishTask implements Runnable, PrioritizedTask {
	private static final Logger log = Logger.getLogger(DHTPublishTask.class);
	
	private final DHT dht;
	private final StorableContainer container;
	private final SubmitMode mode;
	
	private final Future<StorableContainer> future;
	
	public DHTPublishTask(DHT dht, StorableContainer container, SubmitMode mode, Future<StorableContainer> future) {
		this.dht = dht;
		this.container = container;
		this.mode = mode;
		
		this.future = future;
	}
	
	@Override
	public void run() {
		try {
			log.info("Publishing container " + container.getId());
			
			dht.publish(container, Future.<Void>future().setHandler(publishResult -> {
				if (publishResult.succeeded()) {
					future.complete(container);
				}
				else {
					future.fail(publishResult.cause());
				}
			}));
		}
		catch (EncryptionException ee) {
			log.error(ee);
			future.fail(ee);
		}
	}
	
	@Override
	public Priority getPriority() {
		return (mode == SubmitMode.ASYNC) ? Priority.NICED : Priority.REAL;
	}
}
