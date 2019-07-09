package com.goodforgoodbusiness.endpoint.webapp;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.goodforgoodbusiness.endpoint.processor.TaskResult;
import com.goodforgoodbusiness.endpoint.processor.task.ImportStreamTask;
import com.goodforgoodbusiness.endpoint.processor.task.QueryTask;
import com.goodforgoodbusiness.endpoint.processor.task.UpdateTask;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.goodforgoodbusiness.webapp.stream.InputReadStream;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

/**
 * Common query/update methods for SPARQL queries
 */
@Singleton
public class SparqlTaskLauncher {
	private static final Logger log = Logger.getLogger(SparqlTaskLauncher.class);
	
	protected final ExecutorService service;
	protected final Dataset dataset;
	
	@Inject
	public SparqlTaskLauncher(ExecutorService service, Dataset dataset) {
		this.service = service;
		this.dataset = dataset;
	}
	
	/**
	 * Run a 'query' SPARQL query
	 */
	public void query(RoutingContext ctx, Buffer sparqlStmt) {
		String acceptHeader = ctx.request().getHeader(HttpHeaders.ACCEPT);
		
		log.info("Query with accept=" + acceptHeader);
		
		var responseContentType = MIMEMappings.getContentType(Optional.ofNullable(acceptHeader));
		if (responseContentType.isPresent()) {
			log.info("Replying with contentType=" + responseContentType.get());
			
			ctx.response().setStatusCode(200);
			ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, responseContentType.get());
			ctx.response().setChunked(true);
			
			service.submit(new QueryTask(
				dataset,
				responseContentType.get(),
				sparqlStmt.toString(),
				ctx.response(),
				Future.<Long>future().setHandler(result -> {
					if (result.failed()) {
						ctx.fail(result.cause());
					}
					else {
						ctx.response().end();
						ctx.next();
					}
				})
			));
		}
		else {
			ctx.fail(new BadRequestException("Must specify a valid accept header"));
		}
	}
	
	/**
	 * As {@link #query(RoutingContext, Buffer)} but with a String
	 */
	public void query(RoutingContext ctx, String stmt) {
		query(ctx, Buffer.buffer(stmt.getBytes()));
	}
	
	/**
	 * Run a 'update' SPARQL query
	 */
	public void update(RoutingContext ctx, Buffer stmt) {
		ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getContentTypeString());
		
		service.submit(new UpdateTask(
			dataset,
			stmt.toString(),
			Future.<TaskResult>future().setHandler(result -> {
				if (result.failed()) {
					ctx.fail(result.cause());
				}
				else {
					ctx.response().end(result.result().toJson());
					ctx.next();
				}
			})
		));
	}
	
	/**
	 * As {@link #update(RoutingContext, Buffer)} but with a String
	 */
	public void update(RoutingContext ctx, String stmt) {
		update(ctx, Buffer.buffer(stmt.getBytes()));
	}
	
	/**
	 * Starts an import from an uploaded file
	 */
	public void importFile(RoutingContext ctx, String lang, AsyncFile file) {
		service.submit(
			new ImportStreamTask(
			    dataset,
			    new InputReadStream(file),
			    lang,
				Future.<TaskResult>future().setHandler(result -> {
					file.close();
					
					if (result.failed()) {
						ctx.fail(result.cause());
					}
					else {
						// standard JSON result
						ctx.response().end(result.result().toJson());
						ctx.next();
					}
				})
			)
		);
	}
}
