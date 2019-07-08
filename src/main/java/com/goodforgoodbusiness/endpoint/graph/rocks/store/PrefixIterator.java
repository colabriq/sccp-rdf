package com.goodforgoodbusiness.endpoint.graph.rocks.store;

import static com.goodforgoodbusiness.endpoint.graph.rocks.store.PrefixPattern.startsWith;

import java.util.Iterator;

import org.rocksdb.RocksIterator;

/**
 * Iterates over a particular RocksDB prefix.
 * @author ijmad
 */
public class PrefixIterator implements Iterator<byte[]> {
	private final RocksIterator it;
	private final byte[] prefix;
	
	private byte[] curVal = null;
	
	public PrefixIterator(RocksIterator it, byte [] prefix) {
		this.it = it;	
		this.prefix = prefix;
		
		if (prefix != null) {
			it.seek(prefix);
		}
		else {
			it.seekToFirst();
		}
		
		updateCurrent();
	}
	
	public PrefixIterator(RocksIterator it) {
		this(it, null);
	}
	
	private void updateCurrent() {
		if (it.isValid() && (prefix == null || startsWith(it.key(), prefix))) {
			curVal = it.value();
		}
		else {
			curVal = null;
		}
	}
	
	@Override
	public boolean hasNext() {
		return curVal != null;
	}

	@Override
	public byte[] next() {
		var lastVal = curVal;
		
		it.next();
		updateCurrent();
		
		return lastVal;
	}

	public void close() {
		it.close();
	}
}
