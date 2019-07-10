package com.goodforgoodbusiness.endpoint.temp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.shared.encode.Hash;
import com.goodforgoodbusiness.shared.encode.Hex;
import com.google.inject.Singleton;

@Singleton
public class MemDHTBackend implements DHTBackend { 
	private static final Logger log = Logger.getLogger(MemDHTBackend.class);
	
	private Map<String, Set<String>> keysMap = new HashMap<>();
	private Map<String, String> dataMap = new HashMap<>();

	@Override
	public String publish(Set<String> keywords, String data) {
		log.info("PUBLISH " + keywords.toString() + " -> " + data);
		
		String location = Hex.encode(Hash.sha512(data.getBytes()));
		dataMap.put(location, data);
		
		keywords.forEach(keyword -> {
			var existing = keysMap.get(keyword);
			if (existing != null) {
				existing.add(location);
			}
			else {
				var newSet = new HashSet<String>();
				newSet.add(location);
				keysMap.put(keyword, newSet);
			}
		});
		
		return location;
	}
	
	@Override
	public Stream<String> search(String keyword) {
		log.info("SEARCH " + keyword);
		
		return Optional
			.ofNullable(keysMap.get(keyword))
			.stream() // stream so it's just non-empty Optionals
			.flatMap(Set::stream)
		;
	}
	
	@Override
	public Optional<String> fetch(String location) {
		log.info("FETCH " + location);
		
		return Optional.ofNullable(dataMap.get(location));
	}
}
