package com.goodforgoodbusiness.endpoint.rdf.store.extendediterator.functional;

import java.util.List;

import com.goodforgoodbusiness.endpoint.graph.base.store.iterator.functional.MultiExtendedIterator;
import com.goodforgoodbusiness.endpoint.graph.base.store.iterator.functional.WrappedExtendedIterator;

public class MultiExtendedIteratorTest {
	public static void main(String[] args) {
		var exi1 = new WrappedExtendedIterator<>(List.of("1", "2", "3").iterator());
		var exi2 = new WrappedExtendedIterator<>(List.of("a", "b", "c").iterator());
		var exi3 = new WrappedExtendedIterator<>(List.of("$", "%", "#").iterator());
		
		var mexi = new MultiExtendedIterator<>(exi1);
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		mexi.add(exi2);
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		mexi.add(exi3);
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		System.out.println(mexi.hasNext());
		System.out.println(mexi.next());
		
		
	}
}
