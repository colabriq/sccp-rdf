package com.goodforgoodbusiness.endpoint.rdf.store.extendediterator.functional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.util.iterator.ExtendedIterator;

import com.github.jsonldjava.shaded.com.google.common.collect.Streams;

public class ConcatExtendedIterator<T> implements DefaultExtendedIterator<T> {
	private final ExtendedIterator<? extends T> iterator1, iterator2;
	
	public ConcatExtendedIterator(ExtendedIterator<? extends T> iterator1, ExtendedIterator<? extends T> iterator2) {
		this.iterator1 = iterator1;
		this.iterator2 = iterator2;
	}
	
	@Override
	public void close() {
		iterator1.close();
		iterator2.close();
	}

	@Override
	public boolean hasNext() {
		return iterator1.hasNext() || iterator2.hasNext();
	}

	@Override
	public T next() {
		if (iterator1.hasNext()) {
			return iterator1.next();
		}
		else if (iterator2.hasNext()) {
			return iterator2.next();
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
		return 
			Streams.concat(
				iterator1.toList().stream(),
				iterator2.toList().stream()
			)
			.collect(Collectors.toList())
		;
	}

	@Override
	public Set<T> toSet() {
		return 
			Streams.concat(
				iterator1.toSet().stream(),
				iterator2.toSet().stream()
			)
			.collect(Collectors.toSet())
		;
	}
}
