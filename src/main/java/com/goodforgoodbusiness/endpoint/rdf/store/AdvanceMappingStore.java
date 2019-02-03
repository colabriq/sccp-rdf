package com.goodforgoodbusiness.endpoint.rdf.store;

import static com.goodforgoodbusiness.shared.TripleUtil.ANYANYANY;
import static com.goodforgoodbusiness.shared.TripleUtil.makePatterns;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;

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
		for (var pattern : makePatterns(t)) {
			getPatternSet(pattern).add(t);
		}
	}

	@Override
	public void delete(Triple t) {
		for (var pattern : makePatterns(t)) {
			getPatternSet(pattern).remove(t);
		}
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
		return new TripleIterator(getPatternSet(pattern));
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
