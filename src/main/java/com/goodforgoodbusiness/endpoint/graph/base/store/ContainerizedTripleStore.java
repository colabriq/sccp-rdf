package com.goodforgoodbusiness.endpoint.graph.base.store;

import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.graph.container.ContainerCollector;
import com.goodforgoodbusiness.endpoint.graph.container.ContainerStore;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Store which captures triples being added/removed in to containers
 */
@Singleton
public class ContainerizedTripleStore extends AdvanceMapTripleStore implements TripleStore {
	private static final Logger log = Logger.getLogger(ContainerizedTripleStore.class);
	
	private final ContainerStore containerStore;
	private final ContainerCollector collector;
	
	@Inject
	public ContainerizedTripleStore(ContainerStore containerStore, ContainerCollector collector) {
		this.containerStore = containerStore;
		this.collector = collector;
	}

	@Override
	public void add ( Triple trup ) {
		collector.added(trup);
		super.add(trup);
	}

	@Override
	public void delete ( Triple trup ) {
		collector.removed(trup);
		super.delete(trup);
	}

	@Override
	public ExtendedIterator<Triple> find ( Triple trup ) {
		log.debug("Find: " + trup);
		
		return super.find(trup).mapWith(t -> {
			for (var container : containerStore.getSources(t)) {
				collector.linked(new Link(container.getId(), RelType.CAUSED_BY));
			}
			
			return t;
		});
	}
}
