package com.goodforgoodbusiness.endpoint.plugin;

import org.apache.jena.graph.Graph;

/**
 * Task that runs a particular graph listener against a container
 */
class GraphListenerTriggerTask implements Runnable {
	private final Graph graph;
	private final GraphListener listener;
	
	GraphListenerTriggerTask(Graph graph, GraphListener listener) {
		this.graph = graph;
		this.listener = listener;
	}

	@Override
	public void run() {
		listener.newGraph(graph);
	}
}
