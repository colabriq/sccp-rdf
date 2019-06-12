package com.goodforgoodbusiness.endpoint.dht.container;

import org.apache.jena.graph.Graph;

import com.goodforgoodbusiness.endpoint.graph.BaseGraph;
import com.goodforgoodbusiness.model.SubmitResult;
import com.goodforgoodbusiness.model.SubmittableContainer;
import com.goodforgoodbusiness.model.SubmittedContainer;

public class SubmittedGraphContainer extends SubmittedContainer implements GraphContainer {
	private final Graph graph;

	public SubmittedGraphContainer(SubmittableContainer container, SubmitResult result) {
		super(container, result);
		
		this.graph = new BaseGraph();
		this.getAdded().forEach(this.graph::add);
		this.getRemoved().forEach(this.graph::delete);
	}

	@Override
	public Graph toGraph() {
		return graph;
	}
}
