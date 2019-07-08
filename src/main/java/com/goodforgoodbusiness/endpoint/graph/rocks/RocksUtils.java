package com.goodforgoodbusiness.endpoint.graph.rocks;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class RocksUtils {
	/**
	 * Find what column families exist in a RocksDB database
	 */
	public static List<ColumnFamilyDescriptor> getColumnFamilyDescriptors(String dbPath) throws RocksDBException {
		try (var options = new Options()) {
			options.setCreateIfMissing(false);
			
			return RocksDB
				.listColumnFamilies(options, dbPath)
				.stream()
				.map(ColumnFamilyDescriptor::new)
				.collect(toList())
			;
		}
	}
}
