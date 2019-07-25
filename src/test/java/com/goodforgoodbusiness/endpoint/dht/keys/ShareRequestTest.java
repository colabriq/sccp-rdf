package com.goodforgoodbusiness.endpoint.dht.keys;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.endpoint.dht.share.ShareResponse;
import com.goodforgoodbusiness.kpabe.KPABEEncryption;
import com.goodforgoodbusiness.kpabe.KPABEKeyManager;
import com.goodforgoodbusiness.shared.encode.JSON;

public class ShareRequestTest {
	public static void main(String[] args) throws Exception {
		var keys = KPABEKeyManager.newKeys();
		var kpabe = KPABEEncryption.getInstance(keys);
		var esk = new EncodeableShareKey(kpabe.shareKey("foo"));
		
		var t1 = new Triple(
			createURI("https://twitter.com/ijmad8x"),
			createURI("http://xmlns.com/foaf/0.1/name"),
			createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
		);
		
		var sr1 = new ShareResponse().setTriple(t1);
		sr1.setKey(esk);
		
		var j1 = JSON.encodeToString(sr1);
		System.out.println(j1);
		
		var sr1a = JSON.decode(j1, ShareResponse.class);
		System.out.println(sr1a);
		
		var t2 = new Triple(
			createURI("https://twitter.com/ijmad8x"),
			Node.ANY,
			Node.ANY
		);
		
		var sr2 = new ShareResponse().setTriple(t2);
		sr2.setKey(esk);
		
		var j2 = JSON.encodeToString(sr2);
		System.out.println(j2);
		
		var sr2a = JSON.decode(j2, ShareResponse.class);
		System.out.println(sr2a);
	}
}
