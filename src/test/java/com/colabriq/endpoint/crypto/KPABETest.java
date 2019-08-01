package com.colabriq.endpoint.crypto;

import com.colabriq.kpabe.KPABEKeyManager;

public class KPABETest {
	public static void main(String[] args) throws Exception {
		var instance = KPABEKeyManager.newKeys();
		
		System.out.println("public = " + instance.getPublic().toString());
		System.out.println("secret = " + instance.getSecret().toString());
	}
}
