package com.goodforgoodbusiness.endpoint.plugin.internal.builtin;

import static org.apache.jena.graph.NodeFactory.createURI;

import java.io.StringWriter;
import java.util.stream.Collectors;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphOne;

import com.github.jsonldjava.shaded.com.google.common.collect.Streams;
import com.goodforgoodbusiness.endpoint.graph.container.GraphContainer;
import com.goodforgoodbusiness.endpoint.graph.dht.container.StorableGraphContainer;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPlugin;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPluginException;

/** 
 * Adds extra triples to support custody chain exploration.
 */
public class ObjectCustodyChainReasonerPlugin implements InternalPlugin {
	public static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final Node RDF_PREFIX_TYPE = createURI(RDF_PREFIX + "type");
	
	public static final String PRED_CONTAINER_PREFIX = "https://schemas.goodforgoodbusiness.org/general/container#";
	
	public static final Node PRED_CONTAINER_CONTAINER = createURI(PRED_CONTAINER_PREFIX + "Container");
	
	public static final Node PRED_CONTAINER_LINK = createURI(PRED_CONTAINER_PREFIX + "describedByContainer");
	public static final Node PRED_CONTAINER_DID = createURI(PRED_CONTAINER_PREFIX + "creator");
	public static final Node PRED_CONTAINER_CONTENT = createURI(PRED_CONTAINER_PREFIX + "content");
	public static final Node PRED_CONTAINER_ANTECEDENT = createURI(PRED_CONTAINER_PREFIX + "hasAntecedent");
	public static final Node PRED_CONTAINER_ENTITY = createURI(PRED_CONTAINER_PREFIX + "describesEntity");
	
	public static Node containerURI(String id) {
		return createURI("container:" + id);
	}
	
	public static Node containerURI(StorableGraphContainer c) {
		return containerURI(c.getId());
	}
	
	public static Node writeGraph(Graph graph) {
		var df = DatasetFactory.wrap(DatasetGraphOne.create(graph));
		var sw = new StringWriter();
		
		// output as N3
		df.getDefaultModel().write(sw, "N3");
		
		return NodeFactory.createLiteral(sw.toString());
	}
	
//	private Graph mainGraph = null;
	private Graph inferredGraph = null;

	@Override
	public void init(Graph _mainGraph, Graph _inferredGraph) throws InternalPluginException {
//		this.mainGraph = _mainGraph;
		this.inferredGraph = _inferredGraph;
	}

	@Override
	public void exec(GraphContainer newContainer, boolean inMainGraph) throws InternalPluginException {
		var graph = newContainer.toGraph();
		
		if (newContainer instanceof StorableGraphContainer) {
			var sgc = (StorableGraphContainer)newContainer;
			
			var containerURI = containerURI(sgc);
			var contents = writeGraph(sgc.toGraph());
		
			inferredGraph.add(new Triple(containerURI, RDF_PREFIX_TYPE, PRED_CONTAINER_CONTAINER));
		
			// run over all the triples and collect up subjects that are URIs
			var subjects = Streams
				.stream(graph.find())
				.map(Triple::getSubject)
				.filter(Node::isURI)
				.map(Node::getURI)
				.collect(Collectors.toSet())
			;
		
			// we consider a URI to be a 'object-like subject'. 
			// generate new triples that reveal its chain of custody		
			subjects.stream().forEach(subject -> {
				var subjectURI = createURI(subject); // have to re-wrap this
				
				inferredGraph.add(new Triple(subjectURI, PRED_CONTAINER_LINK, containerURI));
				inferredGraph.add(new Triple(containerURI, PRED_CONTAINER_DID, createURI(sgc.getSignature().getDID())));
				inferredGraph.add(new Triple(containerURI, PRED_CONTAINER_CONTENT, contents));
				
				sgc.getLinks().forEach(link -> {
					inferredGraph.add(new Triple(containerURI, PRED_CONTAINER_ANTECEDENT, containerURI(link.getRef())));
				});
				
				inferredGraph.add(new Triple(containerURI, PRED_CONTAINER_ENTITY, subjectURI));
			});
		}
	}
}
