package com.goodforgoodbusiness.endpoint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.sparql.resultset.ResultsFormat;

import com.goodforgoodbusiness.shared.MIMEParse;

public class MIMEMappings {
	public static List<String> RESULT_MIME_TYPES = new LinkedList<>();
	public static Map<String, ResultsFormat> RESULT_MIME_MAP = new HashMap<>();
	public static Map<String, String> RESULT_LANG_MAP = new HashMap<>();
	public static Map<String, String> FILE_TYPES = new HashMap<>();
	
	private static final String DEFAULT_RESULT_MIME_TYPE = "application/sparql-results+xml";
	
	static {
		RESULT_MIME_TYPES.add("application/sparql-results+xml");
		RESULT_MIME_TYPES.add("application/sparql-results+json");
		RESULT_MIME_TYPES.add("application/rdf+xml");
		RESULT_MIME_TYPES.add("application/xml");
		RESULT_MIME_TYPES.add("application/json");
		
		RESULT_MIME_MAP.put("application/sparql-results+xml", ResultsFormat.FMT_RS_XML);
		RESULT_MIME_MAP.put("application/sparql-results+json", ResultsFormat.FMT_RS_JSON);
		RESULT_MIME_MAP.put("application/rdf+xml", ResultsFormat.FMT_RS_XML);
		RESULT_MIME_MAP.put("application/xml", ResultsFormat.FMT_RS_XML);
		RESULT_MIME_MAP.put("application/json", ResultsFormat.FMT_RS_JSON);
		
		RESULT_LANG_MAP.put("application/rdf+xml", "RDF/XML");
		RESULT_LANG_MAP.put("application/xml", "RDF/XML");
		
		FILE_TYPES.put("ttl", "TURTLE");
		FILE_TYPES.put("xml", "RDF/XML");
		FILE_TYPES.put("n3", "N-TRIPLE");
		FILE_TYPES.put("rdf", "RDF/XML");
	}
	
	public static String getContentType(String acceptHeader) {
		if (acceptHeader != null) {
			String contentType = MIMEParse.bestMatch(RESULT_MIME_TYPES, acceptHeader);
			
			if (contentType == null || contentType.length() == 0) {
				return null;
			}
			else {
				return contentType;
			}
		}
		else {
			return DEFAULT_RESULT_MIME_TYPE;
		}
	}
	
	public static ResultsFormat getResultsFormat(String contentType) {
		return RESULT_MIME_MAP.get(contentType);
	}
	
	public static String getResultsLang(String contentType) {
		return RESULT_LANG_MAP.get(contentType);
	}
}
