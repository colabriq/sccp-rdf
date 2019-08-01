package com.colabriq.endpoint.dht.backend.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import com.colabriq.endpoint.dht.backend.DHTBackend;
import com.google.inject.Singleton;

import io.vertx.core.Future;

/** In memory implementation of DHTBackend - for testing */
@Singleton
public class DHTMemBackend implements DHTBackend { 
	private static final Logger log = Logger.getLogger(DHTMemBackend.class);
	
	private Map<String, Set< byte[]>> pointers = new HashMap<>();

	@Override
	public void publishPointer(String hashPattern, byte[] data, Future<Void> future) {
		log.info("PUBLISH POINTER " + hashPattern.toString() + " -> " + data);
		
		pointers.computeIfAbsent(hashPattern, (k) -> new HashSet<>());
		pointers.get(hashPattern).add(data);
		
		future.complete();
	}

	@Override
	public void searchForPointers(String hashPattern, Future<Stream<byte[]>> future) {
		log.info("SEARCH FOR POINTERS " + hashPattern);
		
		var stream = Optional
			.ofNullable(pointers.get(hashPattern))
			.stream() // stream so it's just non-empty Optionals
			.flatMap(Set::stream)
		;
		 
		future.complete(stream);
	}

	private Map<String, Set<String>> containerIDs = new HashMap<>();
	private Map<String, byte[]> containerData = new HashMap<>();
	
	@Override
	public void publishContainer(String id, byte[] data, Future<String> future) {
		log.info("PUBLISH CONTAINER " + id);
		
		var location = RandomStringUtils.random(20, true, true);
		
		containerData.put(location, data);
		containerIDs.computeIfAbsent(id, (k) -> new HashSet<>());
		containerIDs.get(id).add(location);
		
		future.complete(location);		
	}

	@Override
	public void searchForContainer(String id, Future<Stream<String>> future) {
		log.info("SEARCH FOR CONTAINER " + id);
		
		future.complete(
			Optional
				.ofNullable(containerIDs.get(id))
				.stream() // stream so it's just non-empty Optionals
				.flatMap(Set::stream)
		);
	}

	@Override
	public void fetchContainer(String location, Future<Optional<byte[]>> future) {
		log.info("FETCH CONTAINER " + location);
		future.complete(Optional.ofNullable(containerData.get(location)));
	}
}
