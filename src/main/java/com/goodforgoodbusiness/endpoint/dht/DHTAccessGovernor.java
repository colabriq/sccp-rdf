package com.goodforgoodbusiness.endpoint.dht;

import static java.util.stream.Collectors.toSet;
import static org.apache.jena.graph.Node.ANY;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/** 
 * This doesn't actually cache triples, but prevents the system going to the DHT too often
 * or nonsensically. 
 * 
 * If we just queried for (foo, ANY, ANY) a query of (foo, blah, ANY) will not yield any
 * further results that we could decrypt unless our list of available sharekeys has changed.
 * 
 * The intended behaviour is that if this fails, you only draw from the local store.
 */
@Singleton
public class DHTAccessGovernor {
	private static final Logger log = Logger.getLogger(DHTAccessGovernor.class);
	
	// may eventually want to store something as the cache value
	// but for the moment, only need to see if it's present at all
	private static final Object PRESENT = new Object();
	
	private Cache<Triple, Object> tracker;

	@Inject
	public DHTAccessGovernor(@Named("dht.cache.enabled") boolean enabled, @Named("dht.cache.duration") String cacheDuration) {
		this(enabled, Duration.parse(cacheDuration));
	}
	
	public DHTAccessGovernor(boolean enabled, Duration cacheDuration) {
		if (enabled) {
			this.tracker = CacheBuilder
				.newBuilder()
				.expireAfterWrite(cacheDuration)
				.build()
			;
		}
		else {
			this.tracker = null;
		}
	}
	
	public boolean allow(Triple triple) {
		if (tracker != null) {
			// calculate 'wider' combinations that would have netted this triple
			var any = combinations(triple)
				.parallelStream()
				.filter(c -> (tracker.getIfPresent(c) != null))
				.findFirst()
			;
			
			if (any.isPresent()) {
				if (log.isDebugEnabled()) log.debug("Deny search for " + triple + " as recent search for " + any.get());
				return false; // present
			}
			else {
				tracker.put(triple, PRESENT);
				return true;
			}
		}
		else {
			return true;
		}
	}
	
	public void invalidate(Triple triple) {
		if (tracker != null) {
			// for the moment, do full invalidation here.
			// become more nuanced w.r.t. received SharedAcceptRequests with time.
			tracker.invalidateAll();
		}
	}
	
	private static Set<Triple> combinations(Triple triple) {
		var sub = triple.getSubject();
		var pre = triple.getPredicate();
		var obj = triple.getObject();
		
		return 
			Stream.of(
				triple, 
				new Triple(sub, pre, ANY),
				new Triple(sub, ANY, obj),
				new Triple(ANY, pre, obj),
				new Triple(sub, ANY, ANY),
				new Triple(ANY, ANY, obj)
			)
			.parallel()
			// remove (ANY, ANY, ANY) and (ANY, pre, ANY)
			// these can crop up if you specified an incomplete Triple
			.filter(t -> !(t.getSubject().equals(ANY) && t.getObject().equals(ANY)))
			.collect(toSet())
		;
	}
}
