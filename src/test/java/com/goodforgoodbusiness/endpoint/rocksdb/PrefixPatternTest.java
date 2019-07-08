//package com.goodforgoodbusiness.endpoint.rocksdb;
//
//import static com.goodforgoodbusiness.endpoint.graph.rocks.store.PrefixPattern.o;
//import static com.goodforgoodbusiness.endpoint.graph.rocks.store.PrefixPattern.p;
//import static com.goodforgoodbusiness.endpoint.graph.rocks.store.PrefixPattern.s;
//import static org.apache.jena.graph.NodeFactory.createLiteral;
//
//import java.util.Arrays;
//
//import org.apache.jena.graph.Triple;
//
//import com.goodforgoodbusiness.endpoint.graph.rocks.store.PrefixPattern;
//import com.goodforgoodbusiness.model.TriTuple;
//
///**
// * Build index patterns for prefix store
// */
//public final class PrefixPatternTest {
//	public static void main(String[] args) {
//		var tt1 = TriTuple.from(new Triple(createLiteral("s1"), createLiteral("p1"), createLiteral("o1")));
//		
//		byte [] prefix1 = PrefixPattern.encode(s(tt1), p(tt1), o(tt1));
//		System.out.println(Arrays.toString(prefix1));
//		
//		byte [] prefix2 = PrefixPattern.encode(s(tt1), p(tt1));
//		System.out.println(Arrays.toString(prefix2));
//		
//		byte [] prefix3 = PrefixPattern.encode(s(tt1));
//		System.out.println(Arrays.toString(prefix3));
//	}
//}
