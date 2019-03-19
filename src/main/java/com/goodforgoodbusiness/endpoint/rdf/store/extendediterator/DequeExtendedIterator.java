package com.goodforgoodbusiness.endpoint.rdf.store.extendediterator;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.jena.util.iterator.ExtendedIterator;

import com.goodforgoodbusiness.endpoint.rdf.store.extendediterator.functional.FilteredExtendedIterator;
import com.goodforgoodbusiness.endpoint.rdf.store.extendediterator.functional.MappedExtendedIterator;

/**
 * Iterator based on a deque.
 * This is so triples download from the DHT in response to subqueries are reflected immediately. 
 * This will cause the stream to get immeidately iterated, so it's not that efficient, but necessary.
 */
public class DequeExtendedIterator<T> implements ExtendedIterator<T> {
	protected final Deque<T> deque;
	
	protected final Set<T> seen = new HashSet<>();
	protected final Set<T> skip = new HashSet<>();
	
	private boolean closed = false;

	/**
	 * Copy the provided Set in to the deque. 
	 * Subsequent adds/removes will be appended.
	 */
	public DequeExtendedIterator(Stream<T> items) {
		this.deque = new LinkedList<>();
		items.forEach(deque::add);
	}
	
	public DequeExtendedIterator(Collection<T> items) {
		this.deque = new LinkedList<>(items);
	}
	
	private void checkClosed() {
		if (closed) {
			throw new UnsupportedOperationException("Can't operate on a closed Iterator");
		}
	}
	
	public void append(T element) {
		checkClosed();
		
		if (!seen.contains(element)) {
			deque.addLast(element);
		}
	}
	
	public void skip(T element) {	
		checkClosed();
		
		if (seen.contains(element)) {
			throw new IllegalArgumentException("Cannot skip element already consumed");
		}
		
		skip.add(element);
	}

	@Override
	public boolean hasNext() {
		checkClosed();
		
		while(!deque.isEmpty()) {
			var head = deque.peek();
			if (seen.contains(head) || skip.contains(head)) {
				deque.removeFirst();
			}
			else {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public T next() {
		checkClosed();
		
		while(!deque.isEmpty()) {
			var head = deque.removeFirst();
			if (!seen.contains(head) && !skip.contains(head)) {
				seen.add(head);
				return head;
			}
		}
		
		throw new NoSuchElementException();
	}
	
	@Override
	public void close() {
		deque.clear();
		
		seen.clear();
		skip.clear();
		
		closed = true;
	}

	@Override
	public T removeNext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <X extends T> ExtendedIterator<T> andThen(Iterator<X> other) {
		throw new UnsupportedOperationException();
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
		checkClosed();
		
		return new MappedExtendedIterator<>(this, mapper);
	}

	@Override
	public List<T> toList() {
		checkClosed();
		
		var list = new LinkedList<T>();
		
		list.addAll(seen);
		list.addAll(deque);
		
		return list;
	}

	@Override
	public Set<T> toSet() {
		checkClosed();
		
		var set = new HashSet<T>();
		
		set.addAll(seen);
		set.addAll(deque);
		
		return set;
	}
}
