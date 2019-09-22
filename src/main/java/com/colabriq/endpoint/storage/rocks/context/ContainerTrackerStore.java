package com.colabriq.endpoint.storage.rocks.context;

import org.rocksdb.RocksDBException;

import com.colabriq.model.StorableContainer;
import com.colabriq.rocks.PrefixIterator;
import com.colabriq.rocks.RocksManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Stores containers we've seen so they're not processed twice (can lead to triples that have been deleted getting recreated)
 * @author ijmad
 *
 */
@Singleton
public class ContainerTrackerStore {
	private static final byte [] CONTAINER_CFH = "containers".getBytes();
	private static final byte [] TRUE = new byte [] { 1 }; // just tracking keys so use same value everywhere
	
	private RocksManager manager;
	
	@Inject
	public ContainerTrackerStore(RocksManager manager) throws RocksDBException {
		this.manager = manager;
		this.manager.start();
	}
	
	/**
	 * Mark container as having been seen + processed
	 */
	public void markSeen(StorableContainer container) {
		var id = container.getInnerEnvelope().getHashKey().getBytes();
		
		try {
			var cfh = manager.getOrCreateColFH(CONTAINER_CFH);
			manager.put(cfh, id, TRUE);
		}
		catch (RocksDBException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Check if a container has been seen + processed
	 */
	public boolean hasSeen(StorableContainer container) {
		var id = container.getInnerEnvelope().getHashKey().getBytes();
		
		try {
			var cfh = manager.getOrCreateColFH(CONTAINER_CFH);
			
			// use iterator but see if just one element is present
			var it = new PrefixIterator(manager.newIterator(cfh), id);
			
			try {
				return it.hasNext();
			}
			finally {
				it.close();
			}
		}
		catch (RocksDBException e) {
			throw new RuntimeException(e);
		}
	}
}
