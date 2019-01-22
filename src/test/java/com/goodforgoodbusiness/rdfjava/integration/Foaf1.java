package com.goodforgoodbusiness.rdfjava.integration;

import com.goodforgoodbusiness.rdfjava.dht.DHTDatasetFactory;
import com.goodforgoodbusiness.rdfjava.dht.DHTRDFRunner;

public class Foaf1 {
	public static void main(String[] args) throws Exception {
		var runner = new DHTRDFRunner("data", new DHTDatasetFactory());
		
//		runner.update(
//			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>                    \n" + 
//			"INSERT DATA {                                                \n" + 
//			"  <https://twitter.com/ijmad8x>  foaf:name 'Ian Maddison'.   \n" + 
//			"  <https://twitter.com/ijmad8x>  foaf:age 35                 \n" + 
//			"}                                                            \n" 
//		);
		
		runner.query(
			"SELECT ?name                                                           \n" + 
			"WHERE {                                                                \n" + 
			"  <https://twitter.com/ijmad8x> <http://xmlns.com/foaf/0.1/name> ?name \n" + 
			"}															            \n",
			"application/xml",
			System.out
		);
//		
//		runner.update(
//			"PREFIX foaf:  <http://xmlns.com/foaf/0.1/>  \n" + 
//			"DELETE {                                    \n" + 
//			"  ?person foaf:name 'Ian Maddison'          \n" + 
//			"}                                           \n" + 
//			"INSERT {                                    \n" +  
//			"  ?person foaf:name 'Hana Ijecko'           \n" + 
//			"}                                           \n" + 
//			"WHERE {                                     \n" +
//			"  ?person foaf:name 'Ian Maddison'          \n" + 
//			"}                                           \n"
//		);
//
//		runner.query(
//			"SELECT ?name                                                           \n" + 
//			"WHERE {                                                                \n" + 
//			"  <https://twitter.com/ijmad8x> <http://xmlns.com/foaf/0.1/name> ?name \n" + 
//			"}															            \n",
//			"application/xml",
//			System.out
//		);
	}
}
