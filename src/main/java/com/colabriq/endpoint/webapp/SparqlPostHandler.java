package com.goodforgoodbusiness.endpoint.webapp;

import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

/**
 * Handle posted SPARQL queries and updates.
 * As per SPARQL spec.
 * @author ijmad
 */
@Singleton
public class SparqlPostHandler implements Handler<RoutingContext> {
	private final SparqlTaskLauncher sparql;
	
	@Inject
	public SparqlPostHandler(SparqlTaskLauncher sparql) {
		this.sparql = sparql;
	}
	
	@Override
	public void handle(RoutingContext ctx) {
		var contentType = ctx.request().getHeader(HttpHeaders.CONTENT_TYPE);
		
		// post data
		if (contentType != null) {
			// XXX we can optimise here to process piece by piece
			// although it may be sensible to just set a maximum size
			// we have the logic in UploadHandler to do this properly
			ctx.request().bodyHandler(buffer -> {
				if (ContentType.SPARQL_UPDATE.matches(contentType)) {
					sparql.update(ctx, buffer);
				}
				else if (ContentType.SPARQL_QUERY.matches(contentType)) {
					sparql.query(ctx, buffer);
				}
				else {
					ctx.fail(new BadRequestException("Must provide SPARQL query or update in correct content type"));
				}
			});
		}
		else {
			ctx.fail(new BadRequestException("Must specify SPARQL query or update"));
		}
	}
}
