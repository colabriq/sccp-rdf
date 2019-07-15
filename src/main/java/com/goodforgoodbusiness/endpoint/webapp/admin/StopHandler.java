package com.goodforgoodbusiness.endpoint.webapp.admin;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Allows safe stop of the system
 */
public class StopHandler implements Handler<RoutingContext> {
	@Override
	public void handle(RoutingContext ctx) {
		ctx.response().end("OK");
		
		
	}
}
