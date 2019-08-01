package com.colabriq.endpoint.crypto.primitive.key;

import com.colabriq.endpoint.crypto.SymmetricEncryption;
import com.colabriq.endpoint.crypto.key.EncodeableSecretKey;
import com.colabriq.shared.encode.JSON;

public class EncodeableKeyTest {
	public static void main(String[] args) {
		var key = SymmetricEncryption.createKey();
		var json = JSON.encodeToString(key);
		
		System.out.println(json);
		
		JSON.decode(json, EncodeableSecretKey.class);
	}
}
