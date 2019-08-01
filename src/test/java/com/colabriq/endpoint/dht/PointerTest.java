package com.colabriq.endpoint.dht;

import static com.colabriq.shared.encode.Hash.sha512;
import static org.apache.jena.graph.NodeFactory.createURI;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.jena.graph.Triple;

import com.colabriq.endpoint.crypto.SymmetricEncryption;
import com.colabriq.endpoint.dht.share.backend.impl.MemKeyStore;
import com.colabriq.endpoint.graph.containerized.ContainerAttributes;
import com.colabriq.endpoint.graph.containerized.ContainerPatterns;
import com.colabriq.kpabe.KPABEEncryption;
import com.colabriq.kpabe.KPABEKeyManager;
import com.colabriq.model.Pointer;
import com.colabriq.shared.encode.CBOR;
import com.colabriq.shared.encode.Hex;
import com.colabriq.shared.encode.JSON;

public class PointerTest {
	private static final Random RANDOM = new Random();
	
	public static void main(String[] args) throws Exception {
		var id = Hex.encode(sha512(CBOR.forObject("test")));
		var secretKey = SymmetricEncryption.createKey();
		
		var pointer = new Pointer(
			id,
			secretKey.toEncodedString(),
			RANDOM.nextLong()
		);
		
		var keys = KPABEKeyManager.newKeys();
		
		var tts = List.of(
			new Triple(createURI("A"), createURI("H"), createURI("I")),
			new Triple(createURI("B"), createURI("G"), createURI("J")),
			new Triple(createURI("C"), createURI("F"), createURI("K")),
			new Triple(createURI("D"), createURI("E"), createURI("L"))
		);
		
		var attributes = ContainerAttributes.forPublish(keys.getPublic(), tts.stream());		
		
		var kpabe = KPABEEncryption.getInstance(keys);
		var bytes = kpabe.encrypt(JSON.encodeToString(pointer), attributes).getBytes();
		
		System.out.println(Arrays.toString(bytes));
		System.out.println(bytes.length);
		
		tts.stream().forEach(tt -> {
			ContainerPatterns.forPublish(keys.getPublic(), tt).forEach(pattern -> {
				System.out.println("Pattern: " + pattern);
			});;
		});
	}
}
