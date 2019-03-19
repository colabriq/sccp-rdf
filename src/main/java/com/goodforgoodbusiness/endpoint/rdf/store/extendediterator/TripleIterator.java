package com.goodforgoodbusiness.endpoint.rdf.store.extendediterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.goodforgoodbusiness.shared.ObservableSet;
import com.goodforgoodbusiness.shared.ObservableSetListener;

/**
 * Iterator based on a deque.
 * This is so triples download from the DHT in response to subqueries are reflected immediately. 
 */
public class TripleIterator extends DequeExtendedIterator<Triple> implements ExtendedIterator<Triple> {
	private final ObservableSet<Triple> results;
	private final ObservableSetListener<Triple> listener;

	/**
	 * Copy the provided Set in to the deque. 
	 * Subsequent adds/removes will be appended.
	 */
	public TripleIterator(ObservableSet<Triple> results) {
		super(results);
		this.results = results;
		
		// important: maintain a hard ref to the listener
		// otherwise ObservableSet will let it garbage collect
		this.listener = new ObservableSetListener<>() {
			@Override
			public void added(Triple t) {
				TripleIterator.this.append(t);
			}

			@Override
			public void removed(Triple t) {
				TripleIterator.this.skip(t);
			}
		};
		
		results.addListener(this.listener);
	}
	
	@Override
	public void close() {
		super.close();
		this.results.removeListener(this.listener);
	}
}
