package com.goodforgoodbusiness.endpoint.dht.keys.impl;

import static java.util.stream.Collectors.toList;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.endpoint.dht.share.impl.RocksKeyStore;
import com.goodforgoodbusiness.kpabe.KPABEEncryption;
import com.goodforgoodbusiness.kpabe.KPABEKeyManager;
import com.goodforgoodbusiness.rocks.RocksManager;

public class RocksKeyStoreTest {
	public static void main(String[] args) throws Exception {
		var dbm = new RocksManager("db/keys");
		dbm.start();
		
		var store = new RocksKeyStore(dbm);
		
		
		var keys = KPABEKeyManager.newKeys();
		var kpabe = KPABEEncryption.getInstance(keys);
		
		// make random sharekey
		var keyPair = kpabe.shareKey("foo");
		var encShareKey = new EncodeableShareKey(keyPair);
		

		
		// save a key that would cover both tuples
		store.saveKey(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				Node.ANY,
				Node.ANY
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
		
		store.knownContainerCreators(tt1).forEach(r -> {
			try {
				System.out.println(r);
				System.out.println("⇒" + store.keysForDecrypt(r, tt1).collect(toList()));
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
		store.knownContainerCreators(tt2).forEach(r -> {
			try {
				System.out.println(r);
				System.out.println("⇒" + store.keysForDecrypt(r, tt2).collect(toList()));
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
		store.knownContainerCreators(tt3).forEach(r -> {
			try {
				System.out.println(r);
				System.out.println("⇒" + store.keysForDecrypt(r, tt3).collect(toList()));
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
		
		store.knownContainerCreators(tt4).forEach(r -> {
			try {
				System.out.println(r);
				System.out.println("⇒" + store.keysForDecrypt(r, tt4).collect(toList()));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		System.out.println("----------------------------------------");
	}
}
