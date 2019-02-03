package com.goodforgoodbusiness.endpoint.rdf.store.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.jena.util.iterator.ExtendedIterator;

class MappedIterator<X, T> implements ExtendedIterator<T> {
	private final ExtendedIterator<X> iterator;
	private final Function<X, T> mapper;
	
	MappedIterator(ExtendedIterator<X> iterator, Function<X, T> mapper) {
		this.iterator = iterator;
		this.mapper = mapper;
	}
	
	@Override
	public void close() {
		iterator.close();
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		return mapper.apply(iterator.next());
	}

	@Override
	public T removeNext() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <TT extends T> ExtendedIterator<T> andThen(Iterator<TT> other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ExtendedIterator<T> filterKeep(Predicate<T> f) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ExtendedIterator<T> filterDrop(Predicate<T> f) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <Y> ExtendedIterator<Y> mapWith(Function<T, Y> mapper) {
		return new MappedIterator<T, Y>(this, mapper);
	}

	@Override
	public List<T> toList() {
		return iterator.toList().stream().map(mapper).collect(Collectors.toList());
	}

	@Override
	public Set<T> toSet() {
		return iterator.toSet().stream().map(mapper).collect(Collectors.toSet());
	}
}
