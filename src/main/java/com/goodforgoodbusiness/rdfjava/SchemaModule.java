package com.goodforgoodbusiness.rdfjava;

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
