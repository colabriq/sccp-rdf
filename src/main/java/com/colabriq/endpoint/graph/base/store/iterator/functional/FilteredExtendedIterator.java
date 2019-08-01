package com.colabriq.endpoint.graph.base.store.iterator.functional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.jena.util.iterator.ExtendedIterator;

public class FilteredExtendedIterator<T> implements DefaultExtendedIterator<T> {
	private final ExtendedIterator<? extends T> iterator;
	private final Predicate<? super T> predicate;
	
	private T next = null;
	
	public FilteredExtendedIterator(ExtendedIterator<? extends T> iterator, Predicate<? super T> predicate) {
		this.iterator = iterator;
		this.predicate = predicate;
		replaceNext();
	}
	
	private void replaceNext() {
		next = null;
		
		try {
			while (iterator.hasNext()) {
				var candidate = iterator.next();
				if (predicate.test(candidate)) {
					next = candidate;
					break;
				}
			}
		}
		catch (NoSuchElementException e) {
			// do nothing, as next is already null
		}
	}
	
	@Override
	public void close() {
		next = null;
		iterator.close();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public T next() {
		var current = this.next;
		if (current != null) {
			replaceNext();
			return current;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public T removeNext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<T> toList() {
		return iterator.toList().stream().filter(predicate).collect(Collectors.toList());
	}

	@Override
	public Set<T> toSet() {
		return iterator.toSet().stream().filter(predicate).collect(Collectors.toSet());
	}
}
