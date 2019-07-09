package com.goodforgoodbusiness.endpoint.processor.task.dht;

import static java.util.stream.Collectors.counting;

import com.goodforgoodbusiness.endpoint.processor.TaskResult;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.shared.encode.JSON;

/**
 * Result of a DHT backed task includes the container it built
 */
public class DHTSubmitResult extends TaskResult {
	private final StorableContainer container;
	
	public DHTSubmitResult(StorableContainer container, long modelSize) {
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
