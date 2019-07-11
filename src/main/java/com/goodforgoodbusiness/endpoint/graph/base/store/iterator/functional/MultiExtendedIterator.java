package com.goodforgoodbusiness.endpoint.graph.base.store.iterator.functional;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * An iterator to which other iterators can be concatenated while already open
 */
public class MultiExtendedIterator<T> implements ExtendedIterator<T> {
	private final Deque<ExtendedIterator<T>> iterators;
	private ExtendedIterator<T> current;
	
	public MultiExtendedIterator(ExtendedIterator<T> first) {		
		this.current = first;
		this.iterators = new LinkedList<>();
	}
	
	public void add(ExtendedIterator<T> iterator) {
		// test to see if it has at least 1
		if (iterator.hasNext()) {
			this.iterators.addLast(iterator);
		}
	}
	
	@Override
	public void close() {
		if (current != null) {
			current.close();
		}
		
		while (!iterators.isEmpty()) {
			iterators.pop().close();
		}
	}

	@Override
	public boolean hasNext() {
		while (true) {
			if ((current != null) && current.hasNext()) {
				return true;
			}
			else if (!iterators.isEmpty()) {
				current = iterators.pop();
			}
			else {
				break;
			}
		}
		
		return false;
	}

	@Override
	public T next() {
		while (true) {
			if ((current != null) && current.hasNext()) {
				return current.next();
			}
			else if(!iterators.isEmpty()) {
				current = iterators.pop();
			}
			else {
				break;
			}
		}
		
		throw new NoSuchElementException();
	}
	
	@Override
	public T removeNext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <TT extends T> ExtendedIterator<T> andThen(Iterator<TT> other) {
		return new ConcatExtendedIterator<>(this, new WrappedExtendedIterator<>(other));
	}

	@Override
	public ExtendedIterator<T> filterKeep(Predicate<T> f) {
		return new FilteredExtendedIterator<>(this, f);
	}

	@Override
	public ExtendedIterator<T> filterDrop(Predicate<T> f) {
		return new FilteredExtendedIterator<>(this, x -> !f.test(x));
	}

	@Override
	public <Y> ExtendedIterator<Y> mapWith(Function<T, Y> mapper) {
		return new MappedExtendedIterator<>(this, mapper);
	}

	@Override
	public List<T> toList() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<T> toSet() {
		throw new UnsupportedOperationException();
	}
}
