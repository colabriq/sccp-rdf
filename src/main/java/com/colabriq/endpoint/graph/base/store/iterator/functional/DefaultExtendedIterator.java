package com.colabriq.endpoint.graph.base.store.iterator.functional;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.util.iterator.ExtendedIterator;

public interface DefaultExtendedIterator<T> extends ExtendedIterator<T> {
	@Override
	default public <TT extends T> ExtendedIterator<T> andThen(Iterator<TT> other) {
		return new ConcatExtendedIterator<>(this, new WrappedExtendedIterator<>(other));
	}

	@Override
	default public ExtendedIterator<T> filterKeep(Predicate<T> f) {
		return new FilteredExtendedIterator<>(this, f);
	}

	@Override
	default public ExtendedIterator<T> filterDrop(Predicate<T> f) {
		return new FilteredExtendedIterator<>(this, x -> !f.test(x));
	}

	@Override
	default public <Y> ExtendedIterator<Y> mapWith(Function<T, Y> nextMapper) {
		return new MappedExtendedIterator<>(this, nextMapper);
	}
}
