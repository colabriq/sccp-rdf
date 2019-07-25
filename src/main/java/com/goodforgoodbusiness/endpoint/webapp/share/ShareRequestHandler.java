package com.goodforgoodbusiness.endpoint.webapp.share;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.share.ShareManager;
import com.goodforgoodbusiness.endpoint.dht.share.ShareRequest;
import com.goodforgoodbusiness.kpabe.KPABEException;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class ShareRequestHandler implements Handler<RoutingContext> {
	private static final Logger log = Logger.getLogger(ShareRequestHandler.class);
	
	private final ShareManager keyManager;
	
	@Inject
	public ShareRequestHandler(ShareManager keyManager) {
		this.keyManager = keyManager;
	}
	
	@Override
	public void handle(RoutingContext ctx) {
		ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getContentTypeString());
		
		var request = new ShareRequest();
		
		try {
			request.setStart(getDateTime(ctx.request().getParam("start")).orElse(null));
			request.setEnd(getDateTime(ctx.request().getParam("end")).orElse(null));
			request.setSubject(ctx.request().getParam("sub"));
			request.setPredicate(ctx.request().getParam("pre"));
			request.setObject(ctx.request().getParam("obj"));
		}
		catch (BadRequestException e) {
			ctx.fail(e);
			return;
		}
		
		if (!request.getSubject().isPresent() && !request.getObject().isPresent()) {
			ctx.fail(new BadRequestException("Must specify at least subject and object"));
			return;
		}
		
		try {
			log.info("Creating share response for " + request);
			var response = keyManager.newShareKey(request);
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
