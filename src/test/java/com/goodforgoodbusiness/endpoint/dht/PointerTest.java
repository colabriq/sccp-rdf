package com.goodforgoodbusiness.endpoint.dht;

import static com.goodforgoodbusiness.shared.encode.Hash.sha512;
import static org.apache.jena.graph.NodeFactory.createURI;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.crypto.SymmetricEncryption;
import com.goodforgoodbusiness.endpoint.dht.share.ShareManager;
import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerAttributes;
import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerPatterns;
import com.goodforgoodbusiness.kpabe.KPABEEncryption;
import com.goodforgoodbusiness.kpabe.KPABEKeyManager;
import com.goodforgoodbusiness.model.Pointer;
import com.goodforgoodbusiness.shared.encode.CBOR;
import com.goodforgoodbusiness.shared.encode.Hex;
import com.goodforgoodbusiness.shared.encode.JSON;

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
		
		var shareManager = new ShareManager(keys.getPublic(), keys.getSecret());
		
		tts.stream().forEach(tt -> {
			ContainerPatterns.forPublish(shareManager, tt).forEach(pattern -> {
				System.out.println("Pattern: " + pattern);
			});;
		});
	}
}
