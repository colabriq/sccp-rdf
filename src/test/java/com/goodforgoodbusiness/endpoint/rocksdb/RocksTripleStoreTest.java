package com.goodforgoodbusiness.endpoint.rocksdb;

import static org.apache.jena.graph.NodeFactory.createLiteral;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;

import com.goodforgoodbusiness.endpoint.graph.rocks.RocksTripleStore;
import com.goodforgoodbusiness.endpoint.storage.rocks.RocksManager;
import com.goodforgoodbusiness.shared.LogConfigurer;

public class RocksTripleStoreTest {
	public static void main(String[] args) throws Exception {
		LogConfigurer.init(RocksTripleStoreTest.class, "log4j.properties");
		
		var manager = new RocksManager("/Users/ijmad/Desktop/sccp/prototype/rocks");		
		manager.start();
		
		var store = new RocksTripleStore(manager);
		
		store.add(new Triple(createLiteral("s1"), createLiteral("p1"), createLiteral("o1")));
		store.add(new Triple(createLiteral("s1"), createLiteral("p1"), createLiteral("o2")));
		store.add(new Triple(createLiteral("s1"), createLiteral("p1"), createLiteral("o3")));
		store.add(new Triple(createLiteral("s1"), createLiteral("p2"), createLiteral("o4")));
		store.add(new Triple(createLiteral("s2"), createLiteral("p2"), createLiteral("o4")));
		
		find(store, new Triple(Node.ANY, Node.ANY, Node.ANY));
		find(store, new Triple(createLiteral("s1"), Node.ANY, Node.ANY));
		find(store, new Triple(createLiteral("s1"), createLiteral("p1"), Node.ANY));
		find(store, new Triple(Node.ANY, createLiteral("p2"), Node.ANY));
		find(store, new Triple(Node.ANY, Node.ANY, createLiteral("o4")));
		find(store, new Triple(Node.ANY, Node.ANY, createLiteral("o5")));
	}
	
	public static void find(TripleStore store, Triple pattern) {
		System.out.println("--------------------");
		
		System.out.println("FIND: " + pattern);
		
		System.out.println("----------");
		
		var it = store.find(pattern);
		while (it.hasNext()) {
			var next = it.next();
			System.out.println("FOUND = " + next);
		}
		
		System.out.println("--------------------");
	}
}
