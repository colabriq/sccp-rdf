package com.goodforgoodbusiness.endpoint.graph.container;

import java.util.Optional;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.google.inject.Singleton;

@Singleton
public class ContainerCollector {
	private final ThreadLocal<SubmittableContainer> containerLocal = new ThreadLocal<>();
	
	public SubmittableContainer begin() {
		var container = new SubmittableContainer();
		containerLocal.set(container);
		return container;
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
}
