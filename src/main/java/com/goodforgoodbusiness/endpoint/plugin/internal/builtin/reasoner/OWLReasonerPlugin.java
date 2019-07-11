package com.goodforgoodbusiness.endpoint.plugin.internal.builtin.reasoner;

import static java.util.Arrays.asList;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDataPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredDisjointClassesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentDataPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredEquivalentObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyCharacteristicAxiomGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;

import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPlugin;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPluginException;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;

/**
 * High level plugin does reasoning for any reasoner following the {@link OWLReasonerPlugin} interfaces
 */
public class OWLReasonerPlugin implements InternalPlugin {
	private static final Logger log = Logger.getLogger(OWLReasonerPlugin.class);
	
	private final OntologyManager manager;
	private final OWLReasonerFactory reasonerFactory;
	
	private Graph mainGraph;
	private OWLOntology mainGraphOntology;
	
	private Graph inferredGraph;
	private OWLOntology inferredGraphOntology;

	protected OWLReasonerPlugin(OWLReasonerFactory reasonerFactory) {
		this.manager = OntManagers.createONT();
		this.reasonerFactory = reasonerFactory;
	}
	
	@Override
	public void init(Graph _mainGraph) throws InternalPluginException {
		log.info("Initializing reasoner");
		
//		// save mainGraph + inferredGraph for later
//		this.mainGraph = _mainGraph;
//		this.inferredGraph = _inferredGraph;
//		
//		try (var timer = timer(RDF_REASONING)) {
//			// wrap main graph (initial source) + inferred graph (target) in ontology
//			this.mainGraphOntology = manager.loadOntologyFromOntologyDocument(wrap(mainGraph));
//			this.inferredGraphOntology = manager.loadOntologyFromOntologyDocument(wrap(inferredGraph));
//			
//			// perform reasoning on initialized main graph
//			var reasoner = reasonerFactory.createReasoner(mainGraphOntology);
//			var iog = new InferredOntologyGenerator(reasoner);
//			
//			iog.fillOntology(this.manager.getOWLDataFactory(), inferredGraphOntology);
//		}
//		catch (OWLOntologyCreationException e) {
//			throw new InternalPluginException("Could not initialize reasoner plugin", e);
//		}
	}

	@Override
	public void exec(Graph newGraph) throws InternalPluginException {
		log.info("Reasoning...");
		
		try {
			// if the triples are not yet in the main graph
			// create a union so we can reason over the whole thing
			// wrap new graph into ontology
//			var wholeGraph = inMainGraph ? mainGraph : new CustomGraphUnion(mainGraph, newGraph);
//			var wholeGraphOntology = manager.loadOntologyFromOntologyDocument(wrap(wholeGraph));
			
//			var reasoner = reasonerFactory.createReasoner(wholeGraphOntology);
			
			// we only reason over triples in newGraph 
			// use the intercept mechanism so only these are considered
//			var newGraphOntology = manager.loadOntologyFromOntologyDocument(wrap(newGraph));
//			var generators = makeGenerators(newGraphOntology);
//			
			// do triple generation (adding results to inferred graph)
			
//			generators.stream()
//				.flatMap(g -> g.createAxioms(this.manager.getOWLDataFactory(), reasoner).stream())
//				.filter(ax -> !inferredGraphOntology.containsAxiom(ax, Imports.INCLUDED, AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS))
//				.forEach(inferredGraphOntology::add)
//			;
		}
		catch (Exception /*OWLOntologyCreationException*/ e) {
			throw new InternalPluginException("Could not initialize reasoner plugin", e);
		}
	}
	
	private static List<InferredAxiomGenerator<?>> makeGenerators(OWLOntology ont) {
		// each of the following overrides the base class's getEntities methods so only new data is returned
		// the type of OWLObject each needs is specific to each of the classes, must look at the source
		
		return asList(
			createGeneratorWithInterceptedEntities(InferredDisjointClassesAxiomGenerator.class, ont.classesInSignature()),
			createGeneratorWithInterceptedEntities(InferredEquivalentClassAxiomGenerator.class, ont.classesInSignature()),
			createGeneratorWithInterceptedEntities(InferredSubClassAxiomGenerator.class, ont.classesInSignature()),
			createGeneratorWithInterceptedEntities(InferredDataPropertyCharacteristicAxiomGenerator.class, ont.dataPropertiesInSignature()),
			createGeneratorWithInterceptedEntities(InferredEquivalentDataPropertiesAxiomGenerator.class, ont.dataPropertiesInSignature()),
			createGeneratorWithInterceptedEntities(InferredSubDataPropertyAxiomGenerator.class, ont.dataPropertiesInSignature()),
			createGeneratorWithInterceptedEntities(InferredClassAssertionAxiomGenerator.class, ont.individualsInSignature()),
			createGeneratorWithInterceptedEntities(InferredPropertyAssertionGenerator.class, ont.individualsInSignature()),
			createGeneratorWithInterceptedEntities(InferredEquivalentObjectPropertyAxiomGenerator.class, ont.objectPropertiesInSignature()),
			createGeneratorWithInterceptedEntities(InferredInverseObjectPropertiesAxiomGenerator.class, ont.objectPropertiesInSignature()),
			createGeneratorWithInterceptedEntities(InferredObjectPropertyCharacteristicAxiomGenerator.class, ont.objectPropertiesInSignature()),
			createGeneratorWithInterceptedEntities(InferredSubObjectPropertyAxiomGenerator.class, ont.objectPropertiesInSignature())
		);
	}
	
	// intercepts the getEntities method to return only new entities
	private static <T extends InferredAxiomGenerator<?>> T createGeneratorWithInterceptedEntities(
		Class<T> clazz, Stream<? extends OWLObject> entities) {
		
		var factory = new ProxyFactory();
		
		factory.setSuperclass(clazz);
		factory.setFilter(m -> m.getName().equals("getEntities"));
		
		var handler = new MethodHandler() {
			@Override
			public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
				return entities; // return the alternate result
			}
		};
		
		try {
			@SuppressWarnings("unchecked")
			T create = (T)factory.create(new Class[0], new Object[0], handler);
			return create;
		} 
		catch (ReflectiveOperationException e) {
			throw new RuntimeException("Could not instantiate proxy");
		}
	}
}
