package com.colabriq.endpoint.dht.backend.impl;

import com.colabriq.rpclib.client.RPCWebClientCreator;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

/**
 * Provides the {@link WebClient} required for DHT RPC use.
 * @author ijmad
 */
@Singleton
public class DHTRPCWebClientProvider implements Provider<WebClient >{
	private final Vertx vertx;
	
	@Inject
	public DHTRPCWebClientProvider(Vertx vertx) {
		this.vertx = vertx;
	}
	
	@Override @Singleton 
	public WebClient get() {
		return RPCWebClientCreator.create(vertx);
	}
}
