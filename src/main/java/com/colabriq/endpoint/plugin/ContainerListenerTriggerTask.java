package com.colabriq.endpoint.plugin;

import com.colabriq.shared.executor.PrioritizedTask;

/**
 * Task that runs a particular graph listener against a container
 */
class ContainerListenerTriggerTask implements Runnable, PrioritizedTask {
	private final StorableGraphContainer container;
	private final ContainerListener listener;
	
	ContainerListenerTriggerTask(StorableGraphContainer container, ContainerListener listener) {
		this.container = container;
		this.listener = listener;
	}

	@Override
	public void run() {
		listener.newContainer(container);
	}

	@Override
	public Priority getPriority() {
		return Priority.NICED;
	}
}
