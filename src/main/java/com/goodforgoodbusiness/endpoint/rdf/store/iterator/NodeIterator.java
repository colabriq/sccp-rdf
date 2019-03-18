package com.goodforgoodbusiness.endpoint.rdf.store.iterator;

import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.goodforgoodbusiness.shared.ObservableSet;
import com.goodforgoodbusiness.shared.ObservableSetListener;

/**
 * Iterator based on a deque.
 * This is so triples download from the DHT in response to subqueries are reflected immediately. 
 */
public class NodeIterator extends DequeIterator<Node> implements ExtendedIterator<Node> {
	private final ObservableSet<Triple> results;
	private final ObservableSetListener<Triple> listener;

	/**
	 * Copy the provided Set in to the deque. 
	 * Subsequent adds/removes will be appended.
	 */
	public NodeIterator(ObservableSet<Triple> results, Function<? super Triple, ? extends Node> mapper) {
		super(results.stream().map(mapper));
		this.results = results;
		
		// important: maintain a hard ref to the listener
		// otherwise ObservableSet will let it garbage collect
		this.listener = new ObservableSetListener<>() {
			@Override
			public void added(Triple t) {
				NodeIterator.this.append(mapper.apply(t));
			}

			@Override
			public void removed(Triple t) {
				NodeIterator.this.skip(mapper.apply(t));
			}
		};
		
		results.addListener(listener);
	}
	
	@Override
	public void close() {
		super.close();
		this.results.removeListener(this.listener);
	}
}
