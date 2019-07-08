package com.goodforgoodbusiness.endpoint.graph.persistent.rocks.store;

import static com.goodforgoodbusiness.endpoint.graph.persistent.rocks.store.PrefixPattern.makePrefix;
import static com.goodforgoodbusiness.shared.TripleUtil.isNone;
import static com.goodforgoodbusiness.shared.encode.RDFBinary.encodeNode;
import static com.goodforgoodbusiness.shared.encode.RDFBinary.encodeTriple;
import static org.apache.jena.graph.Node.ANY;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.rocksdb.RocksDBException;

import com.goodforgoodbusiness.endpoint.graph.persistent.rocks.RocksManager;

/**
 * A triple store backed by RocksDB
 */
public class RocksTripleStore implements TripleStore {
	// the triple store maintains four maps
	// triple components in different orders
	private static final byte [] SPO = "spo".getBytes();
	private static final byte [] OPS = "ops".getBytes();
	private static final byte [] POS = "pos".getBytes();
	private static final byte [] SOP = "sop".getBytes();
	
	private RocksManager manager;
	
	public RocksTripleStore(RocksManager manager) {
		this.manager = manager;
	}
	
	public RocksManager getManager() {
		return manager;
	}
	
	@Override
	public void add(Triple t) {
		try {
			// encode the whole triple for value storage
			var enc = encodeTriple(t);
			
			// encode individual parts of the triple
			var s = encodeNode(t.getSubject());
			var p = encodeNode(t.getPredicate());
			var o = encodeNode(t.getObject());
			
			// record triple patterns in the four arrangements
			// triples can then be looked up in various formats
			manager.put(manager.getOrCreateColFH(SPO), makePrefix(s, p, o), enc);
			manager.put(manager.getOrCreateColFH(OPS), makePrefix(o, p, s), enc);
			manager.put(manager.getOrCreateColFH(POS), makePrefix(p, o, s), enc);
			manager.put(manager.getOrCreateColFH(SOP), makePrefix(s, o, p), enc);
		}
		catch (RocksDBException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void delete(Triple pattern) {
		// look up a triple pattern
		var iter = find(pattern);
		
		try {
			// then delete any matches
			while (iter.hasNext()) {
				var t = iter.next();
				
				// encode individual parts of the triple
				var s = encodeNode(t.getSubject());
				var p = encodeNode(t.getPredicate());
				var o = encodeNode(t.getObject());
				
				// delete from four pattern stores
				manager.delete(manager.getOrCreateColFH(SPO), makePrefix(s, p, o));
				manager.delete(manager.getOrCreateColFH(OPS), makePrefix(o, p, s));
				manager.delete(manager.getOrCreateColFH(POS), makePrefix(p, o, s));
				manager.delete(manager.getOrCreateColFH(SOP), makePrefix(s, o, p));
			}
		}
		catch (RocksDBException e) {
			throw new RuntimeException(e);
		}
		finally {
			iter.close();
		}
	}
	
	@Override
	public void clear() {
		delete(new Triple(ANY, ANY, ANY));
	}
	
	@Override
	public boolean isEmpty() {
		// peek at the store, see if any results returned
		var iter = find(new Triple(ANY, ANY, ANY));
		
		try {
			return iter.hasNext();
		}
		finally {
			iter.close();
		}
	}
	
	@Override
	public int size() {
		try {
			// XXX this is an estimate only. check this will work ok.
			return manager.getNumKeys(manager.getOrCreateColFH(SPO));
		}
		catch (RocksDBException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Find a triple pattern by doing prefix matching on the underlying store.
	 */
	@Override
	public ExtendedIterator<Triple> find(Triple t) {
		byte [] cfhKey, prefix;
		if (isNone(t.getSubject())) {
			if (isNone(t.getPredicate())) {
				cfhKey = OPS;
				if (isNone(t.getObject())) {
					// (_, _, _) -> use any map, all triples
					prefix = null; // null prefix which will seek over all
				}
				else {
					// (_, _, O) -> use OPS map, prefix O
					prefix = makePrefix(encodeNode(t.getObject()));
				}
			}
			else {
				cfhKey = POS;
				if (isNone(t.getObject())) {
					// (_, P, _) -> use POS map, prefix P
					prefix = makePrefix(encodeNode(t.getPredicate()));
				}
				else {
					// (_, P, O) -> use POS map, prefix PO
					prefix = makePrefix(encodeNode(t.getPredicate()), encodeNode(t.getObject()));
				}
			}
		}
		else {
			if (isNone(t.getPredicate())) {
				cfhKey = SOP;
				if (isNone(t.getObject())) {
					// (S, _, _) -> use SPO map, prefix S
					prefix = makePrefix(encodeNode(t.getSubject()));
				}
				else {
					// (S, _, O) -> use SOP map, prefix SO
					prefix = makePrefix(encodeNode(t.getSubject()), encodeNode(t.getObject()));
				}
			}
			else {
				cfhKey = SPO;
				if (isNone(t.getObject())) {						
					// (S, P, _) -> use SPO map, prefix SP
					prefix = makePrefix(encodeNode(t.getSubject()), encodeNode(t.getPredicate()));
				}
				else {
					// (S, P, O) -> use SPO map, prefix SPO
					prefix = makePrefix(encodeNode(t.getSubject()), encodeNode(t.getPredicate()), encodeNode(t.getObject()));
				}
			}
		}
			
		try {
			return new TripleIterator(new PrefixIterator(manager.newIterator(manager.getOrCreateColFH(cfhKey)), prefix));
		}
		catch (RocksDBException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean contains(Triple t) {
		if (!t.isConcrete()) {
			throw new IllegalArgumentException("Triple to contains must be concrete");
		}
		
		var it = find(t);
		
		try {
			return it.hasNext();
		}
		finally {
			it.close();
		}
	}

	@Override
	public ExtendedIterator<Node> listSubjects() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ExtendedIterator<Node> listPredicates() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ExtendedIterator<Node> listObjects() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void close() {
	}
}
