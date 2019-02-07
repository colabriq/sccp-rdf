package com.goodforgoodbusiness.endpoint.store.iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.rdf.store.AdvanceMappingStore;

public class TripleIteratorTest {
	public static void main(String[] args) throws Exception {
		var store1 = new AdvanceMappingStore();
		
		store1.add(
			new Triple(
				NodeFactory.createURI("urn:s1"),
				NodeFactory.createURI("urn:predicate"),
				NodeFactory.createURI("urn:o1")
			)
		);
		
		store1.add(
			new Triple(
				NodeFactory.createURI("urn:s2"),
				NodeFactory.createURI("urn:predicate"),
				NodeFactory.createURI("urn:o2")
			)
		);
		
		var iter1 = store1.find(new Triple(Node.ANY, NodeFactory.createURI("urn:predicate"), Node.ANY));
		
		System.out.println(iter1.hasNext() + " -> " + iter1.next());
		System.out.println(iter1.hasNext() + " -> " + iter1.next());

		// add another triple that would have matched the pattern
		
		store1.add(
			new Triple(
				NodeFactory.createURI("urn:s3"),
				NodeFactory.createURI("urn:predicate"),
				NodeFactory.createURI("urn:o3")
			)
		);
		
		System.out.println(iter1.hasNext() + " -> " + iter1.next());
		System.out.println(iter1.hasNext());
		
		// try a removal test
		
		var store2 = new AdvanceMappingStore();
		
		store2.add(
			new Triple(
				NodeFactory.createURI("urn:s1"),
				NodeFactory.createURI("urn:predicate"),
				NodeFactory.createURI("urn:o1")
			)
		);
		
		store2.add(
			new Triple(
				NodeFactory.createURI("urn:s2"),
				NodeFactory.createURI("urn:predicate"),
				NodeFactory.createURI("urn:o2")
			)
		);
		
		store2.add(
			new Triple(
				NodeFactory.createURI("urn:s3"),
				NodeFactory.createURI("urn:predicate"),
				NodeFactory.createURI("urn:o3")
			)
		);

		var iter2 = store2.find(new Triple(Node.ANY, NodeFactory.createURI("urn:predicate"), Node.ANY));
		
		System.out.println(iter2.hasNext() + " -> " + iter2.next());
		
		store2.delete(
			new Triple(
				NodeFactory.createURI("urn:s2"),
				NodeFactory.createURI("urn:predicate"),
				NodeFactory.createURI("urn:o2")
			)
		);
		
		System.out.println(iter2.hasNext() + " -> " + iter2.next());
		System.out.println(iter2.hasNext());
	}
}
