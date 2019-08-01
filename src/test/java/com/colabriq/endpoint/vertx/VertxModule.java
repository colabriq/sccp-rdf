package com.goodforgoodbusiness.endpoint.vertx;

import static io.vertx.core.Vertx.vertx;

import java.util.function.Consumer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;

public class VertxModule extends AbstractVerticle {
	public static void main(String[] args) {
		var vertxOptions = new VertxOptions();
		vertxOptions.getEventBusOptions().setClustered(false);

		var deployOptions = new DeploymentOptions();

		Consumer<Vertx> runner = vertx -> vertx.deployVerticle(VertxModule.class, deployOptions);

		var vertx = vertx(vertxOptions);
		runner.accept(vertx);
	}

	@Override
	public void start() {
		var router = Router.router(vertx);

//		router.route().handler(BodyHandler.create());
		
		router.get("/foo")
			.handler(ctx -> {
				var response = ctx.response();
				response.putHeader("content-type", "text/plain").setChunked(true).write("fee\n");
				
				ctx.next();
			})
		;
		
		
		vertx.createHttpServer().requestHandler(router).listen(8080);
	}
}