package com.colabriq.endpoint.graph.dht;

import static com.colabriq.shared.TripleUtil.isMatch;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.colabriq.endpoint.graph.base.store.iterator.functional.MultiExtendedIterator;
import com.colabriq.endpoint.graph.base.store.iterator.functional.WrappedExtendedIterator;

/**
 * Helps us iterate over triples that have been stored, as they arrive from the DHT
 */
public class DHTTripleIterator extends MultiExtendedIterator<Triple> implements ExtendedIterator<Triple> {
	private final Triple pattern;
	private final Consumer<DHTTripleIterator> closeAction;

	public DHTTripleIterator(Triple pattern, ExtendedIterator<Triple> first, Consumer<DHTTripleIterator> closeAction) {
		super(first);
		this.pattern = pattern;
		this.closeAction = closeAction;
	}

	/**
	 * Offer this iterator additional triples from the DHT
	 */
	protected void added(Stream<Triple> triples) {
		super.add(new WrappedExtendedIterator<>(
			// only triples matching actual pattern for this iterator
			triples.filter(triple -> isMatch(pattern, triple)).iterator()
		));
	}
	
	/**
	 * Remove triples deleted by subsequent containers
	 */
	protected void removed(Stream<Triple> triples) {
		
	}
	
	@Override
	public void close() {
		this.closeAction.accept(this);
		super.close();
	}
}
