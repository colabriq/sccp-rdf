package com.colabriq.endpoint.plugin.internal.builtin;

import static com.colabriq.endpoint.storage.TripleContext.Type.REASONER;
import static org.apache.jena.graph.NodeFactory.createURI;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphOne;

import com.github.jsonldjava.shaded.com.google.common.collect.Streams;
import com.colabriq.endpoint.plugin.StorableGraphContainer;
import com.colabriq.endpoint.plugin.internal.InternalPlugin;
import com.colabriq.endpoint.plugin.internal.InternalPluginException;
import com.colabriq.endpoint.storage.TripleContexts;
import com.colabriq.model.GraphContainer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** 
 * Adds extra triples to support custody chain exploration.
 */
@Singleton
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
	
	public static Node containerURI(GraphContainer c) {
		return containerURI(c.getId());
	}
	
	public static Node writeGraph(Graph graph) {
		var df = DatasetFactory.wrap(DatasetGraphOne.create(graph));
		var sw = new StringWriter();
		
		// output as N3
		df.getDefaultModel().write(sw, "N3");
		
		return NodeFactory.createLiteral(sw.toString());
	}
	
	private final Graph graph;
	private final TripleContexts contexts;

	@Inject
	public ObjectCustodyChainReasonerPlugin(TripleContexts contexts, Graph graph) {
		this.graph = graph;
		this.contexts = contexts;
	}
	
	@Override
	public void init() throws InternalPluginException {
	}
	
	@Override
	public void exec(StorableGraphContainer container) throws InternalPluginException {
		var containerURI = containerURI(container);
		var contents = writeGraph(container.asGraph());
		var newTriples = new HashSet<Triple>();
		
		newTriples.add(new Triple(containerURI, RDF_PREFIX_TYPE, PRED_CONTAINER_CONTAINER));
		
		// run over all the triples and collect up subjects that are URIs
		var subjects = Streams
			.stream(container.asGraph().find())
			.map(Triple::getSubject)
			.filter(Node::isURI)
			.map(Node::getURI)
			.collect(Collectors.toSet())
		;
	
		// we consider a URI to be a 'object-like subject'. 
		// generate new triples that reveal its chain of custody		
		subjects.stream().forEach(subject -> {
			var subjectURI = createURI(subject); // have to re-wrap this
			
			newTriples.add(new Triple(subjectURI, PRED_CONTAINER_LINK, containerURI));
			newTriples.add(new Triple(containerURI, PRED_CONTAINER_DID, createURI(container.getSignature().getDID())));
			newTriples.add(new Triple(containerURI, PRED_CONTAINER_CONTENT, contents));
			
			container.getLinks().forEach(link -> {
				newTriples.add(new Triple(containerURI, PRED_CONTAINER_ANTECEDENT, containerURI(link.getRef())));
			});
			
			newTriples.add(new Triple(containerURI, PRED_CONTAINER_ENTITY, subjectURI));
		});
		
		// add the new triples to the graph and record their context
		newTriples.forEach(triple -> {
			contexts.create(triple)
				.withType(REASONER)
				.withReasoner(ObjectCustodyChainReasonerPlugin.class.getName())
				.withContainerID(container.getId())
				.save()
			;
			
			graph.add(triple);
		});
	}
}
