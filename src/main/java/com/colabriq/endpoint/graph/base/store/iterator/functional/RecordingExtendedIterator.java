package com.goodforgoodbusiness.endpoint.graph.base.store.iterator.functional;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.util.iterator.ClosableIterator;

/**
 * Records the items that have been seen.
 * Memory intensive but necessary.
 */
public class RecordingExtendedIterator<T> implements DefaultExtendedIterator<T> {
	private final Iterator<? extends T> underlying;
	private final Set<T> seen = new HashSet<>();
	
	public RecordingExtendedIterator(Iterator<? extends T> underlying) {
		this.underlying = underlying;
	}
	
	@Override
	public boolean hasNext() {
		return underlying.hasNext();
	}

	@Override
	public T next() {
		var next = underlying.next();
		seen.add(next);
		return next;
	}
	
	@Override
	public T removeNext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> toList() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<T> toSet() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void close() {
		if (underlying instanceof ClosableIterator) {
			((ClosableIterator<?>)underlying).close();
		}
		
		try {
			if (underlying instanceof AutoCloseable) {
				((AutoCloseable)underlying).close();
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Close failed", e);
		}
	}
}
