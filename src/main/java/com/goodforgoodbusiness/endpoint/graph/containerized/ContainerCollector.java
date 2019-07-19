package com.goodforgoodbusiness.endpoint.graph.containerized;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.dht.DHT;
import com.goodforgoodbusiness.endpoint.plugin.ContainerListenerManager;
import com.goodforgoodbusiness.endpoint.processor.task.dht.DHTPublishTask;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Future;

/**
 * Collect triples and links up preparing them to form a container
 */
@Singleton
public class ContainerCollector {	
	private final ThreadLocal<SubmittableContainer> containerLocal = new ThreadLocal<>();
	
	private final DHT dht;
	private final ContainerBuilder builder;
	private final ContainerListenerManager listenerManager;
	private final ExecutorService service;
	
	@Inject
	public ContainerCollector(DHT dht, ContainerListenerManager listMan, ContainerBuilder builder, ExecutorService service) {
		this.dht = dht;
		this.listenerManager = listMan;
		this.builder = builder;
		this.service = service;
	}
	
	public SubmittableContainer begin() {
		if (containerLocal.get() == null) {
			var container = new SubmittableContainer() {
				@Override
				public void submit(Future<StorableContainer> future, SubmitMode mode) {
					// create & publish StorableContainer
					var storeableContainer = builder.buildFrom(this);
					
					switch (mode) {
					case SYNCHRONOUS:
						publishSynchronously(storeableContainer, future);
						break;
						
					case ASYNCHRONOUS:
						publishAsynchronously(storeableContainer, future);
						break;
						
					case NONE:
						publishNone(storeableContainer, future);
						break;
					}
					
					
				}
			};
			
			containerLocal.set(container);
			return container;
		}
		else {
			return containerLocal.get();
		}
	}
	
	public void clear() {
		containerLocal.remove();
	}
	
	public Optional<SubmittableContainer> current() {
		return Optional.ofNullable(containerLocal.get());
	}
	
	public void added(Triple trup) {
		current().ifPresent(container -> container.added(trup));
	}

	public void removed(Triple trup) {
		current().ifPresent(container -> container.removed(trup));
	}
	
	public void linked(Link link) {
		current().ifPresent(container -> container.linked(link));
	}
	
	// various publish methods
	
	private void publishSynchronously(StorableContainer container, Future<StorableContainer> future) {
		service.submit(
			new DHTPublishTask(dht, container, Future.<StorableContainer>future().setHandler(
				result -> {
					if (result.succeeded()) {
						// trigger listeners for reasoning etc
						listenerManager.trigger(result.result());
						
						// pass back to outer future
						future.complete(container); 
					}
					else {
						future.fail(result.cause());
					}
				}
			)
		));
	}
	
	public void publishAsynchronously(StorableContainer container, Future<StorableContainer> future) {
		// publish this new container
		service.submit(
			new DHTPublishTask(dht, container, Future.<StorableContainer>future().setHandler(
				result -> {
					if (result.succeeded()) {
						// trigger listeners for reasoning etc
						listenerManager.trigger(result.result());
					}
					else {
						// XXX do something if this fails async
					}
				}
			)
		));
		
		// immediately complete!
		future.complete(container);
	}
	
	public void publishNone(StorableContainer container, Future<StorableContainer> future) {
		// XXX rather than submitting, create context for the triples that marks them as local only.
		// be careful if they already have other context.
		future.complete(container);
	}
}
