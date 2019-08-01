package com.colabriq.endpoint.webapp;

import com.colabriq.webapp.error.BadRequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Handle query param SPARQL queries and updates.
 * As per SPARQL spec.
 * @author ijmad
 */
@Singleton
public class SparqlGetHandler implements Handler<RoutingContext> {
	private final SparqlTaskLauncher sparql;
	
	@Inject
	public SparqlGetHandler(SparqlTaskLauncher sparql) {
		this.sparql = sparql;
	}
	
	@Override
	public void handle(RoutingContext ctx) {
		var query = ctx.request().getParam("query");
		if (query != null) {
			sparql.query(ctx, query);
		}
		else {
			var update = ctx.request().getParam("update");
			if (update != null) {
				sparql.update(ctx, update);
			}
			else {
				ctx.fail(new BadRequestException("Must specify SPARQL query or update"));
			}
		}
	}
}
