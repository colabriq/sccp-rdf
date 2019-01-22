package com.goodforgoodbusiness.rdfjava.dht;

import static java.util.Collections.singletonList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.shared.ContentType;
import com.goodforgoodbusiness.shared.JSON;
import com.goodforgoodbusiness.shared.URIModifier;
import com.goodforgoodbusiness.shared.model.StoredClaim;
import com.goodforgoodbusiness.shared.model.SubmitResult;
import com.goodforgoodbusiness.shared.model.SubmittableClaim;
import com.goodforgoodbusiness.shared.model.SubmittedClaim;
import com.goodforgoodbusiness.shared.treesort.TreeSort;

public class DHTClient {
	private static final Logger log = Logger.getLogger(DHTClient.class);
	
	private static final URI DHT_ENDPOINT;
	
	static {
		try {
			DHT_ENDPOINT = new URI("http://localhost:8090");
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final String MATCHES_PATH = "/matches";
	private static final String CLAIMS_PATH = "/claims";
	
	
	
	private static final HttpClient HTTP_CLIENT = 
		HttpClient.newBuilder().build();
	
	
	
	public static List<StoredClaim> matches(Triple trup) throws URISyntaxException, IOException, InterruptedException {
		log.info("Finding matches for: " + trup);
		
		var uri = URIModifier
			.from(DHT_ENDPOINT)
			.appendPath(MATCHES_PATH)
			.addParam("pattern", JSON.encode(trup).toString())
			.build();
		
		var request = HttpRequest
			.newBuilder(uri)
			.GET()
			.build();
		
		var response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
		
		if (response.statusCode() == 200) {
			List<StoredClaim> claims = StoredClaim.fromJson(response.body());
			return TreeSort.sort(claims, true);
		}
		else {
			throw new IOException("DHT response was " + response.statusCode());
		}
	}
	
	public static List<SubmittedClaim> submit(SubmittableClaim claim) throws URISyntaxException, IOException, InterruptedException {
		log.info("Submitting claim: " + claim);
		
		var uri = URIModifier
			.from(DHT_ENDPOINT)
			.appendPath(CLAIMS_PATH)
			.build();

		var request = HttpRequest
			.newBuilder(uri)
			.header("Content-Type", ContentType.json.getContentTypeString())
			.POST(BodyPublishers.ofString(JSON.encode(claim).toString()))
			.build();
		
		var response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
		
		if (response.statusCode() == 200) {
			// leave the door open for multiple claims?
			return singletonList(new SubmittedClaim(
				claim,
				JSON.decode(response.body(), SubmitResult.class)
			));
		}
		else {
			throw new IOException("DHT response was " + response.statusCode());
		}
	}
}
