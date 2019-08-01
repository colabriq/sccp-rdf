package com.goodforgoodbusiness.endpoint.dht.keys.impl;

import static java.util.stream.Collectors.toList;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.endpoint.dht.share.SharePattern;
import com.goodforgoodbusiness.endpoint.dht.share.backend.impl.MemKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEEncryption;
import com.goodforgoodbusiness.kpabe.KPABEKeyManager;

public class MemKeyStoreTest {
	public static void main(String[] args) throws Exception {
		var keys = KPABEKeyManager.newKeys();
		var kpabe = KPABEEncryption.getInstance(keys);
		
		// make random sharekey
		var keyPair = kpabe.shareKey("foo");
		var encShareKey = new EncodeableShareKey(keyPair);		
		var store = new MemKeyStore();
		
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
			System.out.println(r);
			System.out.println("⇒" + store.getKeys(r, tt1).collect(toList()));
		});
		
		System.out.println("----------------------------------------");
		
		var tt2 = new Triple(
			createURI("https://twitter.com/ijmad9x"),
			createURI("http://xmlns.com/foaf/0.1/age"),
			createLiteralNode("35", null, "http://www.w3.org/2001/XMLSchema/integer")
		);
		
		// check key is not returned when searching for wrong things
		store.getCreators(tt2).forEach(r -> {
			System.out.println(r);
			System.out.println("⇒" + store.getKeys(r, tt2).collect(toList()));
		});
		
		System.out.println("----------------------------------------");
		
		var tt3 = new Triple(
			createURI("https://twitter.com/ijmad8x"),
			createURI("http://xmlns.com/foaf/0.1/name"),
			Node.ANY
		);
		
		// check narrower but partial searches
		store.getCreators(tt3).forEach(r -> {
			System.out.println(r);
			System.out.println("⇒" + store.getKeys(r, tt3).collect(toList()));
		});
		
		System.out.println("----------------------------------------");
		
		var tt4 = new Triple(
			createURI("https://twitter.com/ijmad8x"),
			Node.ANY,
			Node.ANY
		);
		
		store.getCreators(tt4).forEach(r -> {
			System.out.println(r);
			System.out.println("⇒" + store.getKeys(r, tt4).collect(toList()));
		});
		
		System.out.println("----------------------------------------");
	}
}
