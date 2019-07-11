package com.goodforgoodbusiness.endpoint;

import static org.apache.jena.graph.NodeFactory.createLiteral;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.storage.TripleContexts;
import com.goodforgoodbusiness.endpoint.storage.TripleContext.Type;
import com.goodforgoodbusiness.endpoint.storage.rocks.RocksManager;
import com.goodforgoodbusiness.endpoint.storage.rocks.context.TripleContextStore;

public class TripleContextTest {
	public static void main(String[] args) throws Exception {
		var t1 = new Triple(createLiteral("s1"), createLiteral("p1"), createLiteral("o1"));
		var t2 = new Triple(createLiteral("s1"), createLiteral("p1"), createLiteral("o2"));
		
		var rocksManager = new RocksManager("/Users/ijmad/Desktop/sccp/prototype/rocks");
		rocksManager.start();
		
		var manager = new TripleContexts(new TripleContextStore(rocksManager));
		
		manager
			.create(t1)
			.withType(Type.PRELOADED)
			.save()
		;
		
		var ctx1s = manager.getContexts(t1);
		ctx1s.forEach(ctx -> System.out.println("t1: " + ctx));
		
		var ctx2s = manager.getContexts(t2);
		ctx2s.forEach(ctx -> System.out.println("t2: " + ctx));
	}
}
