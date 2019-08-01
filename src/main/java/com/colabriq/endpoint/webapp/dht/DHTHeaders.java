package com.colabriq.endpoint.webapp.dht;

import static java.nio.charset.Charset.defaultCharset;
import static org.apache.http.client.utils.URLEncodedUtils.parse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.http.NameValuePair;

import com.colabriq.model.Link;
import com.colabriq.model.Link.RelType;
import com.colabriq.model.SubmittableContainer.SubmitMode;

import io.vertx.ext.web.RoutingContext;

class DHTHeaders {
	public static final String CUSTODY_CHAIN_HEADER = "X-Custody-Chain";
	
	/**
	 * Add any specified chain of custody headers in to the link structure.
	 * 
	 * X-Custody header looks like this:
	 * 
	 * X-Custody: ref=d41d8cd98f00b204e9800998ecf8427e&rel=causedBy ; ref=cf84e9800d4198f00b20998e427ed8cd&rel=causedBy ;
	 */
	public static Stream<Link> processCustodyChainHeader(RoutingContext ctx) {
		return processCustodyChainHeader(ctx.request().getHeader(CUSTODY_CHAIN_HEADER));
	}
	
	/**
	 * Add any specified chain of custody headers in to the link structure.
	 * 
	 * X-Custody header looks like this:
	 * 
	 * X-Custody: ref=d41d8cd98f00b204e9800998ecf8427e&rel=causedBy ; ref=cf84e9800d4198f00b20998e427ed8cd&rel=causedBy ;
	 */
	public static Stream<Link> processCustodyChainHeader(String header) {
		if (header != null && header.length() > 0) {
			return Stream.<String>of(header.split(";"))
				.map(String::trim)
				.map(str -> parse(str, defaultCharset()))
				.map(DHTHeaders::toLink)
				.flatMap(Optional::stream)
				
			;
		}
		
		return Stream.empty();
	}
	
	private static Optional<Link> toLink(List<NameValuePair> pairs) {
		var ref = pairs.stream()
			.filter(nvp -> nvp.getName().equals("ref"))
			.map(nvp -> nvp.getValue().trim())
			.findFirst()
		;
		
		var rel = pairs.stream()
			.filter(nvp -> nvp.getName().equals("rel"))
			.flatMap(nvp -> {
				try {
					return Optional.ofNullable(RelType.fromUri(nvp.getValue().trim())).stream();
				}
				catch (IllegalArgumentException e) {
					return Stream.empty();
				}
			})
			.findFirst()
		;
		
		if (ref.isPresent() && rel.isPresent()) {
			return Optional.of(new Link(ref.get(), rel.get()));
		}
		else {
			return Optional.empty();
		}
	}
	
	public static final String PUBLISH_MODE_HEADER = "X-Publish-Mode";
	
	public static Optional<SubmitMode> processSubmitModeHeader(RoutingContext ctx) {
		return processSubmitModeHeader(ctx.request().getHeader(PUBLISH_MODE_HEADER));
	}

	public static Optional<SubmitMode> processSubmitModeHeader(String header) {
		if (header != null) {
			try {
				return Optional.of(SubmitMode.valueOf(header.toUpperCase()));
			}
			catch (IllegalArgumentException e) {
				return Optional.empty();
			}
		}
		else {
			return Optional.empty();
		}
	}
}
