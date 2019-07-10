package com.goodforgoodbusiness.endpoint.graph.persistent.rocks.triples;

import static com.goodforgoodbusiness.endpoint.graph.persistent.rocks.triples.PrefixPattern.startsWith;

import java.util.Iterator;

import org.rocksdb.RocksIterator;

import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.triples.PrefixIterator.Row;
import com.goodforgoodbusiness.shared.TimingRecorder;
import com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory;

/**
 * Iterates over values matching a particular RocksDB prefix.
 * @author ijmad
 */
public class PrefixIterator implements Iterator<Row>, AutoCloseable {
	public static class Row {
		public final byte [] key, val;
		
		public Row(byte [] key, byte [] val) {
			this.key = key;
			this.val = val;
		}
	}
	
	private final RocksIterator it;
	private final byte[] prefix;
	
	private Row curRow = null;
	
	public PrefixIterator(RocksIterator it, byte [] prefix) {
		this.it = it;	
		this.prefix = prefix;
		
		try (var timer = TimingRecorder.timer(TimingCategory.RDF_DATABASE)) {
			if (prefix != null) {
				it.seek(prefix);
			}
			else {
				it.seekToFirst();
			}
		}
		
		updateCurrent();
	}
	
	public PrefixIterator(RocksIterator it) {
		this(it, null);
	}
	
	private void updateCurrent() {
		try (var timer = TimingRecorder.timer(TimingCategory.RDF_DATABASE)) {
			if (it.isValid() && (prefix == null || startsWith(it.key(), prefix))) {
				curRow = new Row(it.key(), it.value());
			}
			else {
				curRow = null;
			}
		}
	}
	
	@Override
	public boolean hasNext() {
		return curRow != null;
	}

	@Override
	public Row next() {
		var lastVal = curRow;
		
		it.next();
		updateCurrent();
		
		return lastVal;
	}

	@Override
	public void close() {
		it.close();
	}
}
