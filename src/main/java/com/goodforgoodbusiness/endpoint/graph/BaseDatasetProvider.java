package com.goodforgoodbusiness.endpoint.graph;

import static com.github.jsonldjava.shaded.com.google.common.collect.Iterators.concat;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Wrap graphs in to {@link Dataset}
 * @author ijmad
 */
@Singleton
public class BaseDatasetProvider implements Provider<Dataset> {
	/**
	 * Refers to the graph representing preloaded data
	 */
	@BindingAnnotation @Target({ PARAMETER, METHOD, FIELD }) @Retention(RUNTIME)
	public static @interface Preloaded {
		// annotation, no content
	}
	
	/**
	 * Refers to the graph representing data retrieved from DHT
	 */
	@BindingAnnotation @Target({ PARAMETER, METHOD, FIELD }) @Retention(RUNTIME)
	public static @interface Fetched {
		// annotation, no content
	}
	
	/**
	 * Refers to the graph of inference
	 */
	@BindingAnnotation @Target({ PARAMETER, METHOD, FIELD }) @Retention(RUNTIME)
	public static @interface Inferred {
		// annotation, no content
	}
	
	/**
	 * Create a union over some graphs 
	 */
	public static Graph newUnionView(Graph head, Graph... others) {
		var union = new MultiUnion(concat(singleton(head).iterator(), asList(others).iterator()));
		union.setBaseGraph(head);
		return union;
	}

	protected final Graph defaultGraph;
	
	@Inject
	public BaseDatasetProvider(@Preloaded Graph preloadedGraph, @Fetched Graph fetchedGraph, @Inferred Graph inferredGraph) {
		this.defaultGraph = newUnionView(fetchedGraph, preloadedGraph, inferredGraph);
	}
	
	@Override
	public Dataset get() {
		return DatasetFactory.wrap(
			new BaseDatasetGraph(
				defaultGraph,
				// doing nothing for the base dataset
				new GraphMaker() {
					@Override
					public Graph create(Node name) {
						return null;
					}
				}
			)
		);
	}
}
