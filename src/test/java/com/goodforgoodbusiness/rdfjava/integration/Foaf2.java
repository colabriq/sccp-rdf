package com.goodforgoodbusiness.rdfjava.integration;

import com.goodforgoodbusiness.rdfjava.dht.DHTDatasetFactory;
import com.goodforgoodbusiness.rdfjava.dht.DHTRDFRunner;

public class Foaf2 {
	public static void main(String[] args) throws Exception {
		// run with separate runners, as if the system was restarted.
		
		var runner1 = new DHTRDFRunner("data", new DHTDatasetFactory());
		
		runner1.update(
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>                    \n" + 
			"INSERT DATA {                                                \n" + 
			"  <https://twitter.com/ijmad8x>  foaf:name 'Ian Maddison'.   \n" + 
			"  <https://twitter.com/ijmad8x>  foaf:age 35                 \n" + 
			"}                                                            \n" 
		);
		
		var runner2 = new DHTRDFRunner("data", new DHTDatasetFactory());
		
		runner2.query(
			"SELECT ?name                                                           \n" + 
			"WHERE {                                                                \n" + 
			"  <https://twitter.com/ijmad8x> <http://xmlns.com/foaf/0.1/name> ?name \n" + 
			"}															            \n",
			"application/xml",
			System.out
		);
		
		var runner3 = new DHTRDFRunner("data", new DHTDatasetFactory());
		
		runner3.update(
			"PREFIX foaf:  <http://xmlns.com/foaf/0.1/>  \n" + 
			"DELETE {                                    \n" + 
			"  ?person foaf:name 'Ian Maddison'          \n" + 
			"}                                           \n" + 
			"INSERT {                                    \n" +  
			"  ?person foaf:name 'Hana Ijecko'           \n" + 
			"}                                           \n" + 
			"WHERE {                                     \n" +
			"  ?person foaf:name 'Ian Maddison'          \n" + 
			"}                                           \n"
		);

		var runner4 = new DHTRDFRunner("data", new DHTDatasetFactory());
		
		runner4.query(
			"SELECT ?name                                                           \n" + 
			"WHERE {                                                                \n" + 
			"  <https://twitter.com/ijmad8x> <http://xmlns.com/foaf/0.1/name> ?name \n" + 
			"}															            \n",
			"application/xml",
			System.out
		);
	}
}
