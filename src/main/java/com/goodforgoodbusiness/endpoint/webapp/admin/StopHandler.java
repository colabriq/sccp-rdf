package com.goodforgoodbusiness.endpoint.webapp.admin;

import com.goodforgoodbusiness.endpoint.EndpointModule;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Allows safe stop of the system
 */
public class StopHandler implements Handler<RoutingContext> {
	private final EndpointModule module;
	
	public StopHandler(EndpointModule module) {
		this.module = module;
	}

	@Override
	public void handle(RoutingContext ctx) {
		ctx.response().end("OK");
		
		// run shutdown in new thread to avoid blocking Vert.x from shutting down
		// can't run it inside the ExecutorService either because that needs to shut down!
		var shutdownThread = new Thread(() -> module.shutdown(), "Shutdown Thread");
		shutdownThread.start();
	}
}
