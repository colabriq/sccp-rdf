package com.goodforgoodbusiness.rdfjava.integration;

import java.io.File;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphFactory;

import com.goodforgoodbusiness.rdfjava.RDFRunner;
import com.goodforgoodbusiness.shared.FileLoader;

public class Beef {
	private static final String BEEF_CLAIM_PATH = "/Users/ijmad/Desktop/sccp/tb_example/generated_claims";
	
	private static final String COW_QUERY = 
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
	
	public static void main(String[] args) throws Exception {
		var dataset = DatasetFactory.create(DatasetGraphFactory.createMem());
		var runner = new RDFRunner("schema", dataset);
		
		FileLoader.scan(new File(BEEF_CLAIM_PATH), runner.fileConsumer("TURTLE"));
		
		System.out.println(runner.query(COW_QUERY, "application/xml"));
	}
}
