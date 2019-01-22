package com.goodforgoodbusiness.rdfjava.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.Triple.Field;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.mem.GraphTripleStoreBase;
import org.apache.jena.mem.NodeToTriplesMapMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphMaker;

public class Skolemizer {
	/**
	 * Returns a temporary Dataset to use for import purposes (for example).
	 */
	public static Dataset autoSkolemizingDataset() {
		var dataGraphMaker = new DatasetGraphMaker(
			new GraphMem() {
				@Override
				protected TripleStore createTripleStore() {
					return new SkolemizingTripleStore( this );
				}
			}
		);
		
		return DatasetFactory.create(dataGraphMaker);
	}
	
	private static class SkolemizingTripleStore extends GraphTripleStoreBase implements TripleStore {
		private final Map<BlankNodeId, Node> replacements = new HashMap<>();
		
		public SkolemizingTripleStore(Graph parent) {
			super(
				parent,
	            new NodeToTriplesMapMem( Field.fieldSubject, Field.fieldPredicate, Field.fieldObject ),
	            new NodeToTriplesMapMem( Field.fieldPredicate, Field.fieldObject, Field.fieldSubject ),
	            new NodeToTriplesMapMem( Field.fieldObject, Field.fieldSubject, Field.fieldPredicate )
		    ); 
		}
		
		protected String newURI() {
			return "urn:uuid:" + UUID.randomUUID();
		}
		
		public void add(Triple trup) {
			var subject = trup.getSubject();
			var predicate = trup.getPredicate();
			var object = trup.getObject();
			
			if (subject.isBlank() || predicate.isBlank() || object.isBlank()) {
				if (subject.isBlank()) {
					subject = getOrMakeReplacement(subject.getBlankNodeId());
				}
				
				if (predicate.isBlank()) {
					predicate = getOrMakeReplacement(predicate.getBlankNodeId());
				}
				
				if (object.isBlank()) {
					object = getOrMakeReplacement(object.getBlankNodeId());
				}
				
				super.add(new Triple(subject, predicate, object));
			}
			else {
				super.add(trup);
			}
		}
		
		private Node getOrMakeReplacement(BlankNodeId id) {
			Node node = replacements.get(id);
			
			if (node == null) {
				node = NodeFactory.createURI(newURI());
				replacements.put(id, node);
			}
			
			return node;
		}
	}
}
