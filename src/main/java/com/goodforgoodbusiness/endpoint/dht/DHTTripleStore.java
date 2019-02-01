package com.goodforgoodbusiness.endpoint.dht;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.Triple.Field;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphTripleStoreBase;
import org.apache.jena.mem.NodeToTriplesMapMem;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;
import com.goodforgoodbusiness.model.StoredClaim;

public class DHTTripleStore extends GraphTripleStoreBase implements TripleStore {
	private static final Logger log = Logger.getLogger(DHTTripleStore.class);
	
	private final DHTEngineClient client;
	private final ClaimContext contextMap;
	private final ClaimCollector collector;
	
	public DHTTripleStore(Graph parent, DHTEngineClient client, ClaimContext contextMap, ClaimCollector collector) {
		super(
			parent,
            new NodeToTriplesMapMem( Field.fieldSubject, Field.fieldPredicate, Field.fieldObject ),
            new NodeToTriplesMapMem( Field.fieldPredicate, Field.fieldObject, Field.fieldSubject ),
            new NodeToTriplesMapMem( Field.fieldObject, Field.fieldSubject, Field.fieldPredicate )
	    ); 
		
		this.client = client;
		this.contextMap = contextMap;
		this.collector = collector;
	}

	public void add ( Triple trup ) {
		collector.added(trup);
		super.add(trup) ;
	}

	public void delete ( Triple trup ) {
		collector.removed(trup);
		super.delete(trup);
	}

	public boolean contains ( Triple trup ) {
		return super.contains(trup);
	}

	public ExtendedIterator<Triple> find ( Triple trup ) {
		try {
			// hit up the DHT
			for (StoredClaim claim : client.matches(trup)) {
				// call super delete/add rather than delete/add to avoid
				// these received claims getting added to the claim we're building
				// via the claimCollector. also, they are already wrapped.
				
				claim.getRemoved()
					.forEach(t -> {
						contextMap.add(t, claim);
						super.delete(t);
					});
				
				claim.getAdded()
					.forEach(t -> {
						contextMap.add(t, claim);
						super.add(t);
					});
			}
		}
		catch (Exception e) {
			log.error("Could not reach the DHT", e); // XXX how to handle better?
		}
		
		return super.find(trup).mapWith(t -> {
			for (var claim : contextMap.get(t)) {
				collector.linked(new Link(claim.getId(), RelType.CAUSED_BY));
			}
			
			return t;
		});
	}
}
