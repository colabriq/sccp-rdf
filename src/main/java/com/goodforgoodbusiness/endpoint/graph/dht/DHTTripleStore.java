package com.goodforgoodbusiness.endpoint.graph.dht;

import static java.util.Collections.newSetFromMap;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.DHTSearch;
import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerTripleStore;
import com.goodforgoodbusiness.endpoint.graph.rocks.RocksTripleStore;
import com.goodforgoodbusiness.endpoint.plugin.ContainerListenerManager;
import com.goodforgoodbusiness.endpoint.storage.TripleContext.Type;
import com.goodforgoodbusiness.endpoint.storage.TripleContexts;
import com.goodforgoodbusiness.model.StorableContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

/**
 * A store that fetches more triples from the DHT
 */
@Singleton
public class DHTTripleStore implements TripleStore {
	private static final Logger log = Logger.getLogger(DHTTripleStore.class);
	
	private final DHTSearch search;
	private final TripleContexts contexts;
	private final ContainerListenerManager listenerManager;
	private final ContainerTripleStore<RocksTripleStore> baseStore;
	
	private final Set<DHTTripleIterator> openIterators = newSetFromMap(new ConcurrentHashMap<>());
	
	@Inject
	public DHTTripleStore(DHTSearch search, TripleContexts contexts,
		ContainerListenerManager listenerManager, ContainerTripleStore<RocksTripleStore> baseStore) {
		
		this.search = search;
		this.contexts = contexts;
		this.listenerManager = listenerManager;
		this.baseStore = baseStore;
	}
	
	@Override
	public ExtendedIterator<Triple> find(Triple pattern) {
		// to avoid ConcurrentModificationExceptions, the ExtendedIterator will
		// be a 'forward snapshot' that contains triples in the local store, with
		// triples retrieved from the DHT.
		
		// 1 fetch from DHT
		// we have to wait if operating synchronously
		var block = new CompletableFuture<AsyncResult<Stream<StorableContainer>>>();
		
		search.search(
			pattern,
			Future.<Stream<StorableContainer>>future().setHandler(result -> block.complete(result))
		);
		
		try {
			var result = block.get();
			if (result.succeeded()) {
				processResults(result.result());
			}
			else {
				log.error("DHT call failed", result.cause());
			}
		}
		catch (ExecutionException | InterruptedException e) {
			log.error("DHT call interrupted", e);
		}
		
		// set up this find to return additional results where needed
		// when it's closed, remove to avoid leaks
		var mexit = new DHTTripleIterator(
			pattern,
			baseStore.find(pattern),
			it -> openIterators.remove(it)
		);
		
		// register in the CHM to receive any additonal triples
		openIterators.add(mexit);
		return mexit;
	}
	
	private void processResults(Stream<StorableContainer> containers) {
		var newTriplesDeleted = new HashSet<Triple>();
		var newTriplesAdded = new HashSet<Triple>();
		
		// 2 add to local stores
		// record any genuinely new triples
		containers.forEach(container -> {
			if (log.isDebugEnabled()) {
				log.debug("Matching container " + container.getId());
			}
			
			container.getRemoved()
				.forEach(t -> {
					if (log.isTraceEnabled()) {
						log.trace("Delete " + t);
					}
					
					if (baseStore.contains(t)) {
						baseStore.delete(t);
						newTriplesDeleted.add(t);
					}
				});
			
			container.getAdded()
				.forEach(t -> {
					if (log.isTraceEnabled()) {
						log.trace("Adding " + t);
					}
					
					if (!baseStore.contains(t)) {
						baseStore.add(t);
						newTriplesAdded.add(t);
						
						// new triples being saved get context
						contexts.create(t)
							.withType(Type.CONTAINER)
							.withContainerID(container.getId())
							.save()
						;
					}
				});
			
			listenerManager.trigger(container);
		});
		
		// update any open iterators with new triples
		openIterators.forEach(it -> {
			it.added(newTriplesAdded.stream());
			it.removed(newTriplesDeleted.stream());
		});
	}
	
	@Override
	public boolean isEmpty() {
		return baseStore.isEmpty();
	}
	
	@Override
	public int size() {
		return baseStore.size();
	}
	
	@Override
	public void add(Triple trup) {
		baseStore.add(trup);
	}

	@Override
	public void delete(Triple trup) {
		baseStore.delete(trup);
	}

	@Override
	public void clear() {
		baseStore.clear();
	}
	
	@Override
	public boolean contains(Triple t) {
		return baseStore.contains(t);
	}
	
	@Override
	public ExtendedIterator<Node> listSubjects() {
		return baseStore.listSubjects();
	}

	@Override
	public ExtendedIterator<Node> listPredicates() {
		return baseStore.listPredicates();
	}

	@Override
	public ExtendedIterator<Node> listObjects() {
		return baseStore.listObjects();
	}
	
	@Override
	public void close() {
		baseStore.close();
	}
}
