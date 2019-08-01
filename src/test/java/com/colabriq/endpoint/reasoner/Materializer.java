package com.goodforgoodbusiness.endpoint.reasoner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
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

import ru.avicomp.ontapi.OntGraphDocumentSource;
import ru.avicomp.ontapi.OntManagers;
import ru.avicomp.ontapi.owlapi.objects.entity.OWLClassImpl;

public class Materializer {

    public static void apply(Graph graph, OWLReasonerFactory reasonerFactory) throws OWLOntologyCreationException {
        var manager = OntManagers.createONT();
        var docSource = OntGraphDocumentSource.wrap(graph);
        
		var ontology = manager.loadOntologyFromOntologyDocument(docSource);
		
        var reasoner = reasonerFactory.createReasoner(ontology);
        
        reasoner.precomputableInferenceTypes();
        
//        OWLDataPropertyAxiom
        
//        System.out.println("-- classes -- ");
//        List<OWLClass> classes = ontology.classesInSignature().collect(Collectors.toList());
//        System.out.println(classes);
//        
//        System.out.println("-- data -- ");
//        List<OWLDataProperty> dataProperties = ontology.dataPropertiesInSignature().collect(Collectors.toList()); 
//        System.out.println(dataProperties);
//        
//        System.out.println("-- individuals -- ");
//        List<OWLNamedIndividual> individuals = ontology.individualsInSignature().collect(Collectors.toList()); 
//        System.out.println(individuals);
//        
//        System.out.println("-- objects -- ");
//        List<OWLObjectProperty> objects = ontology.objectPropertiesInSignature().collect(Collectors.toList()); 
//        System.out.println(objects);
        
        var iog = new InferredOntologyGenerator(
        	reasoner,
        	Arrays.asList(
        		// InferredClassAxiomGenerator
        		new InferredDisjointClassesAxiomGenerator(),
        		new InferredEquivalentClassAxiomGenerator(),
        		new InferredSubClassAxiomGenerator(),
        		// InferredDataPropertyAxiomGenerator
        		new InferredDataPropertyCharacteristicAxiomGenerator(),
        		new InferredEquivalentDataPropertiesAxiomGenerator(),
        		new InferredSubDataPropertyAxiomGenerator(),
        		// InferredIndividualAxiomGenerator
        		new InferredClassAssertionAxiomGenerator(),
        		new InferredPropertyAssertionGenerator(),
        		// InferredObjectPropertyAxiomGenerator
        		new InferredEquivalentObjectPropertyAxiomGenerator(),
        		new InferredInverseObjectPropertiesAxiomGenerator(),
        		new InferredObjectPropertyCharacteristicAxiomGenerator(),
        		new InferredSubObjectPropertyAxiomGenerator()
        	)
        );
        
//        iog.fillOntology(manager.getOWLDataFactory(), ontology);
        
//        System.out.println(graph.size());
        
        System.out.println("-- InferredSubClassAxiomGenerator --");
        
        InferredSubClassAxiomGenerator gen1 = new InferredSubClassAxiomGenerator() {
        	@Override
        	protected Stream<OWLClass> getEntities(OWLOntology ont) {
        		return Collections.<OWLClass>singleton(new OWLClassImpl(IRI.create("http://example.com/Chicken"))).stream();
        		
//        		return super.getEntities(ont);
        	}
        	
//        	@Override
//		    public Set<OWLSubClassOfAxiom> createAxioms(OWLDataFactory df, OWLReasoner reasoner) {
//		        Set<OWLSubClassOfAxiom> result = new HashSet<>();
//		        List<OWLOntology> imports = reasoner.getRootOntology().importsClosure().collect(Collectors.toList());
//		        
//		        for (OWLOntology ont : imports) {
//		        	List<OWLClass> classes = ont.classesInSignature().distinct().collect(Collectors.toList());
//		        	for (OWLClass clazz : classes) {
//		        		System.out.println("Class: " + clazz);
//		        		addAxioms(clazz, reasoner, df, result);
//		        	}
//		        }
//		        
//		        return result;
//		    }
        	
//		    @Override
//		    protected void addAxioms(OWLClass entity, OWLReasoner reasoner, OWLDataFactory dataFactory, Set<OWLSubClassOfAxiom> result) {
//		        if (reasoner.isSatisfiable(entity)) {
//		            reasoner.getSuperClasses(entity, true).entities().forEach(sup -> result.add(dataFactory.getOWLSubClassOfAxiom(entity, sup)));
//		        } 
//		        else {
//		            result.add(dataFactory.getOWLSubClassOfAxiom(entity, dataFactory.getOWLNothing()));
//		        }
//		    }
        };
        
        Set<OWLSubClassOfAxiom> axioms1 = gen1.createAxioms(manager.getOWLDataFactory(), reasoner);
        
        for (OWLSubClassOfAxiom axiom : axioms1) {
        	System.out.println("AXIOM: " + axiom);
        	ontology.add(axiom);
        }
        
//        System.out.println(graph.size());
//        
//        System.out.println("-- InferredClassAssertionAxiomGenerator --");
//        
//        System.out.println(graph.size());
//        
//        InferredClassAssertionAxiomGenerator gen2 = new InferredClassAssertionAxiomGenerator();
//        Set<OWLClassAssertionAxiom> axioms2 = gen2.createAxioms(manager.getOWLDataFactory(), reasoner);
//        for (OWLClassAssertionAxiom axiom : axioms2) {
//        	System.out.println("AXIOM: " + axiom);
//        	ontology.add(axiom);
//        }
        
//        System.out.println(graph.size());
//        
//        System.out.println("-- InferredPropertyAssertionGenerator --");
//        
//        System.out.println(graph.size());
//        
//        InferredPropertyAssertionGenerator gen3 = new InferredPropertyAssertionGenerator();
//        Set<OWLPropertyAssertionAxiom<?, ?>> axioms3 = gen3.createAxioms(manager.getOWLDataFactory(), reasoner);
//        for (OWLPropertyAssertionAxiom<?, ?> axiom : axioms3) {
//        	System.out.println("AXIOM: " + axiom);
//        	ontology.add(axiom);
//        }
        
//        System.out.println(graph.size());
//        
//        System.out.println("-- InferredSubDataPropertyAxiomGenerator --");
//        
//        System.out.println(graph.size());
//        
//        InferredSubDataPropertyAxiomGenerator gen4 = new InferredSubDataPropertyAxiomGenerator();
//        Set<OWLSubDataPropertyOfAxiom> axioms4 = gen4.createAxioms(manager.getOWLDataFactory(), reasoner);
//        for (OWLSubDataPropertyOfAxiom axiom : axioms4) {
//        	System.out.println("AXIOM: " + axiom);
//        	ontology.add(axiom);
//        }
//        
//        System.out.println(graph.size());
        
//        var r2 = generator.createAxioms(manager.getOWLDataFactory(), reasoner);
//        System.out.println(r2);
//        
//        var r3 = generator.createAxioms(manager.getOWLDataFactory(), reasoner);
//        System.out.println(r3);
    }
}
