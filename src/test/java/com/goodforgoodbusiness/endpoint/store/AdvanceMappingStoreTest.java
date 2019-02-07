package com.goodforgoodbusiness.endpoint.store;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.rdf.store.AdvanceMappingStore;

public class AdvanceMappingStoreTest {
	public static void main(String[] args) {
		var store = new AdvanceMappingStore();
		
		var t1 = new Triple(
			NodeFactory.createURI("urn:s1"),
			NodeFactory.createURI("urn:predicateA"),
			NodeFactory.createURI("urn:o1")
		);
		
		var t2 = new Triple(
			NodeFactory.createURI("urn:s2"),
			NodeFactory.createURI("urn:predicateA"),
			NodeFactory.createURI("urn:o2")
		);
		
		var t3 = new Triple(
			NodeFactory.createURI("urn:s3"),
			NodeFactory.createURI("urn:predicateB"),
			NodeFactory.createURI("urn:o3")
		);
		
		var t4 = new Triple(
			NodeFactory.createURI("urn:s4"),
			NodeFactory.createURI("urn:predicateB"),
			NodeFactory.createURI("urn:o4")
		);
		
		var t5 = new Triple(
			NodeFactory.createURI("urn:s5"),
			NodeFactory.createURI("urn:predicateC"),
			NodeFactory.createURI("urn:o5")
		);
		
		store.add(t1);
		store.add(t2);
		store.add(t3);
		store.add(t4);
		store.add(t5);
		
		var pattern1 = new Triple(Node.ANY, Node.ANY, Node.ANY);
		System.out.println("size = " + store.size());
		System.out.println("pattern = " + pattern1);
		for (var triple : store.find(pattern1).toList()) {
			System.out.println(triple);
		}
		
		System.out.println();
		
		var pattern2 = new Triple(Node.ANY, NodeFactory.createURI("urn:predicateB"), Node.ANY);
		System.out.println("size = " + store.size());
		System.out.println("pattern = " + pattern2);
		for (var triple : store.find(pattern2).toList()) {
			System.out.println(triple);
		}
		
		System.out.println();
		
		store.delete(t3);
		
		var pattern3 = new Triple(Node.ANY, Node.ANY, Node.ANY);
		System.out.println("size = " + store.size());
		System.out.println("pattern = " + pattern3);
		for (var triple : store.find(pattern3).toList()) {
			System.out.println(triple);
		}
		
		System.out.println();
		
		var pattern4 = new Triple(Node.ANY, NodeFactory.createURI("urn:predicateB"), Node.ANY);
		System.out.println("size = " + store.size());
		System.out.println("pattern = " + pattern4);
		for (var triple : store.find(pattern4).toList()) {
			System.out.println(triple);
		}
		
		System.out.println();
	}
}
