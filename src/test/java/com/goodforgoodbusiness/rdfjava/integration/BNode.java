package com.goodforgoodbusiness.rdfjava.integration;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class BNode {
	public static Node blankNodeToIri(Node node) {
        if ( node.isBlank() ) {
            String x = node.getBlankNodeLabel() ;
            return NodeFactory.createURI("_:" + x) ;
        }
        return node;
    }
	
	public static void main(String[] args) throws Exception {
		var n = NodeFactory.createBlankNode("foo");
		
		System.out.println(n);
		System.out.println(blankNodeToIri(n));
	}
}
