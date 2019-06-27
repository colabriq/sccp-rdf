package com.goodforgoodbusiness.endpoint.graph.dht.store;

import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.graph.base.store.ContainerizedTripleStore;
import com.goodforgoodbusiness.endpoint.graph.container.ContainerCollector;
import com.goodforgoodbusiness.endpoint.graph.container.ContainerStore;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTBlacklist;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTEngineClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RemoteTripleStore extends ContainerizedTripleStore implements TripleStore {
	private static final Logger log = Logger.getLogger(RemoteTripleStore.class);
	
	private final DHTEngineClient client;
	private final DHTBlacklist blacklist;
	
	private final ContainerStore containerStore;
	
	@Inject
	public RemoteTripleStore(DHTEngineClient client, DHTBlacklist blacklist, ContainerStore store, ContainerCollector collector) {
		super(store, collector);
		this.client = client;
		this.blacklist = blacklist;
		
		this.containerStore = store;
	}

	@Override
	public ExtendedIterator<Triple> find ( Triple trup ) {
		log.debug("Find: " + trup);
		
		// to avoid ConcurrentModificationExceptions, the ExtendedIterator will
		// be a 'forward snapshot' that contains triples in the local store, with
		// triples retrieved from the DHT.
		
		if (!blacklist.includes(trup)) {
			try {
				// hit up the DHT for extra matches
				for (var container : client.matches(trup)) {
					log.debug("Matching container " + container.getId());
					if (containerStore.addContainer(container, false)) {
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
					else {
						log.debug("(container already processed)");
					}
				}
			}
			catch (Exception e) {
				log.error("Error reaching DHT", e); // XXX how to handle better?
			}
		}
		
		return super.find(trup);
	}
}
