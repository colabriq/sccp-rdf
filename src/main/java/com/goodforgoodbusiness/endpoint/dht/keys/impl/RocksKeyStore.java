package com.goodforgoodbusiness.endpoint.dht.keys.impl;

import static com.goodforgoodbusiness.shared.encode.RDFBinary.encodeTriple;

import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.RocksDBException;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.goodforgoodbusiness.endpoint.dht.keys.ShareKeyStore;
import com.goodforgoodbusiness.kpabe.key.KPABEPublicKey;
import com.goodforgoodbusiness.model.TriTuple;
import com.goodforgoodbusiness.rocks.RocksManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * RocksDB-backed implementation of the {@link ShareKeyStore}.
 * @author ijmad
 */
@Singleton
public class RocksKeyStore implements ShareKeyStore {
	private final RocksManager dbm;
	private final ColumnFamilyHandle tripleCFH;
	private final ColumnFamilyHandle keyCFH;
	
	@Inject
	public RocksKeyStore(RocksManager dbm) throws RocksDBException {
		this.dbm = dbm;
		this.tripleCFH = this.dbm.getOrCreateColFH("SK_TRIPLES".getBytes());
		this.keyCFH = this.dbm.getOrCreateColFH("SK_KEYS".getBytes());
	}
	
	@Override
	public Stream<KPABEPublicKey> knownContainerCreators(Triple pattern) {
		// get: triple -- result: public
		
//		pattern.matchingCombinations();
		
		var enc = encodeTriple(pattern);
	}
	
	@Override
	public Stream<EncodeableShareKey> keysForDecrypt(KPABEPublicKey publicKey, Triple tuple) {
		// get: public -- result: share key
	}
	
	@Override
	public void saveKey(Triple tuple, EncodeableShareKey shareKey) {
		
		// store: tuple -> public
		
		
		// store: public -> share key
		
	}
}
