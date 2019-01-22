package com.goodforgoodbusiness.rdfjava.dht;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.shared.model.StoredClaim;

public class DHTTest {
	public static void main(String[] args) throws Exception {
		List<StoredClaim> claims = DHTClient.matches(
			new Triple(
				NodeFactory.createURI("https://twitter.com/ijmad8x"),
				NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"), // NodeFactory.createLiteral("foo"),
				Node.ANY
			)
		);
		
		for (StoredClaim c : claims) {		
			System.out.println(c);
		}
	}
}
