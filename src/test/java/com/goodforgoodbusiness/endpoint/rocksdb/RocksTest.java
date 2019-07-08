package com.goodforgoodbusiness.endpoint.rocksdb;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class RocksTest {
	static {
		RocksDB.loadLibrary();
	}

	public static void main(String[] args) throws Exception {
		byte [] key = new byte [] { 1, 2, 3, };
		
		byte [] val = new byte [] { 4, 5, 6, };
		
		try (final Options options = new Options().setCreateIfMissing(true)) {
			try (final RocksDB db = RocksDB.open(options, "/Users/ijmad/Desktop/sccp/prototype/rocks")) {
				final byte[] got = db.get(key);
				
				if (got != null) {
					System.out.println("got = " + got);
				}
				else {
					System.out.println("got = null");
					db.put(key, val);
				}
				
				var est = db.getProperty("rocksdb.estimate-num-keys");
				System.out.println(est);
			}
		}
	}
}
