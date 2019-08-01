package com.goodforgoodbusiness.endpoint.plugin;

import org.apache.jena.graph.Graph;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.model.GraphContainer;
import com.goodforgoodbusiness.model.StorableContainer;

/**
 * {@link StorableContainer} & {@link GraphContainer} wrapper
 * @author ijmad
 */
public class StorableGraphContainer extends StorableContainer implements GraphContainer {
	private final Graph graph;

	public StorableGraphContainer(StorableContainer container) {
		super(container.getInnerEnvelope(), container.getLinks(), container.getSignature());
		
		// XXX doing anything with removed here is pointless
		// XXX unless we layer this on top of the existing graph?
		// XXX but how will that play with using the reasoners in streaming mode?
		
		this.graph = BaseGraph.newGraph();
		container.getInnerEnvelope().getContents().getRemoved().forEach(this.graph::add);
		container.getInnerEnvelope().getContents().getAdded().forEach(this.graph::add);
	}
	
	@Override
	public Graph asGraph() {
		return graph;
	}
}
