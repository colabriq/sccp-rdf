package com.goodforgoodbusiness.endpoint.processor.task.dht;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.crypto.EncryptionException;
import com.goodforgoodbusiness.endpoint.graph.dht.DHT;
import com.goodforgoodbusiness.endpoint.graph.persistent.container.ContainerBuilder;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.SubmittableContainer;

import io.vertx.core.Future;

/**
 * Prepares and submits a container to the DHT
 */
public class DHTPublishTask implements Runnable {
	private static final Logger log = Logger.getLogger(DHTPublishTask.class);
	
	private final DHT dht;
	private final ContainerBuilder builder;
	
	private final SubmittableContainer submittableContainer;
	private final Future<StorableContainer> future;
	
	public DHTPublishTask(DHT dht, ContainerBuilder builder, SubmittableContainer container, Future<StorableContainer> future) {
		this.dht = dht;
		this.builder = builder;
		this.submittableContainer = container;
		this.future = future;
	}
	
	@Override
	public void run() {
		try {
			var container = builder.buildFrom(submittableContainer);
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
}
