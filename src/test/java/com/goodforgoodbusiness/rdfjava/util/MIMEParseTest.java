package com.goodforgoodbusiness.rdfjava.util;

import java.util.LinkedList;

import com.goodforgoodbusiness.shared.MIMEParse;

public class MIMEParseTest {
	public static void main(String[] args) {
		var supported = new LinkedList<String>();
		supported.add("application/xml");
		
		System.out.println(
			MIMEParse.bestMatch(supported, null).length()
		);
	}
}
