package com.colabriq.endpoint.rdf.store.extendediterator.functional;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import com.colabriq.endpoint.graph.base.store.iterator.DequeExtendedIterator;
import com.colabriq.endpoint.graph.base.store.iterator.functional.WrappedExtendedIterator;

public class ExtendedIteratorTest {
	public static void main(String[] args) throws Exception {
		var it0 = new WrappedExtendedIterator<>(Arrays.asList("1", "2", "3").iterator());
		
		var it1 = new DequeExtendedIterator<>(Stream.of("a", "b", "c", "dee", "e", "f"));
		var it2 = it1.filterKeep(x -> x.length() == 1);
		
		var it3 = it2.mapWith(x -> x.toUpperCase());
		var it4 = it3.filterDrop(x -> x.equals("B"));
		
		var it5 = it4.andThen(it0);
		
		while (it5.hasNext()) {
			System.out.println(it5.next());
		}
		
		try {
			it5.next(); // should generate a NoSuchElementException
			throw new Exception("Nope");
		}
		catch (NoSuchElementException e) {
			System.out.println("Yup!");
		}
	}
}
