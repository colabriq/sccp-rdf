package com.goodforgoodbusiness.endpoint.graph.containerized;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.dht.DHT;
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
	private final ExecutorService service;
	
	@Inject
	public ContainerCollector(DHT dht, ContainerBuilder builder, ExecutorService service) {
		this.dht = dht;
		this.builder = builder;
		this.service = service;
	}
	
	public SubmittableContainer begin() {
		if (containerLocal.get() == null) {
			var container = new SubmittableContainer();
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
	
	/**
	 * Submit a container for publishing
	 */
	public void submit(SubmittableContainer container, Future<StorableContainer> future) {
		service.submit(new DHTPublishTask(dht, builder, container, future));
	}
}
