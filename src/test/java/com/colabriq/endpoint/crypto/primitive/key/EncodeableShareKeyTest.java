package com.colabriq.endpoint.crypto.primitive.key;

import com.colabriq.endpoint.crypto.key.EncodeableShareKey;
import com.colabriq.kpabe.KPABEEncryption;
import com.colabriq.kpabe.KPABEKeyManager;
import com.colabriq.shared.encode.JSON;

public class EncodeableShareKeyTest {
	public static void main(String[] args) throws Exception {
		var keys = KPABEKeyManager.newKeys();
		var abe = KPABEEncryption.getInstance(keys);
		
		var keyPair = abe.shareKey("foo");
		var encodeablePair = new EncodeableShareKey(keyPair);
		var json = JSON.encodeToString(encodeablePair);
		
		System.out.println(json);
		
		EncodeableShareKey keyPair2 = JSON.decode(json, EncodeableShareKey.class);
		
		System.out.println(keyPair2.getPublic().toString());
		System.out.println(keyPair2.getShare().toString());
	}
}
