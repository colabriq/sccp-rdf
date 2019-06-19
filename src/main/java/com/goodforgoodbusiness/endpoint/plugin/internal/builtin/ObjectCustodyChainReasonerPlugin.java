package com.goodforgoodbusiness.endpoint.plugin.internal.builtin;

import org.apache.jena.graph.Graph;

import com.goodforgoodbusiness.endpoint.plugin.internal.InternalReasonerException;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalReasonerPlugin;

public class ObjectCustodyChainReasonerPlugin implements InternalReasonerPlugin {
	@Override
	public void init(Graph mainGraph, Graph inferredGraph) throws InternalReasonerException {
	}

	@Override
	public void reason(Graph newGraph, boolean inMainGraph) throws InternalReasonerException {
	}
}
