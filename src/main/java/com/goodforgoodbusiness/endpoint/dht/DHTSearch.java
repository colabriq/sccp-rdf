package com.goodforgoodbusiness.endpoint.dht;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableSecretKey;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

/**
 * Searcher brings together facilities from the Weft and Warp to find containers
 */
@Singleton
public class DHTSearch {
	private static final Logger log = Logger.getLogger(DHTSearch.class);
	
	private final DHTWarpDriver warp;
	private final DHTWeftDriver weft;
	
	@Inject
	public DHTSearch(DHTWarpDriver warp, DHTWeftDriver weft) {
		this.warp = warp;
		this.weft = weft;
	}
	
//							try {
//								// attempt fetch & decrypt
//								return .stream(); // stream so it's just non-empty Optionals
//							}
//							catch (EncryptionException e) {
//								log.error("Couldn not decrypt container", e);
//								return Stream.empty();
//							}

	
	/**
	 * Search the warp + weft for a triple pattern
	 */
	public void search(TriTuple tuple, Future<Stream<StorableContainer>> future) {
		if (log.isDebugEnabled()) {
			log.debug("Searching DHT for " + tuple);
		}

		// process the stream because these operations have side effects
		warp.search(tuple, Future.<Stream<Pointer>>future().setHandler(
			warpResults -> {
				if (warpResults.succeeded()) {
					@SuppressWarnings("rawtypes") // CompositeFuture's generics are broken
					List<Future> fetchFutures = 
						warpResults.result()
							.map(ptr -> {
								// fetch from warp, collect futures
								var fetchFuture = Future.<Optional<StorableContainer>>future();
								
								weft.fetch(
									ptr.getContainerId(),
									new EncodeableSecretKey(ptr.getContainerKey()),
									fetchFuture
								);
								
								return fetchFuture;
							})
							.collect(Collectors.toList())
					;
					
					// wait for futures, process results
					CompositeFuture.all(fetchFutures).setHandler(fetchResults -> {
						var containers = new ArrayList<StorableContainer>(fetchFutures.size());
						
						if (fetchResults.succeeded()) {
							for (var x = 0; x < fetchResults.result().size(); x++) {
								Optional<StorableContainer> container = fetchResults.result().resultAt(x);
								if (container.isPresent()) {
									containers.add(container.get());
								}
							}
							
							if (log.isDebugEnabled()) {
								log.debug("Containers found = " + containers.size());
							}
							
							// return containers we've found
							future.complete(containers.parallelStream());
						}
						else {
							future.fail(fetchResults.cause());
						}
					});
				}
				else {
					future.fail(warpResults.cause());
				}
			}
		));
	}
}
