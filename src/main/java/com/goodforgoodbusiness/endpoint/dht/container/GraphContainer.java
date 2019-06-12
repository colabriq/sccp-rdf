package com.goodforgoodbusiness.endpoint.dht.container;

import org.apache.jena.graph.Graph;

import com.goodforgoodbusiness.model.AccessibleContainer;
import com.goodforgoodbusiness.shared.treesort.TreeNode;

public interface GraphContainer extends AccessibleContainer, TreeNode<String> {
	public Graph toGraph();
}
