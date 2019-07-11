package com.goodforgoodbusiness.endpoint.dht;

import java.util.HashSet;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.crypto.EncryptionException;
import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableSecretKey;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.TriTuple;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
	
	/**
	 * Search the warp + weft for a triple pattern
	 */
	public Stream<StorableContainer> search(TriTuple tuple) {
		if (log.isDebugEnabled()) {
			log.debug("Searching DHT for " + tuple);
		}

		// process the stream because these operations have side effects.
		var containers = new HashSet<StorableContainer>();
		warp.search(tuple)
			// only work on containers we've not already seen/stored.
//			.filter(pointer -> !store.contains(pointer.getContainerId()))
			.flatMap(pointer -> {
				try {
					// attempt fetch & decrypt
					return weft.fetch(
						pointer.getContainerId(),
						new EncodeableSecretKey(pointer.getContainerKey())
					).stream(); // stream so it's just non-empty Optionals
				}
				catch (EncryptionException e) {
					log.error("Couldn not decrypt container", e);
					return Stream.empty();
				}
			})
			.forEach(container -> {
				containers.add(container);
			});
		
		if (log.isDebugEnabled()) {
			log.debug("Containers found = " + containers.size());
		}
		
		return containers.parallelStream();
	}
}
