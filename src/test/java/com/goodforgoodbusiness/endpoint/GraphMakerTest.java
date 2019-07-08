package com.goodforgoodbusiness.endpoint;

import static org.apache.jena.graph.NodeFactory.createURI;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;
import org.apache.jena.sparql.resultset.ResultsFormat;

import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetGraph;
import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;

public class GraphMakerTest {
	public static void main(String[] args) {
		var mainGraph = BaseGraph.newGraph();
		
		mainGraph.add(new Triple(
			createURI("urn:s1"),
			createURI("urn:p1"),
			createURI("urn:o1")
		));
		
		mainGraph.add(new Triple(
			createURI("urn:s2"),
			createURI("urn:p2"),
			createURI("urn:o2")
		));

		mainGraph.add(new Triple(
			createURI("urn:s3"),
			createURI("urn:p3"),
			createURI("urn:o3")
		));
		
		
		
		var contGraph = BaseGraph.newGraph();
		
		contGraph.add(new Triple(
			createURI("urn:cs1"),
			createURI("urn:cp1"),
			createURI("urn:co1")
		));
		
		contGraph.add(new Triple(
			createURI("urn:cs2"),
			createURI("urn:cp2"),
			createURI("urn:co2")
		));

		contGraph.add(new Triple(
			createURI("urn:cs3"),
			createURI("urn:cp3"),
			createURI("urn:co3")
		));		
		
//		var dataset =
//			DatasetFactory.wrap(
//				new DatasetGraphOverlay(
//					mainGraph
//				)
//			)
//		;
		
		BaseDatasetGraph g = new BaseDatasetGraph(
			mainGraph,
			new GraphMaker() {
			@Override
				public Graph create(Node name) {
					if (name.getURI().equals("container:abc")) {
						return contGraph;
					}
					
					return null;
				}
			}
		);
		
		var dataset = DatasetFactory.wrap(g);
		
		
		query(dataset, "SELECT ?s ?p ?o WHERE { ?s ?p ?o }");
		
		query(dataset, 
			"SELECT ?subject ?predicate ?object  " + 
			"WHERE {                             " + 
			"    GRAPH <container:abc> {         " + 
			"        ?subject ?predicate ?object " + 
			"    }                               " + 
			"}                                   "
		);
	}
	
	private static void query(Dataset dataset, String queryStmt) {
		var query = QueryFactory.create(queryStmt);
		var exe = QueryExecutionFactory.create(query, dataset);
			
		var resultSet = exe.execSelect();
			
		ResultSetFormatter.output(System.out, resultSet, ResultsFormat.FMT_RS_XML);
		System.out.println("--------------------");
	}
}
