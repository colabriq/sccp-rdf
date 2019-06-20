package com.goodforgoodbusiness.endpoint.graph.dht.container;

import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Graph;

import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.container.GraphContainer;
import com.goodforgoodbusiness.model.Envelope;
import com.goodforgoodbusiness.model.ProvenLink;
import com.goodforgoodbusiness.model.Signature;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.common.reflect.TypeToken;

/**
 * Extends {@link StorableContainer} by putting the added/removed triples in to their own mini-graph.
 * @author ijmad
 *
 */
public class StorableGraphContainer extends StorableContainer implements GraphContainer {
	@SuppressWarnings("serial")
	public static List<StorableGraphContainer> toStorableGraphContainers(String json) {
		return JSON.decode(json, new TypeToken<List<StorableGraphContainer>>() {}.getType() );
	}
	
	// lazy as this might be loaded through deserialization
	private Graph graph = null;

	public StorableGraphContainer(Envelope env, Set<? extends ProvenLink> links, Signature signature) {
		super(env, links, signature);
	}
	
	@Override
	public Graph toGraph() {
		// lazy init in case we're deserializing
		if (this.graph == null) {
			this.graph = new BaseGraph();
			this.getAdded().forEach(this.graph::add);
			this.getRemoved().forEach(this.graph::delete);
		}
		
		return this.graph;
	}
	
	
	@Override
	public String toString() {
		return "StorableGraphContainer(" + getInnerEnvelope() + ", " + getLinks() + ", " + getSignature() + ")";
	}
}
