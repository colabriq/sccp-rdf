package com.goodforgoodbusiness.endpoint.dht.store;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.ContainerCollector;
import com.goodforgoodbusiness.endpoint.dht.ContainerContexts;
import com.goodforgoodbusiness.endpoint.dht.DHTEngineClient;
import com.goodforgoodbusiness.endpoint.rdf.store.AdvanceMappingStore;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;

public class DHTTripleStore extends AdvanceMappingStore implements TripleStore {
	private static final Logger log = Logger.getLogger(DHTTripleStore.class);
	
	private final DHTEngineClient client;
	private final ContainerContexts context;
	private final ContainerCollector collector;
	
	public DHTTripleStore(Graph parent, DHTEngineClient client, ContainerContexts context, ContainerCollector collector) {
		this.client = client;
		this.collector = collector;
		this.context = context;
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
		
		// to avoid ConcurrentModificationExceptions, the ExtendedIterator will
		// be a 'forward snapshot' that contains triples in the local store, with
		// triples retrieved from the DHT.
		
		try {
			// hit up the DHT for extra matches
			for (var container : client.matches(trup)) {
				log.debug("Matching container " + container.getId());
				
				if (context.contains(container)) {
					log.debug("(container already processed)");
				}
				else {
					// call super delete/add rather than delete/add to avoid
					// these received containers getting added to the container we're building
					// via the containerCollector. also, they are already wrapped.
					
					container.getRemoved()
						.forEach(t -> {
							log.debug("Delete " + t);
							context.add(t, container);
							super.delete(t);
						});
					
					container.getAdded()
						.forEach(t -> {
							log.debug("Adding " + t);
							context.add(t, container);
							super.add(t);
						});
				}
			}
		}
		catch (Exception e) {
			log.error("Could not reach the DHT", e); // XXX how to handle better?
		}
		
		return super.find(trup).mapWith(t -> {
			for (var container : context.get(t)) {
				collector.linked(new Link(container.getId(), RelType.CAUSED_BY));
			}
			
			return t;
		});
	}
}
