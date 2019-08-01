package com.colabriq.endpoint.webapp.share;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.colabriq.endpoint.dht.share.ShareKeyStore;
import com.colabriq.endpoint.dht.share.SharePattern;
import com.colabriq.endpoint.dht.share.ShareRequest;
import com.colabriq.kpabe.KPABEException;
import com.colabriq.shared.encode.JSON;
import com.colabriq.webapp.ContentType;
import com.colabriq.webapp.error.BadRequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class ShareRequestHandler implements Handler<RoutingContext> {
	private static final Logger log = Logger.getLogger(ShareRequestHandler.class);
	
	private final ShareKeyStore keyStore;
	
	@Inject
	public ShareRequestHandler(ShareKeyStore keyStore) {
		this.keyStore = keyStore;
	}
	
	@Override
	public void handle(RoutingContext ctx) {
		ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getContentTypeString());
		
		var pattern = new SharePattern();
		
		pattern.setSubject(ctx.request().getParam("sub"));
		pattern.setPredicate(ctx.request().getParam("pre"));
		pattern.setObject(ctx.request().getParam("obj"));
		
		if (!pattern.getSubject().isPresent() && !pattern.getObject().isPresent()) {
			ctx.fail(new BadRequestException("Must specify at least subject and object"));
			return;
		}
		
		var request = new ShareRequest();
		request.setPattern(pattern);
		
		try {
			request.setStart(getDateTime(ctx.request().getParam("start")).orElse(null));
			request.setEnd(getDateTime(ctx.request().getParam("end")).orElse(null));
		}
		catch (BadRequestException e) {
			ctx.fail(e);
			return;
		}
		
		try {
			log.info("Creating share response for " + request);
			var response = keyStore.newShareKey(request);
			ctx.response().end(JSON.encodeToString(response));
		}
		catch (KPABEException e) {
			ctx.fail(e);
		}
	}
	
	private static Optional<ZonedDateTime> getDateTime(String input) throws BadRequestException {
		try {
			return Optional
				.ofNullable(input)
				.map(i -> LocalDateTime.parse(i, ISO_LOCAL_DATE_TIME).atZone(ZoneId.of("UTC")))
			;
		}
		catch (DateTimeParseException e) {
			throw new BadRequestException("Bad datetime", e);
		}
	}
}
