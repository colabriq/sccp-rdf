package com.goodforgoodbusiness.rdfjava;

import static java.lang.System.getenv;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

import java.io.File;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;

import com.goodforgoodbusiness.rdfjava.dht.DHTDatasetFactory;
import com.goodforgoodbusiness.rdfjava.service.DataService;
import com.goodforgoodbusiness.rdfjava.service.SchemaService;
import com.goodforgoodbusiness.rdfjava.util.FileLoader;

public class RDFApp {
	private static final int SCHEMA_PORT = toInt(getenv("SCHEMA_PORT"), 8080);
	private static final String SCHEMA_PRELOAD_PATH = getenv("SCHEMA_PRELOAD_PATH");
	
	private static final int DATA_PORT = toInt(getenv("DATA_PORT"), 8081);
	private static final String DATA_PRELOAD_PATH = getenv("DATA_PRELOAD_PATH");
	
	public static void main(String[] args) throws Exception {
		// set up schema endpoint (in-memory)
		var schemaDataset = DatasetFactory.create(DatasetGraphFactory.createMem());
		var schemaRunner = new RDFRunner("schema", schemaDataset);
		if (SCHEMA_PRELOAD_PATH != null) {
			File schemaPreloadPath = new File(SCHEMA_PRELOAD_PATH);
			if (schemaPreloadPath.exists()) {
				FileLoader.scan(schemaPreloadPath, schemaRunner.fileConsumer("TURTLE"));
			}
			else {
				throw new Exception(SCHEMA_PRELOAD_PATH + " specified but not found");
			}
		}
		
		var schemaService = new SchemaService(SCHEMA_PORT, schemaRunner);
		schemaService.start();
		

		
		var dataRunner = new RDFRunner("data", new DHTDatasetFactory().create());
		if (DATA_PRELOAD_PATH != null) {
			File dataPreloadPath = new File(DATA_PRELOAD_PATH);
			if (dataPreloadPath.exists()) {
				FileLoader.scan(dataPreloadPath, dataRunner.fileConsumer("TURTLE"));
			}
			else {
				throw new Exception(DATA_PRELOAD_PATH + " specified but not found");
			}
		}
		
		var dataService = new DataService(DATA_PORT, dataRunner);
		dataService.start();
	}
}
