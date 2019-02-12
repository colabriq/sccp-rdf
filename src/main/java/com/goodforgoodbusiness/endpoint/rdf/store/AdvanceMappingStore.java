package com.goodforgoodbusiness.endpoint.rdf.store;

import static com.goodforgoodbusiness.shared.TripleUtil.ANYANYANY;
import static org.apache.jena.graph.Node.ANY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.rdf.store.iterator.NodeIterator;
import com.goodforgoodbusiness.endpoint.rdf.store.iterator.TripleIterator;
import com.goodforgoodbusiness.shared.ObservableSet;

/**
 * A TripleStore that computes combinations in advance.
 * 
 * This is helpful for a number of reasons when working with the DHT, primarily because 
 * there is a clear list to return from a particular find operation.
 * 
 * Takes more memory but likely a bit faster than the s/p/o-style store.
 */
public class AdvanceMappingStore implements TripleStore {
	private static final Logger log = Logger.getLogger(AdvanceMappingStore.class);
	
	private static Stream<Triple> mappings(Triple triple) {
		var sub = triple.getSubject();
		var pre = triple.getPredicate();
		var obj = triple.getObject();
		
		if (sub == null || sub.equals(ANY) || pre == null || pre.equals(ANY) || obj == null || obj.equals(ANY)) {
			throw new IllegalArgumentException("Triples in store must be concrete (no ANY)");
		}
		
		return Stream.of(
			// pick 3
			new Triple(sub, pre, obj),
			
			// pick 2
			new Triple(sub, pre, ANY),
			new Triple(sub, ANY, obj),
			new Triple(ANY, pre, obj),
			
			// pick 1
			new Triple(sub, ANY, ANY),
			new Triple(ANY, pre, ANY),
			new Triple(ANY, ANY, obj),
			
			// pick 0
			new Triple(ANY, ANY, ANY)
		);
	}
	
	private Map<Triple, ObservableSet<Triple>> patternMap = new HashMap<>();
	
	public AdvanceMappingStore() {
	}
	
	private ObservableSet<Triple> getPatternSet(Triple pattern) {
		patternMap.computeIfAbsent(pattern, k -> new ObservableSet<Triple>(new HashSet<>(), Triple.class));
		return patternMap.get(pattern);
	}
	
	@Override
	public void close() {
		patternMap.clear();
	}
	
	@Override
	public void clear() {
		patternMap.clear();
	}

	@Override
	public void add(Triple t) {
		mappings(t).forEach(p -> getPatternSet(p).add(t));
	}

	@Override
	public void delete(Triple t) {
		mappings(t).forEach(p -> getPatternSet(p).remove(t));
	}

	@Override
	public int size() {
		return getPatternSet(ANYANYANY).size();
	}

	@Override
	public boolean isEmpty() {
		return getPatternSet(ANYANYANY).isEmpty();
	}

	@Override
	public boolean contains(Triple t) {
		return getPatternSet(ANYANYANY).contains(t);
	}
	
	@Override
	public ExtendedIterator<Triple> find(Triple pattern) {
		if (log.isDebugEnabled()) log.debug("Pattern: " + pattern);
		var patternSet = getPatternSet(pattern);
		
		if (log.isDebugEnabled()) log.debug("Results= " + patternSet.size());
		return new TripleIterator(patternSet);
	}

	@Override
	public ExtendedIterator<Node> listSubjects() {
		return new NodeIterator(getPatternSet(ANYANYANY), Triple::getSubject);
	}
	
	@Override
	public ExtendedIterator<Node> listPredicates() {
		return new NodeIterator(getPatternSet(ANYANYANY), Triple::getPredicate);
	}

	@Override
	public ExtendedIterator<Node> listObjects() {
		return new NodeIterator(getPatternSet(ANYANYANY), Triple::getObject);
	}
}
