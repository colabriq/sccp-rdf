package com.colabriq.endpoint.processor.task.dht;

import java.io.InputStream;
import java.util.stream.Stream;

import com.colabriq.endpoint.graph.containerized.ContainerCollector;
import com.colabriq.endpoint.processor.ModelTaskResult;
import com.colabriq.endpoint.processor.task.ImportStreamTask;
import com.colabriq.endpoint.processor.task.Importer;
import com.colabriq.model.Link;
import com.colabriq.model.StorableContainer;
import com.colabriq.model.SubmittableContainer.SubmitMode;
import com.colabriq.shared.executor.PrioritizedTask;

import io.vertx.core.Future;

/**
 * Import data in a stream directly
 */
public class DHTImportTask implements Runnable, PrioritizedTask {
	private final ContainerCollector collector;
	private final Importer importer;
	
	private final SubmitMode mode;
	private final Stream<Link> custodyChainHeader;
	
	private final InputStream stream;
	private final String lang;
	private final Future<DHTPublishResult> future;
	
	public DHTImportTask(ContainerCollector collector, Importer importer, SubmitMode mode, Stream<Link> custodyChainHeader, 
		InputStream stream, String lang, Future<DHTPublishResult> future) {
		
		this.collector = collector;
		this.importer = importer;
		this.custodyChainHeader = custodyChainHeader;
		this.mode = mode;
		this.stream = stream;
		this.lang = lang;
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
			var task = new ImportStreamTask(
				importer,
				stream,
				lang,
				false,
				Future.<ModelTaskResult>future().setHandler(importResult -> {
					// change the TaskResult in to a DHTTaskResult
					if (importResult.succeeded()) {
						// now submit
						container.submit(
							Future.<StorableContainer>future().setHandler(submitResult -> {
								if (submitResult.succeeded()) {
									future.complete(new DHTPublishResult(
										submitResult.result(),
										importer.getModel().size()
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
						future.fail(importResult.cause());
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
