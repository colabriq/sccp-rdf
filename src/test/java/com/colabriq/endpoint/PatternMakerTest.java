package com.colabriq.endpoint;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.sparql.util.NodeFactoryExtra.createLiteralNode;

import java.util.stream.Collectors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.colabriq.endpoint.graph.containerized.ContainerPatterns;
import com.colabriq.kpabe.KPABEKeyManager;

public class PatternMakerTest {
	public static void main(String[] args) throws Exception {
		var keys = KPABEKeyManager.newKeys();
		
		System.out.println(
			ContainerPatterns.forPublish(
				keys.getPublic(),
				new Triple(
					createURI("https://twitter.com/ijmad8x"),
					createURI("http://xmlns.com/foaf/0.1/name"),
					createLiteralNode("Hello", null, "http://www.w3.org/2001/XMLSchema/string")
				)
			).collect(Collectors.toSet())
		);
		
		System.out.println(
			ContainerPatterns.forSearch(
				keys.getPublic(),
				
					new Triple(
						createURI("https://twitter.com/ijmad8x"),
						createURI("http://xmlns.com/foaf/0.1/name"),
						Node.ANY
					)
			)
		);
		
		System.out.println(
			ContainerPatterns.forSearch(
				keys.getPublic(),
				new Triple(
					createURI("https://twitter.com/ijmad8x"),
					Node.ANY,
					Node.ANY
				)
			)
		);
		
		System.out.println(
			ContainerPatterns.forSearch(
				keys.getPublic(),
				new Triple(
					Node.ANY,
					Node.ANY,
					createLiteralNode("Hello", null, "http://www.w3.org/2001/XMLSchema/string")
				)
			)
		);
	}
}
