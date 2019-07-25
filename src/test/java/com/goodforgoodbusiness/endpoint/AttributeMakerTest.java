package com.goodforgoodbusiness.endpoint;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.dht.share.ShareRequest;
import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerAttributes;
import com.goodforgoodbusiness.kpabe.KPABEKeyManager;

public class AttributeMakerTest {
	public static void main(String[] args) throws Exception {
		var keys = KPABEKeyManager.newKeys();
		
		String attributes = ContainerAttributes.forPublish(
			keys.getPublic(),
			Stream.of(
				new Triple(
					createURI("https://twitter.com/ijmad8x"),
					createURI("http://xmlns.com/foaf/0.1/name"),
					createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")					
				),
				
				new Triple(
					createURI("https://twitter.com/ijmad8x"),
					createURI("http://xmlns.com/foaf/0.1/age"),
					createLiteralNode("35", null, "http://www.w3.org/2001/XMLSchema/integer")
				)
			)
		);
		
		System.out.println(attributes);
		
		var r1 = new ShareRequest();
		r1.setTriple(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				createURI("http://xmlns.com/foaf/0.1/name"),
				createLiteralNode("Ian Maddison", null, "http://www.w3.org/2001/XMLSchema/string")
			)
		);
		
		var share1 = ContainerAttributes.forShare(keys.getPublic(), r1);
		System.out.println(share1);
		
		var r2 = new ShareRequest();
		r2.setTriple(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				createURI("http://xmlns.com/foaf/0.1/name"),
				Node.ANY
			)
		);
		
		var share2 = ContainerAttributes.forShare(keys.getPublic(), r2);
		System.out.println(share2);
		
		
		var r3 = new ShareRequest();
		
		r3.setTriple(
			new Triple(
				createURI("https://twitter.com/ijmad8x"),
				createURI("http://xmlns.com/foaf/0.1/name"),
				Node.ANY
			)
		);
		
		r3.setStart(ZonedDateTime.now());
		r3.setEnd(ZonedDateTime.now().plusDays(1));
		
		var share3 = ContainerAttributes.forShare(keys.getPublic(), r3);
		System.out.println(share3);
	}
}
