package com.goodforgoodbusiness.endpoint.plugin.internal.builtin;

import static java.util.Arrays.asList;
import static org.semanticweb.owlapi.model.parameters.AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS;
import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubDataPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;

import com.goodforgoodbusiness.endpoint.plugin.internal.InternalReasonerPlugin;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import ru.avicomp.ontapi.OntGraphDocumentSource;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.OntologyManager;


public class AbstractReasonerPlugin implements InternalReasonerPlugin {
	private final OntologyManager manager;
	private final OWLReasonerFactory reasonerFactory;

	public AbstractReasonerPlugin(Graph graph, OWLReasonerFactory reasonerFactory) throws OWLOntologyCreationException {
		this.manager = OntManagers.createONT();
		this.reasonerFactory = reasonerFactory;
	}
	
	@Override
	public void initialized(Graph baseGraph) throws OWLOntologyCreationException {
		// perform reasoning on full base graph
		var baseSource = OntGraphDocumentSource.wrap(baseGraph);
		var ontology = manager.loadOntologyFromOntologyDocument(baseSource);
		var reasoner = reasonerFactory.createReasoner(ontology);
		var iog = new InferredOntologyGenerator(reasoner);
		iog.fillOntology(this.manager.getOWLDataFactory(), ontology);
	}

	@Override
	public void updated(Graph updateGraph) throws OWLOntologyCreationException {
		// wrap updated graph into ontology
		var updateSource = OntGraphDocumentSource.wrap(updateGraph);
		var ontology = manager.loadOntologyFromOntologyDocument(updateSource);
		var reasoner = reasonerFactory.createReasoner(ontology);
		var generators = makeGenerators(ontology, updateGraph);
		
		generators.stream()
			.flatMap(g -> g.createAxioms(this.manager.getOWLDataFactory(), reasoner).stream())
			.filter(ax -> !ontology.containsAxiom(ax, INCLUDED, IGNORE_AXIOM_ANNOTATIONS))
			.forEach(ontology::add)
		;
	}
	
	private static List<InferredAxiomGenerator<?>> makeGenerators(OWLOntology ont, Graph graph) {
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
	
	/* Intercepts the getEntities method to return only new entities */
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
