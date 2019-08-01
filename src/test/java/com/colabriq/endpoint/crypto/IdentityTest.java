package com.colabriq.endpoint.crypto;

import com.colabriq.endpoint.crypto.Identity;

public class IdentityTest {
	public static void main(String[] args) throws Exception {
		var kp = AsymmetricEncryption.createKeyPair();
		
		System.out.println("private = " + kp.getPrivate().toEncodedString());
		System.out.println("public = " + kp.getPublic().toEncodedString());
		
		var id = new Identity("blah", kp.getPrivate().toEncodedString(), kp.getPublic().toEncodedString());
		
		var sig = id.sign("foo");
		
		System.out.println(id.verify("foo", sig));
	}
}
