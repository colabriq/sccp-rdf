package com.goodforgoodbusiness.endpoint.processor.task;

import java.io.InputStream;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.storage.TripleContext.Type;
import com.goodforgoodbusiness.endpoint.storage.TripleContexts;
import com.goodforgoodbusiness.shared.Skolemizer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Common import functions*
 */
@Singleton
public class Importer {
	private static Logger log = Logger.getLogger(Importer.class);
	
	private final TripleContexts contexts;
	private final Dataset dataset;
	
	@Inject
	public Importer(TripleContexts contexts, Dataset dataset) {
		this.contexts = contexts;
		this.dataset = dataset;
	}
	
	public Model getModel() {
		return dataset.getDefaultModel();
	}
	
	public void read(InputStream stream, String lang, boolean isPreload) {
		// read in to a separate model so we can control the triples
		var newDataset = Skolemizer.autoSkolemizingDataset();
		
		var newModel = newDataset.getDefaultModel();
		newModel.read(stream, null, lang);
		
		log.info("Adding " + newModel.size() + " stmts");
		
		// if preload, create the preload context for the new triples
		if (isPreload) {
			var it = newModel.getGraph().find();
			while (it.hasNext()) {
				contexts.create(it.next()).withType(Type.PRELOADED).save();
			}
		}
		
		getModel().add(newModel);
		

	}
}
