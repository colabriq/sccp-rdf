package com.goodforgoodbusiness.endpoint.processor.task;

import static com.goodforgoodbusiness.shared.TimingRecorder.timer;
import static com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory.RDF_QUERYING;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.goodforgoodbusiness.webapp.stream.OutputWriteStream;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;

/**
 * Performs a SPARQL query
 * @author ijmad
 */
public class QueryTask implements Runnable {
	private static Logger log = Logger.getLogger(QueryTask.class);
	
	private final Dataset dataset;
	private final String contentType;
	private final String stmt;
	private final WriteStream<Buffer> writeStream;
	private final Future<Long> future;
	
	public QueryTask(Dataset dataset, String contentType, String stmt, WriteStream<Buffer> writeStream, Future<Long> future) {
		this.dataset = dataset;
		this.contentType = contentType;
		this.stmt = stmt;
		this.writeStream = writeStream;
		this.future = future;
	}
	
	@Override
	public void run() {
		try (var timer = timer(RDF_QUERYING)) {
			var query = QueryFactory.create(stmt);
			var exe = QueryExecutionFactory.create(query, dataset);
			
			if (exe.getQuery().isSelectType()) {
				var format = MIMEMappings.getResultsFormat(contentType);
				log.info("Result format=" + format.getSymbol());
				
				var resultSet = exe.execSelect();

				ResultSetFormatter.output(
					new OutputWriteStream(writeStream),
					resultSet,
					format
				);
				
				log.info("Output " + resultSet.getRowNumber() + " results");
				future.complete((long)resultSet.getRowNumber());
			}
			//else if (queryExec.getQuery().isAskType()) {
			//	boolean result = queryExec.execAsk();
			//}
			else if (exe.getQuery().isDescribeType() || exe.getQuery().isConstructType()) {
				var lang = MIMEMappings.getResultsLang(contentType);
				if (lang != null) {
					log.info("Result lang=" + lang);
					var result = exe.getQuery().isDescribeType() ? exe.execDescribe() : exe.execConstruct();
					
					var writer = result.getWriter(lang);
					writer.setProperty("allowBadURIs", true);
					writer.write(
						result,
						new OutputWriteStream(writeStream),
						"PREFIX:"
					);
					
					future.complete(result.size());
				}
				else {
					future.fail("Unable to serialize to " + contentType);
				}
			}
			else {
				future.fail("Could not determine query type");
			}
		}
	}
}
