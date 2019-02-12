package com.goodforgoodbusiness.endpoint.dht.store;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.ClaimCollector;
import com.goodforgoodbusiness.endpoint.dht.ClaimContext;
import com.goodforgoodbusiness.endpoint.dht.DHTEngineClient;
import com.goodforgoodbusiness.endpoint.rdf.store.AdvanceMappingStore;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;
import com.goodforgoodbusiness.model.StoredClaim;

public class DHTTripleStore extends AdvanceMappingStore implements TripleStore {
	private static final Logger log = Logger.getLogger(DHTTripleStore.class);
	
	private final DHTEngineClient client;
	private final ClaimContext context;
	private final ClaimCollector collector;
	
	public DHTTripleStore(Graph parent, DHTEngineClient client, ClaimContext context, ClaimCollector collector) {
		this.client = client;
		this.collector = collector;
		this.context = context;
	}

	public void add ( Triple trup ) {
		collector.added(trup);
		super.add(trup);
	}

	public void delete ( Triple trup ) {
		collector.removed(trup);
		super.delete(trup);
	}

	public ExtendedIterator<Triple> find ( Triple trup ) {
		log.debug("Find: " + trup);
		
		// to avoid ConcurrentModificationExceptions, the ExtendedIterator will
		// be a 'forward snapshot' that contains triples in the local store, with
		// triples retrieved from the DHT.
		
		try {
			// hit up the DHT for extra matches
			for (StoredClaim claim : client.matches(trup)) {
				log.debug("Matching claim " + claim.getId());
				
				if (context.contains(claim)) {
					log.debug("(claim already processed)");
				}
				else {
					// call super delete/add rather than delete/add to avoid
					// these received claims getting added to the claim we're building
					// via the claimCollector. also, they are already wrapped.
					
					claim.getRemoved()
						.forEach(t -> {
							log.debug("Delete " + t);
							context.add(t, claim);
							super.delete(t);
						});
					
					claim.getAdded()
						.forEach(t -> {
							log.debug("Adding " + t);
							context.add(t, claim);
							super.add(t);
						});
				}
			}
		}
		catch (Exception e) {
			log.error("Could not reach the DHT", e); // XXX how to handle better?
		}
		
		return super.find(trup).mapWith(t -> {
			for (var claim : context.get(t)) {
				collector.linked(new Link(claim.getId(), RelType.CAUSED_BY));
			}
			
			return t;
		});
	}
}
