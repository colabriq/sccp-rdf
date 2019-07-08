package com.goodforgoodbusiness.endpoint.processor.task;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.shared.Skolemizer;

public class ImportCommon {
	private static Logger log = Logger.getLogger(ImportCommon.class);
	
	protected static void read(Model model, InputStream stream, String lang) {
		// read in to a separate model so we can control the triples
		var newDataset = Skolemizer.autoSkolemizingDataset();
		
		var newModel = newDataset.getDefaultModel();
		newModel.read(stream, null, lang);
		
		log.info("Adding " + newModel.size() + " stmts");
		
		model.add(newModel);
	}
}
