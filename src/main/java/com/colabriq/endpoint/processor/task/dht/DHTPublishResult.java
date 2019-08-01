package com.colabriq.endpoint.processor.task.dht;

import static java.util.stream.Collectors.counting;

import com.colabriq.endpoint.processor.ModelTaskResult;
import com.colabriq.model.StorableContainer;
import com.colabriq.shared.encode.JSON;

/**
 * Result of a DHT backed task includes the container it built
 */
public class DHTPublishResult extends ModelTaskResult {
	private final StorableContainer container;
	
	public DHTPublishResult(StorableContainer container, long modelSize) {
		super(
			container.getAdded().collect(counting()),
			container.getRemoved().collect(counting()),
			modelSize
		);
		
		this.container = container;
	}
	
	public StorableContainer getContainer() {
		return container;
	}
	
	@Override
	public String toJson() {
		return JSON.encodeToString(container);
	}
}
