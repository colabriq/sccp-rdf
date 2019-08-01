package com.colabriq.endpoint.graph.base.store.iterator.functional;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.jena.util.iterator.ClosableIterator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class WrappedExtendedIterator<T> implements DefaultExtendedIterator<T> {
	private final Iterator<? extends T> it;

	public WrappedExtendedIterator(Iterator<? extends T> it) {
		this.it = it;
	}
	
	@Override
	public void close() {
		if (it instanceof ClosableIterator) {
			((ClosableIterator<?>)it).close();
		}
		
		try {
			if (it instanceof AutoCloseable) {
				((AutoCloseable)it).close();
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Close failed", e);
		}
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public T next() {
		return it.next();
	}

	@Override
	public T removeNext() {
		if (it.hasNext()) {
			var next = it.next();
			it.remove();
			return next;
		}
		
		throw new NoSuchElementException();
	}

	@Override
	public List<T> toList() {
		return Lists.newArrayList(it);
	}

	@Override
	public Set<T> toSet() {
		return Sets.newHashSet(it);
	}
}
