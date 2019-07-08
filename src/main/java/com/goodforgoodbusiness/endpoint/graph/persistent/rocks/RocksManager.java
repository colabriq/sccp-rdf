package com.goodforgoodbusiness.endpoint.graph.persistent.rocks;

import static java.lang.Integer.parseInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Wrapper around RocksDB
 */
public class RocksManager {
	private static final Logger log = Logger.getLogger(RocksManager.class);
	
	private static class ByteArrayKey {
		private final byte[] array;
		private int hashCode;

		ByteArrayKey(byte [] array) {
			this.array = array;
			this.hashCode = Arrays.hashCode(array);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		@Override
		public boolean equals(Object o) {
			return (o == this) || 
				((o instanceof ByteArrayKey) && (o.hashCode() == hashCode()) && Arrays.equals(array,((ByteArrayKey)o).array));
		}
	}
	
	private final String path;
	
	private final List<ColumnFamilyDescriptor> initialCFDList;
	private final Map<ByteArrayKey, ColumnFamilyHandle> columnFamilyHandles = new HashMap<>();
	
	private DBOptions options = null;
	private RocksDB db = null;

	@Inject
	public RocksManager(@Named("rocks.path") String path) throws RocksDBException {
		this.path = path;
		
		this.initialCFDList = RocksUtils.getColumnFamilyDescriptors(path);
		
		// if a default CFD is not present, add it.
		// generally this means a new database
		boolean hasDefaultCFD = this.initialCFDList
			.stream()
			.filter( c -> Arrays.equals(c.columnFamilyName(), RocksDB.DEFAULT_COLUMN_FAMILY))
			.findFirst()
			.isPresent()
		;
		
		if (!hasDefaultCFD) {
			this.initialCFDList.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
		}
	}
	
	public void start() throws RocksDBException {
//		// add shutdown hook to ensure close
//		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//			close();
//		}));
		
		this.options = new DBOptions();
		this.options.setCreateIfMissing(true);
		
		var initialCFHList = new ArrayList<ColumnFamilyHandle>(initialCFDList.size());
		
		this.db = RocksDB.open(options, path, initialCFDList, initialCFHList);
		
		log.debug("RocksDB returned " + initialCFHList.size() + " column family handles");
		
		for (var x = 0; x < initialCFHList.size(); x++) {
			this.columnFamilyHandles.put(
				new ByteArrayKey(initialCFDList.get(x).columnFamilyName()),
				initialCFHList.get(x)
			);
		}
	}
	
	/**
	 * Return the DB instance that is currently opened.
	 */
	public RocksDB db() throws RocksDBException {
		if (db != null) {
			return db;
		}
		else {
			throw new RocksDBException("DB is not open");
		}
	}
	
	/**
	 * Get estimated number of keys in the database
	 */
	public int getNumKeys(ColumnFamilyHandle cfh) {
		try {
			return parseInt(db().getProperty(cfh, "rocksdb.estimate-num-keys"));
		}
		catch (RocksDBException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @see RocksDB#get(ColumnFamilyHandle, byte[])
	 */
	public byte[] get(ColumnFamilyHandle cfh, byte [] key) throws RocksDBException {
		return db().get(cfh, key);
	}
	
	/**
	 * @see RocksDB#newIterator()
	 */
	public RocksIterator newIterator(ColumnFamilyHandle columnFamilyHandle) throws RocksDBException {
		return db().newIterator(columnFamilyHandle);
	}
	
	/**
	 * @see RocksDB#put(ColumnFamilyHandle, byte[], byte[])
	 */
	public void put(ColumnFamilyHandle columnFamilyHandle, byte[] key, byte[] value) throws RocksDBException {
		db().put(columnFamilyHandle, key, value);
	}
	
	/**
	 * @see RocksDB#delete(ColumnFamilyHandle, byte[])
	 */
	public void delete(ColumnFamilyHandle columnFamilyHandle, byte[] key) throws RocksDBException {
		db().delete(columnFamilyHandle, key);
	}
	
	/**
	 * Return an existing Column family handle
	 */
	public ColumnFamilyHandle getColFH(byte [] key) {
		return columnFamilyHandles.get(new ByteArrayKey(key));
	}
	
	/**
	 * Return an existing Column family handle, or create if it doesn't yet exist
	 */
	public ColumnFamilyHandle getOrCreateColFH(byte [] key) throws RocksDBException {
		var barKey = new ByteArrayKey(key);
		
		if (columnFamilyHandles.containsKey(barKey)) {
			return columnFamilyHandles.get(barKey);
		}
		
		var cfh = db().createColumnFamily(new ColumnFamilyDescriptor(key));
		columnFamilyHandles.put(barKey, cfh);
		return cfh;
	}
	
	public void close() {
		if (options != null) {
			options.close();
		}
		
		if (db != null) {
			db.close();
		}
	}
}
