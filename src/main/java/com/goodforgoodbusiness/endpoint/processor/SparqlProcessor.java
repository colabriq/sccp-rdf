package com.goodforgoodbusiness.endpoint.processor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Runs SPARQL queries and imports turtle files, etc.
 */
@Singleton
public class SparqlProcessor {
	private static Logger log = Logger.getLogger(SparqlProcessor.class);
	
	private final Dataset dataset;
	
	@Inject
	public SparqlProcessor(Provider<Dataset> datasetProvider) {
		this.dataset = datasetProvider.get();
	}
	
	public String query(String queryStmt, String contentType) throws SparqlProcessException {
		try (var writer = new StringWriter()) {
			try (var stream = new WriterOutputStream(writer, Charset.forName("UTF-8"))) {
				query(queryStmt, contentType, stream);
				return writer.getBuffer().toString();
			}
		}
		catch (IOException ioe) {
			throw new SparqlProcessException(ioe.getMessage(), ioe);
		}
	}
	
	public void query(String queryStmt, String contentType, OutputStream outputStream) throws SparqlProcessException, IOException {
		log.info("Querying: \n" + queryStmt);
		
		var query = QueryFactory.create(queryStmt);
		try (var exe = QueryExecutionFactory.create(query, dataset)) {
			if (exe.getQuery().isSelectType()) {
				var format = MIMEMappings.getResultsFormat(contentType);
				log.info("Result format=" + format.getSymbol());
				
				var resultSet = exe.execSelect();
				
				if (log.isDebugEnabled()) {
					var captureStream = new ByteArrayOutputStream();
					ResultSetFormatter.output(captureStream, resultSet, format);
					log.debug("Result=\n" + new String(captureStream.toByteArray()));
					outputStream.write(captureStream.toByteArray());
				}
				else {
					ResultSetFormatter.output(outputStream, resultSet, format);
				}
			}
//			else if (queryExec.getQuery().isAskType()) {
//				boolean result = queryExec.execAsk();
//			}
			else if (exe.getQuery().isDescribeType() || exe.getQuery().isConstructType()) {
				var lang = MIMEMappings.getResultsLang(contentType);
				log.info("Result lang=" + lang);
				
				if (lang == null) {
					throw new SparqlProcessException("Unable to serialize to " + contentType);
				}
				
				var result = exe.getQuery().isDescribeType() ? exe.execDescribe() : exe.execConstruct();
				
				var writer = result.getWriter(lang);
				writer.setProperty("allowBadURIs", true);
				
				if (log.isDebugEnabled()) {
					var captureStream = new ByteArrayOutputStream();
					writer.write(result, captureStream, "PREFIX:");
					log.debug("Result=\n" + new String(captureStream.toByteArray()));
					outputStream.write(captureStream.toByteArray());
				}
				else {
					writer.write(result, outputStream, "PREFIX:");
				}
			}
			else {
				throw new SparqlProcessException("Could not determine query type");
			}
		}
	}
	
	public void update(String updateStmt) throws SparqlProcessException {
		log.info("Updating: \n" + updateStmt);
		
		var update = UpdateFactory.create(updateStmt);
		var processor = UpdateExecutionFactory.create(update, dataset);
		processor.execute();
	}
}
