package com.goodforgoodbusiness.endpoint.storage.rocks.triples;

/**
 * Build index patterns for prefix store
 */
public final class PrefixPattern {
	private static final byte [] SPACER = new byte [] { 0, 0, 0, };
	
	/** 
	 * Combine a number of elements into a standardised prefix format
	 * One node version (e.g. S, P, O)
	 */
	public static byte [] makePrefix(byte [] node1) {
		var dest = new byte[node1.length + SPACER.length];
		var destPos = 0;
		
		System.arraycopy(node1, 	0,	dest,	destPos,	node1.length);
		destPos += node1.length;
		
		System.arraycopy(SPACER, 	0, 	dest,	destPos,	SPACER.length);
		
		return dest;
	}
	
	/** 
	 * Combine a number of elements into a standardised prefix format
	 * Two node version (e.g. SP, PS, OP, ...)
	 */
	public static byte [] makePrefix(byte [] node1, byte [] node2) {
		var dest = new byte[node1.length + SPACER.length + node2.length + SPACER.length];
		var destPos = 0;
		
		System.arraycopy(node1, 	0,	dest,	destPos,	node1.length);
		destPos += node1.length;
		
		System.arraycopy(SPACER, 	0, 	dest,	destPos,	SPACER.length);
		destPos += SPACER.length;
		
		System.arraycopy(node2, 	0,	dest,	destPos,	node2.length);
		destPos += node2.length;
		
		System.arraycopy(SPACER, 	0, 	dest,	destPos,	SPACER.length);
		
		return dest;
	}
	
	/** 
	 * Combine a number of elements into a standardised prefix format
	 * Three node version (e.g. SPO, POS, OPS, ...)
	 */
	public static byte [] makePrefix(byte [] node1, byte [] node2, byte [] node3) {
		// no trailing space for 3 node version because these represent complete triples
		var dest = new byte[node1.length + SPACER.length + node2.length + SPACER.length + node3.length];
		var destPos = 0;
		
		System.arraycopy(node1, 	0,	dest,	destPos,	node1.length);
		destPos += node1.length;
		
		System.arraycopy(SPACER, 	0, 	dest,	destPos,	SPACER.length);
		destPos += SPACER.length;
		
		System.arraycopy(node2, 	0,	dest,	destPos,	node2.length);
		destPos += node2.length;
		
		System.arraycopy(SPACER, 	0, 	dest,	destPos,	SPACER.length);
		destPos += SPACER.length;
		
		System.arraycopy(node3, 	0,	dest,	destPos,	node3.length);
		
		return dest;
	}
	
	private PrefixPattern() {
	}
}
