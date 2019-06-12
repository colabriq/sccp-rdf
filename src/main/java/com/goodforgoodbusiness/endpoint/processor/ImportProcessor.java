package com.goodforgoodbusiness.endpoint.processor;

import static com.goodforgoodbusiness.endpoint.MIMEMappings.FILE_TYPES;
import static com.goodforgoodbusiness.shared.FileLoader.scan;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.shared.Skolemizer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/** 
 * Run imports turtle data (files, streams, etc.)
 */
@Singleton
public class ImportProcessor {
	private static Logger log = Logger.getLogger(ImportProcessor.class);
	
	private static void doImport(Model model, InputStream stream, String lang) throws ImportProcessException {
		// read in to a separate model so we can control the triples
		var newDataset = Skolemizer.autoSkolemizingDataset();
		
		var newModel = newDataset.getDefaultModel();
		newModel.read(stream, null, lang);
		
		log.info("Adding " + newModel.size() + " stmts");
		
		model.add(newModel);
	}
	
	private final Dataset dataset;
	
	@Inject
	public ImportProcessor(Provider<Dataset> datasetProvider) {
		this.dataset = datasetProvider.get();
	}
	
	/**
	 * Import a local file or directory from storage
	 * @throws FileNotFoundException 
	 */
	public void importPath(File path) throws FileNotFoundException {
		var model = dataset.getDefaultModel();
		
		scan(path, file -> {
			var lang = FILE_TYPES.get(getExtension(file.getName().toLowerCase()));
			if (lang != null) {
				log.info("Loading file " + file);
				try (var stream = new FileInputStream(file)) {
					doImport(model, stream, lang);
					log.info("Loaded file " + file + " (now " + model.size() + ")");
				}
				catch (IOException e) {
					throw new ImportProcessException("Error reading file " + file, e);
				}
			}
			else {
				log.info("Skipped file " + file);
			}
		});		
	}

	/**
	 * Import data in a buffer directly
	 */
	public void importData(CharSequence data, String lang) throws ImportProcessException {
		var model = dataset.getDefaultModel();
		
		try (InputStream stream = new ByteArrayInputStream(data.toString().getBytes("UTF-8"))) {
			doImport(model, stream, lang);
			log.info("Loaded data (now " + model.size() + ")");
		}
		catch (IOException e) {
			throw new ImportProcessException("Error reading string", e);
		}
	}
	
	/**
	 * Import data in a stream directly
	 */
	public void importStream(InputStream stream, String lang) throws ImportProcessException {
		var model = dataset.getDefaultModel();
		
		doImport(model, stream, lang);
		log.info("Loaded data (now " + model.size() + ")");
	}
}
