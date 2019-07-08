package com.goodforgoodbusiness.endpoint.rocksdb;

import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.RocksManager;
import com.goodforgoodbusiness.shared.LogConfigurer;

public class RocksManagerTest {
	public static void main(String[] args) throws Exception {
		LogConfigurer.init(RocksManagerTest.class, "log4j.properties");
		
		var manager = new RocksManager("/Users/ijmad/Desktop/sccp/prototype/rocks");
		
		manager.start();
		
		var cfh = manager.getOrCreateColFH(new byte [] { 1, 2, 3, 4, 5 });
	}
}
