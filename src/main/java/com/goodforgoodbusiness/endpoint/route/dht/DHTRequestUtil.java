package com.goodforgoodbusiness.endpoint.route.dht;

import static java.nio.charset.Charset.defaultCharset;
import static org.apache.http.client.utils.URLEncodedUtils.parse;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.http.NameValuePair;

import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.Link.RelType;

import spark.Request;

class DHTRequestUtil {
	public static final String CUSTODY_CHAIN_HEADER = "X-Custody-Chain";
	
	/**
	 * Add any specified chain of custody headers in to the link structure.
	 * 
	 * X-Custody header looks like this:
	 * 
	 * X-Custody: ref=d41d8cd98f00b204e9800998ecf8427e&rel=causedBy ; ref=cf84e9800d4198f00b20998e427ed8cd&rel=causedBy ;
	 */
	public static Stream<Link> processCustodyChainHeader(Request req) {
		return processCustodyChainHeader(req.headers(CUSTODY_CHAIN_HEADER));
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
			return Stream.<String>of((String[])header.split(";"))
				.map(String::trim)
				.map(str -> parse(str, defaultCharset()))
				.map(DHTRequestUtil::toLink)
				.flatMap(Optional::stream)
				
			;
		}
		else {
			return Stream.empty();
		}
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
}
