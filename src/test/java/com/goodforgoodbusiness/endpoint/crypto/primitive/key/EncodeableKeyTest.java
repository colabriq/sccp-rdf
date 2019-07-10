package com.goodforgoodbusiness.endpoint.crypto.primitive.key;

import com.goodforgoodbusiness.endpoint.crypto.SymmetricEncryption;
import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableSecretKey;
import com.goodforgoodbusiness.shared.encode.JSON;

public class EncodeableKeyTest {
	public static void main(String[] args) {
		var key = SymmetricEncryption.createKey();
		var json = JSON.encodeToString(key);
		
		System.out.println(json);
		
		JSON.decode(json, EncodeableSecretKey.class);
	}
}
