package com.colabriq.endpoint.dht.keys.impl;

import static java.util.stream.Collectors.toList;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.colabriq.endpoint.crypto.key.EncodeableShareKey;
import com.colabriq.endpoint.dht.share.SharePattern;
import com.colabriq.endpoint.dht.share.backend.impl.RocksKeyStore;
import com.colabriq.kpabe.KPABEEncryption;
import com.colabriq.kpabe.KPABEKeyManager;
import com.colabriq.rocks.RocksManager;

public class RocksKeyStoreTest {
	public static void main(String[] args) throws Exception {
		var keys = KPABEKeyManager.newKeys();
		var kpabe = KPABEEncryption.getInstance(keys);
		
		var dbm = new RocksManager("db/keys");
		dbm.start();
		
		var store = new RocksKeyStore(dbm);
		
		// make random sharekey
		var keyPair = kpabe.shareKey("foo");
		var encShareKey = new EncodeableShareKey(keyPair);
		

		
		// save a key that would cover both tuples
		store.saveKey(
			new SharePattern(
				new Triple(
					createURI("https://twitter.com/ijmad8x"),
					Node.ANY,
					Node.ANY
				)
			),
			encShareKey
		);
		
		System.out.println("----------------------------------------");
		
		// check share key is returned when searching for right things
		var tt1 = new Triple(
			createURI("https://twitter.com/ijmad8x"),
			createURI("http://xmlns.com/foaf/0.1/name"),
			createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
		);
		
		store.getCreators(tt1).forEach(r -> {
			try {
				System.out.println(r);
				System.out.println("⇒" + store.getKeys(r, tt1).collect(toList()));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		System.out.println("----------------------------------------");
		
		var tt2 = new Triple(
			createURI("https://twitter.com/ijmad9x"),
			createURI("http://xmlns.com/foaf/0.1/age"),
			createLiteralNode("35", null, "http://www.w3.org/2001/XMLSchema/integer")
		);
		
		// check key is not returned when searching for wrong things
		store.getCreators(tt2).forEach(r -> {
			try {
				System.out.println(r);
				System.out.println("⇒" + store.getKeys(r, tt2).collect(toList()));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		System.out.println("----------------------------------------");
		
		var tt3 = new Triple(
			createURI("https://twitter.com/ijmad8x"),
			createURI("http://xmlns.com/foaf/0.1/name"),
			Node.ANY
		);
		
		// check narrower but partial searches
		store.getCreators(tt3).forEach(r -> {
			try {
				System.out.println(r);
				System.out.println("⇒" + store.getKeys(r, tt3).collect(toList()));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		System.out.println("----------------------------------------");
		
		var tt4 = new Triple(
			createURI("https://twitter.com/ijmad8x"),
			Node.ANY,
			Node.ANY
		);
		
		store.getCreators(tt4).forEach(r -> {
			try {
				System.out.println(r);
				System.out.println("⇒" + store.getKeys(r, tt4).collect(toList()));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		System.out.println("----------------------------------------");
	}
}
