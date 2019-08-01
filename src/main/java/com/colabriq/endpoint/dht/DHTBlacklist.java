package com.colabriq.endpoint.dht;

import static com.colabriq.shared.TripleUtil.isAny;

import java.util.List;
import java.util.function.Function;

import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

/**
 * Stops certain problematic searches going to the DHT (for now).
 */
public class DHTBlacklist {
	private static final Logger log = Logger.getLogger(DHTBlacklist.class);
	
	public static List<Function<String, Boolean>> BLACKLIST = List.of(
		uri -> uri.startsWith("http://www.w3.org/2000/01/rdf-schema"),
		uri -> uri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns"),
		uri -> uri.startsWith("http://www.w3.org/2002/07/owl"),
		uri -> uri.startsWith("http://www.w3.org/2003/11/swrl")
	);

	@Inject
	public DHTBlacklist() {
	}
	
	/**
	 * Reasoner asks for some patterns we cannot support in fetches
	 * Return if the blacklist includes these patterns (will not be fetched from the DHT).
	 * Eventually this should become a properties file or similar.
	 */
	public boolean includes(Triple t) {		
		// reasoner makes open predicate queries
		// for now, suppress these going to the DHT:
		if (isAny(t.getSubject()) && !isAny(t.getPredicate()) && isAny(t.getObject())) {			
			if (t.getPredicate().isURI()) {
				var uri = t.getPredicate().getURI();
				
				if (BLACKLIST.stream().map(fn -> fn.apply(uri)).anyMatch(x -> x)) {
					log.info(uri + " is blacklisted");
					return true;
				}
			}
		}
		
		return false;
	}
}
