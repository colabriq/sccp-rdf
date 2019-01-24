package com.goodforgoodbusiness.rdfjava.dht;

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
	
	private final ClaimContextMap claimContextMap;
	private final ClaimCollector claimCollector;
	
	public DHTTripleStore(Graph parent, ClaimContextMap claimContextMap, ClaimCollector claimCollector) {
		super(
			parent,
            new NodeToTriplesMapMem( Field.fieldSubject, Field.fieldPredicate, Field.fieldObject ),
            new NodeToTriplesMapMem( Field.fieldPredicate, Field.fieldObject, Field.fieldSubject ),
            new NodeToTriplesMapMem( Field.fieldObject, Field.fieldSubject, Field.fieldPredicate )
	    ); 
		
		this.claimContextMap = claimContextMap;
		this.claimCollector = claimCollector;
	}

	public void add ( Triple trup ) {
		claimCollector.added(trup);
		super.add(trup) ;
	}

	public void delete ( Triple trup ) {
		claimCollector.removed(trup);
		super.delete(trup);
	}

	public boolean contains ( Triple trup ) {
		return super.contains(trup);
	}

	public ExtendedIterator<Triple> find ( Triple trup ) {
		// caching ??
		
		try {
			// hit up the DHT
			for (StoredClaim claim : DHTClient.matches(trup)) {
				// call super delete/add rather than delete/add to avoid
				// these received claims getting added to the claim we're building
				// via the claimCollector. also, they are already wrapped.
				
				claim.getRemoved().forEach( t -> super.delete(t) );
				claim.getAdded().forEach(t -> super.add(t) );
			}
		}
		catch (Exception e) {
			log.error("Could not reach the DHT", e);
		}
		
		return super.find(trup).mapWith(t -> {
			for (var claim : claimContextMap.get(t)) {
				claimCollector.linked(new Link(claim.getId(), RelType.CAUSED_BY));
			}
			
			return t;
		});
	}
}
