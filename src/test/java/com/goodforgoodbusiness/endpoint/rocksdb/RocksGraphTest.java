package com.goodforgoodbusiness.endpoint.rocksdb;

import static com.goodforgoodbusiness.endpoint.webapp.MIMEMappings.FILE_TYPES;
import static com.goodforgoodbusiness.shared.FileLoader.scan;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraphOne;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.graph.rocks.RocksTripleStore;
import com.goodforgoodbusiness.rocks.RocksManager;
import com.goodforgoodbusiness.shared.Skolemizer;

public class RocksGraphTest {
	private static Logger log = Logger.getLogger(RocksGraphTest.class);
	
	public static void importFiles(Model model, File path) throws Exception {
		scan(path, file -> {
			var lang = FILE_TYPES.get(getExtension(file.getName().toLowerCase()));
			if (lang != null) {
				log.info("Loading file " + file);
				try (var stream = new FileInputStream(file)) {
					doImport(model, stream, lang);
					log.info("Loaded file " + file + " (now " + model.size() + ")");
				}
				catch (IOException e) {
					throw new RuntimeException("Error reading file " + file, e);
				}
			}
		});
	}
	
	public static void doImport(Model model, InputStream stream, String lang) {
		// read in to a separate model so we can control the triples
		var newDataset = Skolemizer.autoSkolemizingDataset();
		
		var newModel = newDataset.getDefaultModel();
		newModel.read(stream, null, lang);
		
		log.info("Adding " + newModel.size() + " stmts");
		
		model.add(newModel);
		
		log.info("Model size now " + model.size());
	}
	
	public static void main(String[] args) throws Exception {
		var manager = new RocksManager("/Users/ijmad/Desktop/sccp/prototype/rocks");		
		manager.start();
		
		var rockStore = new RocksTripleStore(manager);
		
		var graph = new GraphMem() {
			@Override
			protected TripleStore createTripleStore() {
				return rockStore;
			}
		};
		
		var datasetGraph = DatasetGraphOne.create(graph);
		var dataset = DatasetFactory.wrap(datasetGraph);
		
		importFiles(
			dataset.getDefaultModel(), 
			new File("/Users/ijmad/Desktop/sccp/prototype/test/src/test/resources/beef/midi/generated_claims")
		);
		
		String sparqlStmt =
			"PREFIX com: <https://schemas.goodforgoodbusiness.com/common-operating-model/lite/>" +
			"SELECT ?buyerRef ?quantity ?unitPrice ?shipmentRef ?ain ?vaccine WHERE {" + 
			"    ?order com:buyer <urn:uuid:448c5299-b858-4eb1-bc55-0a7a6c04efee>;" + 
			"        com:buyerRef ?buyerRef;" + 
			"        com:quantity ?quantity;" + 
			"        com:unitPrice ?unitPrice;" + 
			"        com:fulfilledBy ?shipment." + 
			"    ?shipment com:consignee <urn:uuid:448c5299-b858-4eb1-bc55-0a7a6c04efee>;" + 
		    "        com:shipmentRef ?shipmentRef." + 
			"    OPTIONAL {" + 
		    "        ?shipment com:usesItem ?cow." + 
			"        OPTIONAL {" + 
		    "            ?cow com:ain ?ain." + 
			"            OPTIONAL {" + 
			"                ?cow com:vaccination ?vaccination." + 
			"                ?vaccination com:vaccine ?vaccine." + 
			"            }" + 
			"        }" + 
		    "    }" + 
		    "}"; 
		
		var query = QueryFactory.create(sparqlStmt);
		try (var exe = QueryExecutionFactory.create(query, datasetGraph)) {
			var resultSet = exe.execSelect();
			ResultSetFormatter.output(System.out, resultSet, ResultsFormat.FMT_RS_XML);
		}
	}
}
