package com.goodforgoodbusiness.endpoint.storage.rocks.context;

import static com.goodforgoodbusiness.shared.encode.RDFBinary.encodeTriple;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.rocksdb.RocksDBException;

import com.goodforgoodbusiness.endpoint.storage.TripleContext;
import com.goodforgoodbusiness.rocks.PrefixIterator;
import com.goodforgoodbusiness.rocks.PrefixIterator.Row;
import com.goodforgoodbusiness.rocks.RocksManager;
import com.goodforgoodbusiness.shared.encode.JSON;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Maintains the context store for Triples
 */
@Singleton
public class TripleContextStore {	
	protected static byte [] encodeContext(TripleContext tc) {
		return JSON.encodeToString(tc).getBytes();
	}
	
	protected static TripleContext decodeContext(Row row) {
		var tc = JSON.decode(new String(row.val), TripleContext.class);
		System.arraycopy(row.key, row.key.length - 16, tc.getID(), 0, 16);
		return tc;
	}
	
	private static final byte [] CONTEXT_CFH = "context".getBytes();
	
	private RocksManager manager;

	@Inject
	public TripleContextStore(RocksManager manager) throws RocksDBException {
		this.manager = manager;
		this.manager.start();
	}
	
	/**
	 * Retrieve the context object for a Triple if it exists
	 */
	public Set<TripleContext> getContexts(Triple triple) {
		var enc = encodeTriple(triple);
		
		try {
			var cfh = manager.getOrCreateColFH(CONTEXT_CFH);
			
			// contexts are stored with the encoded triple as a prefix and the ID of the context (random)
			var it = new PrefixIterator(manager.newIterator(cfh), enc);
			var ctxSet = new HashSet<TripleContext>();
			it.forEachRemaining(row -> ctxSet.add(decodeContext(row)));
			it.close();
			
			return ctxSet;
		}
		catch (RocksDBException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Add context for a Triple
	 */
	public void addContext(Triple triple, TripleContext context) {
		var enc = encodeTriple(triple);
		var val = encodeContext(context);
		
		try {
			var cfh = manager.getOrCreateColFH(CONTEXT_CFH);
			
			// triple + the id of the context is the key
			var key = new byte[enc.length + 16];
			System.arraycopy(enc, 0, key, 0, enc.length);
			System.arraycopy(context.getID(), 0, key, enc.length, 16);
			
			// store in database
			manager.put(cfh, key, val);
		}
		catch (RocksDBException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Remove context for a Triple
	 */
	public void removeContext(Triple triple, TripleContext context) {
		
	}
}
