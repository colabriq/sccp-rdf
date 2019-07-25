package com.goodforgoodbusiness.endpoint.webapp.share;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.share.ShareKeyStore;
import com.goodforgoodbusiness.endpoint.dht.share.ShareKeyStoreException;
import com.goodforgoodbusiness.endpoint.dht.share.ShareResponse;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class ShareAcceptHandler implements Handler<RoutingContext> {
	private static final Logger log = Logger.getLogger(ShareAcceptHandler.class);
	
	private final ShareKeyStore store;
	
	@Inject
	public ShareAcceptHandler(ShareKeyStore store) {
		this.store = store;
	}
	
	@Override
	public void handle(RoutingContext ctx) {
		ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getContentTypeString());
		
		try {
			var sar = JSON.decode(ctx.getBodyAsString(), ShareResponse.class);
			if (sar != null && sar != null) {
				log.info("Processing share accept " + sar.toString());
				
				store.saveKey(sar);
				//cache.invalidate(sar.pattern); // as we may now be able to decrypt more triples
			}
			
			ctx.response().end("\"OK\"");
		}
		catch (ShareKeyStoreException e) {
			ctx.fail(e);
		}
	}
}
