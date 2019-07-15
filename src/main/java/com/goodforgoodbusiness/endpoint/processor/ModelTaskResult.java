package com.goodforgoodbusiness.endpoint.processor;

import com.google.gson.JsonObject;

/**
 * Standard result for tasks that have changed the model
 */
public class ModelTaskResult {
	private final long added, removed, size;

	public ModelTaskResult(long added, long removed, long size) {
		this.added = added;
		this.removed = removed;
		this.size = size;
	}

	public long getAdded() {
		return added;
	}

	public long getRemoved() {
		return removed;
	}
	
	public long getSize() {
		return size;
	}
	
	public String toJson() {
		JsonObject o = new JsonObject();
		
		o.addProperty("added", added);
		o.addProperty("removed", removed);
		o.addProperty("size", size);
		
		return o.toString();
	}
}
