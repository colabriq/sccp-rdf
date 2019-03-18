package com.goodforgoodbusiness.endpoint.rdf;

import static com.goodforgoodbusiness.endpoint.MIMEMappings.FILE_TYPES;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.goodforgoodbusiness.shared.Skolemizer;
import com.google.inject.Inject;

public class RDFRunner {
	private static Logger log = Logger.getLogger(RDFRunner.class);
	
	private final Dataset dataset;
	private final Model model;
	
	@Inject
	public RDFRunner(Dataset dataset, Model model) {
		this.dataset = dataset;
		this.model = model;
	}
	
	public String query(String queryStmt, String contentType) throws RDFException {
		try (var writer = new StringWriter()) {
			try (var stream = new WriterOutputStream(writer, Charset.forName("UTF-8"))) {
				query(queryStmt, contentType, stream);
				return writer.getBuffer().toString();
			}
		}
		catch (IOException ioe) {
			throw new RDFException(ioe.getMessage(), ioe);
		}
	}
	
	public void query(String queryStmt, String contentType, OutputStream outputStream) throws RDFException, IOException {
		log.info("Querying: \n" + queryStmt);
		
		var query = QueryFactory.create(queryStmt);
		try (var exe = QueryExecutionFactory.create(query, model)) {
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
					throw new RDFException("Unable to serialize to " + contentType);
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
				throw new RDFException("Could not determine query type");
			}
		}
	}
	
	public void update(String updateStmt) throws RDFException {
		log.info("Updating: \n" + updateStmt);
		
		var update = UpdateFactory.create(updateStmt);
		var processor = UpdateExecutionFactory.create(update, dataset);
		processor.execute();
	}

	public void importFile(File file, String lang) throws RDFException {
		try (InputStream stream = new FileInputStream(file)) {
			doImport(stream, lang);
			log.info("Loaded file " + file + " (now " + model.size() + ")");
		}
		catch (IOException e) {
			throw new RDFException("Error reading string", e);
		}
	}
	
	public Consumer<File> fileConsumer() throws RDFException {
		return file -> {
			var lang = FILE_TYPES.get(getExtension(file.getName().toLowerCase()));
			if (lang != null) {
				importFile(file, lang);
			}
			else {
				log.info("Skipped file " + file);
			}
		};
	}
	
	public void importData(CharSequence data, String lang) throws RDFException {
		try (InputStream stream = new ByteArrayInputStream(data.toString().getBytes("UTF-8"))) {
			doImport(stream, lang);
			log.info("Loaded data (now " + model.size() + ")");
		}
		catch (IOException e) {
			throw new RDFException("Error reading string", e);
		}
	}
	
	public void importStream(InputStream stream, String lang) throws RDFException {
		doImport(stream, lang);
		log.info("Loaded data (now " + model.size() + ")");
	}
	
	protected void doImport(InputStream stream, String lang) throws RDFException {
		// read in to a separate model so we can control the triples
		var newDataset = Skolemizer.autoSkolemizingDataset();
		
		var newModel = newDataset.getDefaultModel();
		newModel.read(stream, null, lang);
		
		log.info("Adding " + newModel.size() + " stmts");
		
		this.model.add(newModel);
	}
}
