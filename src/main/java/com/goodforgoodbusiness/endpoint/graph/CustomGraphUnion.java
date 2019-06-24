package com.goodforgoodbusiness.endpoint.graph;

import static com.github.jsonldjava.shaded.com.google.common.collect.Iterators.concat;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;

/**
 * Tweaks to {@link MultiUnion} in the initialization phase + 
 * custom size that doesn't call find(ANY, ANY ANY) on the underlying graphs.
 *
 */
public class CustomGraphUnion extends MultiUnion {
	/**
	 * Create a union over some graphs 
	 */
	public CustomGraphUnion(Graph head, Graph... others) {
		super(concat(singleton(head).iterator(), asList(others).iterator()));
		setBaseGraph(head);
	}
	
	@Override
	protected int graphBaseSize() {
		return m_subGraphs
			.stream()
			.map(Graph::size)
			.reduce(Integer::sum)
			.orElse(0)
		;
	}
}
