package com.colabriq.endpoint.crypto.primitive;

import com.colabriq.endpoint.crypto.SymmetricEncryption;
import com.colabriq.endpoint.crypto.key.EncodeableSecretKey;

public class SymmetricEncryptionTest {
	public static void main(String[] args) throws Exception {
		// create key from bytes
		EncodeableSecretKey key = SymmetricEncryption.createKey();
		
		var ciphertext = SymmetricEncryption.encrypt("hello world", key);
		System.out.println(ciphertext);
		
		System.out.println(key.toEncodedString());
		
		// decode
		var key2 = new EncodeableSecretKey(key.toEncodedString());
		
		System.out.println(SymmetricEncryption.decrypt(ciphertext, key2));
	}
}
