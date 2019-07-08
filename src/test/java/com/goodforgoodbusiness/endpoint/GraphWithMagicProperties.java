package com.goodforgoodbusiness.endpoint;

import static org.apache.jena.graph.NodeFactory.createLiteral;
import static org.apache.jena.graph.NodeFactory.createURI;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.core.DatasetGraphOne;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.pfunction.PFuncSimple;
import org.apache.jena.sparql.pfunction.PropertyFunction;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.sparql.resultset.ResultsFormat;

class ExamplePropertyFunctionFactory implements PropertyFunctionFactory {
    @Override
    public PropertyFunction create(final String uri) {   
        return new PFuncSimple() {
            @Override
            public QueryIterator execEvaluated(Binding parent, Node s, Node p, Node o, ExecutionContext execCtx) {   
                return QueryIterNullIterator.create(execCtx);
            	
//            	QueryIterSingleton.create(parent, var, value, execCxt);
            }
        };
    }
}

public class GraphWithMagicProperties {
	public static void main(String[] args) {
		final var reg = PropertyFunctionRegistry.chooseRegistry(ARQ.getContext());
		reg.put("urn:ex:fn#example", new ExamplePropertyFunctionFactory());
		PropertyFunctionRegistry.set(ARQ.getContext(), reg);

		var graph = new GraphMem();
		
		graph.add(new Triple(
			createURI("#ian"),
			createURI("http://xmlns.com/foaf/0.1/name"),
			createLiteral("Ian Maddison")
		));
		
		var dataset = DatasetGraphOne.create(graph);
		
//		String sparqlStmt =
//			"PREFIX apf: <java:org.apache.jena.sparql.pfunction.library.>\n" +
//			"SELECT                     \n" + 
//			"  ?s ?p ?o                 \n" + 
//			"WHERE {                    \n" + 
//			"  ?s ?p ?o                 \n" + 
//			"}                          \n"
//		;
		
		String sparqlStmt =
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
			"PREFIX apf: <java:org.apache.jena.sparql.pfunction.library.>\n" +
			"SELECT ?namespace ?localname { \n" + 
			"    ?s ?p ?o ." +
			"    ?p apf:splitIRI (?namespace ?localname) \n" + 
			"}";
		
		
		
		var query = QueryFactory.create(sparqlStmt);
		try (var exe = QueryExecutionFactory.create(query, dataset)) {
			var resultSet = exe.execSelect();
			ResultSetFormatter.output(System.out, resultSet, ResultsFormat.FMT_RS_TSV);
		}
	}
}
