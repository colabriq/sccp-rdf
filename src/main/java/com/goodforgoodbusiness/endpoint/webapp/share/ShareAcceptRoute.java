//package com.goodforgoodbusiness.endpoint.webapp.share;
//
//import org.apache.log4j.Logger;
//
//import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
//import com.goodforgoodbusiness.model.TriTuple;
//import com.goodforgoodbusiness.shared.encode.JSON;
//import com.google.gson.annotations.Expose;
//import com.google.gson.annotations.SerializedName;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//
//import io.vertx.core.Handler;
//import io.vertx.ext.web.RoutingContext;
//
//@Singleton
//public class ShareAcceptRoute implements Handler<RoutingContext> {
//	private static final Logger log = Logger.getLogger(ShareAcceptRoute.class);
//	
//	public static class ShareAcceptRequest {
//		@Expose
//		@SerializedName("pattern")
//		private TriTuple pattern;
//		
//		@Expose
//		@SerializedName("key")
//		private EncodeableShareKey key;
//	}
//	
//	private final ShareKeyStore store;
//	private final Governer cache;
//	
//	@Inject
//	public ShareAcceptRoute(ShareKeyStore store, Governer cache) {
//		this.store = store;
//		this.cache = cache;
//	}
//	
//	@Override
//	public void handle(RoutingContext ctx) {		
//		var sar = JSON.decode(req.body(), ShareAcceptRequest.class);
//		if (sar != null && sar.key != null) {
//			log.info("Processing share accept for " + sar.pattern);
//			
//			store.saveKey(sar.pattern, sar.key);
//			cache.invalidate(sar.pattern); // as we may now be able to decrypt more triples
//		}
//		
//		return "OK";
//	}
//}
