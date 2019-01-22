package com.goodforgoodbusiness.rdfjava.util;

import java.net.URI;

import com.goodforgoodbusiness.shared.URIModifier;

public class URIModifierTest {
	public static void main(String[] args) throws Exception {
		System.out.println(
			URIModifier.from(new URI("http://localhost:8082")).build()
		);
		
		System.out.println(
			URIModifier.from(new URI("http://localhost:8082")).addParam("foo", "bar").build()
		);
		
		System.out.println(
			URIModifier.from(new URI("http://localhost:8082")).appendPath("/foo").build()
		);
	
		System.out.println(
			URIModifier.from(new URI("http://localhost:8082")).appendPath("foo/bar").build()
		);
		
		System.out.println(
			URIModifier.from(new URI("http://localhost:8082")).addParam("foo", "bar").appendPath("foo").build()
		);
		
		System.out.println(
			URIModifier.from(new URI("http://localhost:8082")).addParam("fo o", "b?ar").appendPath("foo").build()
		);
		
		System.out.println(
			URIModifier.from(new URI("http://localhost:8082/bar#fragment")).build()
		);
	}
}
