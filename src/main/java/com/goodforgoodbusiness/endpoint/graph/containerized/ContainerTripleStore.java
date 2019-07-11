package com.goodforgoodbusiness.endpoint.graph.containerized;

import java.util.EnumSet;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.storage.TripleContexts;
import com.goodforgoodbusiness.endpoint.storage.TripleContext.Type;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Store which captures triples being added/removed in to containers
 */
@Singleton
public class ContainerTripleStore<UNDERLYING_TYPE extends TripleStore> implements TripleStore {
	private static final Logger log = Logger.getLogger(ContainerTripleStore.class);
	
	private final UNDERLYING_TYPE underlying;
	private final TripleContexts contexts;
	private final ContainerCollector collector;
	
	@Inject
	public ContainerTripleStore(UNDERLYING_TYPE underlying, TripleContexts contexts, ContainerCollector collector) {
		this.underlying = underlying;
		this.contexts = contexts;
		this.collector = collector;
	}

	public UNDERLYING_TYPE getUnderlyingStore() {
		return underlying;
	}
	
	@Override
	public boolean isEmpty() {
		return underlying.isEmpty();
	}
	
	@Override
	public int size() {
		return underlying.size();
	}
	
	@Override
	public void add(Triple trup) {
		collector.added(trup);
		underlying.add(trup);
	}

	@Override
	public void delete(Triple trup) {
		collector.removed(trup);
		underlying.delete(trup);
	}

	@Override
	public void clear() {
		underlying.clear();
	}
	
	@Override
	public boolean contains(Triple t) {
		return underlying.contains(t);
	}
	
	@Override
	public ExtendedIterator<Triple> find(Triple trup) {
		if (log.isTraceEnabled()) {
			log.trace("Find: " + trup);
		}
		
		return underlying.find(trup).mapWith(t -> {
			contexts.getContexts(t, EnumSet.of(Type.CONTAINER_ID))
				.forEach(ctx -> {
					collector.linked(new Link(ctx.getContainerID().get(), RelType.CAUSED_BY));
				});
			
			return t;
		});
	}
	
	@Override
	public ExtendedIterator<Node> listSubjects() {
		return underlying.listSubjects();
	}

	@Override
	public ExtendedIterator<Node> listPredicates() {
		return underlying.listPredicates();
	}

	@Override
	public ExtendedIterator<Node> listObjects() {
		return underlying.listObjects();
	}
	
	@Override
	public void close() {
		underlying.close();
	}
}
