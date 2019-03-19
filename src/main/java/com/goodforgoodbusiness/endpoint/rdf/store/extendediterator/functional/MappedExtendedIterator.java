package com.goodforgoodbusiness.endpoint.rdf.store.extendediterator.functional;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.util.iterator.ExtendedIterator;

public class MappedExtendedIterator<X, T> implements DefaultExtendedIterator<T> {
	private final ExtendedIterator<X> iterator;
	private final Function<X, T> mapper;
	
	public MappedExtendedIterator(ExtendedIterator<X> iterator, Function<X, T> mapper) {
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
	public List<T> toList() {
		return iterator.toList().stream().map(mapper).collect(Collectors.toList());
	}

	@Override
	public Set<T> toSet() {
		return iterator.toSet().stream().map(mapper).collect(Collectors.toSet());
	}
}
