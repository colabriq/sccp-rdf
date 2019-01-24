package com.goodforgoodbusiness.rdfjava;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.google.inject.Guice.createInjector;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraphMaker;

import com.goodforgoodbusiness.rdfjava.rdf.RDFRunner;
import com.goodforgoodbusiness.rdfjava.service.SchemaService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class RDFSchemaModule extends AbstractModule {
	private final Configuration config;
	
	public RDFSchemaModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		binder().requireExplicitBindings();
		
		Properties props = getProperties(config);
		Names.bindProperties(binder(), props);
		
		bind(SchemaService.class);
		bind(RDFRunner.class);
	}
	
	@Provides @Singleton
	protected Dataset getDataset() {
		return DatasetFactory.create(
			new DatasetGraphMaker(new GraphMem())
		);
	}
	
	@Provides @Singleton
	protected Model getModel(Dataset dataset) {
		return dataset.getDefaultModel();
	}
	
	public static void main(String[] args) throws Exception {
		createInjector(new RDFSchemaModule(loadConfig(RDFDataModule.class, "schema.properties")))
			.getInstance(SchemaService.class)
			.start()
		;
	}
}


////return new DHTTripleStore( this, claimContextMap, claimCollector );
//
//public class SchemaModule extends AbstractModule {
//	private static final int SCHEMA_PORT = toInt(getenv("SCHEMA_PORT"), 8080);
//	private static final String SCHEMA_PRELOAD_PATH = getenv("SCHEMA_PRELOAD_PATH");
//	
//	
//	
//	public static void main(String[] args) throws Exception {
//		// set up schema endpoint (in-memory)
//		var schemaDataset = DatasetFactory.create(DatasetGraphFactory.createMem());
//		var schemaRunner = new RDFRunner("schema", schemaDataset);
//		if (SCHEMA_PRELOAD_PATH != null) {
//			File schemaPreloadPath = new File(SCHEMA_PRELOAD_PATH);
//			if (schemaPreloadPath.exists()) {
//				FileLoader.scan(schemaPreloadPath, schemaRunner.fileConsumer("TURTLE"));
//			}
//			else {
//				throw new Exception(SCHEMA_PRELOAD_PATH + " specified but not found");
//			}
//		}
//		
//		var schemaService = new SchemaService(SCHEMA_PORT, schemaRunner);
//		schemaService.start();
//	}
//}
