//package com.goodforgoodbusiness.rdfjava.dht;
//
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//
//import org.apache.jena.graph.Node;
//import org.apache.jena.graph.Triple;
//
//import com.goodforgoodbusiness.shared.model.AccessibleClaim;
//import com.goodforgoodbusiness.shared.model.SubmittedClaim;
//
//public class DHTTriple extends Triple {
//	public static DHTTriple wrap(Triple t) {
//		if (t instanceof DHTTriple) {
//			return (DHTTriple)t;
//		}
//		else {
//			return new DHTTriple(
//				t.getSubject(),
//				t.getPredicate(),
//				t.getObject()
//			);
//		}
//	}
//	
//	public static DHTTriple wrap(AccessibleClaim c, Triple t) {
//		var dt = wrap(t);
//		dt.claims.add(c);
//		return dt;
//	}
//	
//	private final List<AccessibleClaim> claims = new LinkedList<>();
//	
//	public DHTTriple(Node s, Node p, Node o) {
//		super(s, p, o);
//	}
//	
//	public void addClaim(SubmittedClaim claim) {
//		claims.add(claim);
//	}
//	
//	public List<AccessibleClaim> getClaims() {
//		return Collections.unmodifiableList(claims);
//	}
//}
