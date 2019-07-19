package com.goodforgoodbusiness.endpoint.processor.task.dht;

import java.util.stream.Stream;

import org.apache.jena.query.Dataset;

import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerCollector;
import com.goodforgoodbusiness.endpoint.processor.ModelTaskResult;
import com.goodforgoodbusiness.endpoint.processor.PrioritizedTask;
import com.goodforgoodbusiness.endpoint.processor.task.UpdateTask;
import com.goodforgoodbusiness.model.Link;
import com.goodforgoodbusiness.model.StorableContainer;
import com.goodforgoodbusiness.model.SubmittableContainer.SubmitMode;

import io.vertx.core.Future;

/**
 * Wraps {@link UpdateTask} so that triples are collected and containerized
 */
public class DHTUpdateTask implements Runnable, PrioritizedTask {
	private final ContainerCollector collector;
	private final Dataset dataset;
	
	private final Stream<Link> custodyChainHeader;
	private final SubmitMode mode;
	private final String stmt;
	private final Future<DHTPublishResult> future;

	public DHTUpdateTask(ContainerCollector collector, Dataset dataset, 
		SubmitMode mode, Stream<Link> custodyChainHeader, String stmt, Future<DHTPublishResult> future) {
		
		this.collector = collector;
		this.dataset = dataset;
		this.mode = mode;
		this.custodyChainHeader = custodyChainHeader;
		this.stmt = stmt;
		this.future = future;
	}
	
	@Override
	public void run() {
		// begin collection
		// collector uses ThreadLocals which will still be valid when task.run is called
		// but may not be valid inside the future
		// so get a ref to the container we can use inside the future
		var container = collector.begin();
		custodyChainHeader.forEach(container::linked);
		
		try {
			var task = new UpdateTask(
				dataset,
				stmt,
				Future.<ModelTaskResult>future().setHandler(updateResult -> {
					// change the TaskResult in to a DHTTaskResult
					if (updateResult.succeeded()) {
						// now submit
						container.submit(
							Future.<StorableContainer>future().setHandler(submitResult -> {
								if (submitResult.succeeded()) {
									future.complete(new DHTPublishResult(
										submitResult.result(),
										dataset.getDefaultModel().size()
									));
								}
								else {
									future.fail(submitResult.cause());
								}
							}),
							mode
						);
					}
					else {
						future.fail(updateResult.cause());
					}
				})
			);
		
			task.run();
		}
		finally {
			collector.clear();
		}
	}
	
	@Override
	public Priority getPriority() {
		return Priority.REAL;
	}
}
