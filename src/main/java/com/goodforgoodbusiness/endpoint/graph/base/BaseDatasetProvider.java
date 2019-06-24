package com.goodforgoodbusiness.endpoint.graph.base;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.jena.query.DatasetFactory.wrap;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;

import com.goodforgoodbusiness.endpoint.graph.CustomGraphUnion;
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

	private final Graph preloadedGraph, fetchedGraph, inferredGraph;
	
	@Inject
	public BaseDatasetProvider(@Preloaded Graph preloadedGraph, @Fetched Graph fetchedGraph, @Inferred Graph inferredGraph) {
		this.preloadedGraph = preloadedGraph;
		this.fetchedGraph = fetchedGraph;
		this.inferredGraph = inferredGraph;
	}
	
	@Override
	public Dataset get() {
		return wrap(
			new BaseDatasetGraph(
				new CustomGraphUnion(
					fetchedGraph,
					preloadedGraph,
					inferredGraph
				),
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
