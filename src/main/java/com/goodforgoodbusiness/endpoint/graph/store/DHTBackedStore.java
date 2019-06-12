package com.goodforgoodbusiness.endpoint.graph.store;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.DHTContainerCollector;
import com.goodforgoodbusiness.endpoint.dht.DHTEngineClient;
import com.goodforgoodbusiness.endpoint.dht.DHTContainerStore;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DHTBackedStore extends AdvanceMappingStore implements TripleStore {
	private static final Logger log = Logger.getLogger(DHTBackedStore.class);
	
	private final DHTEngineClient client;
	private final DHTContainerStore containerStore;
	private final DHTContainerCollector collector;
	
	@Inject
	public DHTBackedStore(Graph parent, DHTEngineClient client, DHTContainerStore containerStore, DHTContainerCollector collector) {
		this.client = client;
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
		
		// to avoid ConcurrentModificationExceptions, the ExtendedIterator will
		// be a 'forward snapshot' that contains triples in the local store, with
		// triples retrieved from the DHT.
		
		try {
			// hit up the DHT for extra matches
			for (var container : client.matches(trup)) {
				log.debug("Matching container " + container.getId());
				
				if (containerStore.hasContainer(container)) {
					log.debug("(container already processed)");
				}
				else {
					containerStore.addContainer(container);
					
					// call super delete/add rather than delete/add to avoid
					// these received containers getting added to the container we're building
					// via the containerCollector. also, they are already wrapped.
					
					container.getRemoved()
						.forEach(t -> {
							log.debug("Delete " + t);
							containerStore.addSource(t, container);
							super.delete(t);
						});
					
					container.getAdded()
						.forEach(t -> {
							log.debug("Adding " + t);
							containerStore.addSource(t, container);
							super.add(t);
						});
				}
			}
		}
		catch (Exception e) {
			log.error("Could not reach the DHT", e); // XXX how to handle better?
		}
		
		return super.find(trup).mapWith(t -> {
			for (var container : containerStore.getSources(t)) {
				collector.linked(new Link(container.getId(), RelType.CAUSED_BY));
			}
			
			return t;
		});
	}
}
