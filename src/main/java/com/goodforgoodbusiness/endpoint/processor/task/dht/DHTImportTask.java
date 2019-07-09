package com.goodforgoodbusiness.endpoint.processor.task.dht;

import java.io.InputStream;

import org.apache.jena.query.Dataset;

import com.goodforgoodbusiness.endpoint.graph.persistent.container.ContainerCollector;
import com.goodforgoodbusiness.endpoint.processor.TaskResult;
import com.goodforgoodbusiness.endpoint.processor.task.ImportStreamTask;
import com.goodforgoodbusiness.model.StorableContainer;

import io.vertx.core.Future;

/**
 * Import data in a stream directly
 */
public class DHTImportTask implements Runnable {
	private final ContainerCollector collector;
	private final Dataset dataset;
	private final InputStream stream;
	private final String lang;
	private final Future<DHTSubmitResult> future;
	
	public DHTImportTask(ContainerCollector collector, Dataset dataset, InputStream stream, String lang, Future<DHTSubmitResult> future) {
		this.collector = collector;
		this.dataset = dataset;
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
		
		try {
			var task = new ImportStreamTask(
				dataset,
				stream,
				lang,
				Future.<TaskResult>future().setHandler(importResult -> {
					// change the TaskResult in to a DHTTaskResult
					if (importResult.succeeded()) {
						// now submit
						collector.submit(
							container,
							Future.<StorableContainer>future().setHandler(storeResult -> {
								if (storeResult.succeeded()) {
									future.complete(new DHTSubmitResult(
										storeResult.result(),
										dataset.getDefaultModel().size()
									));
								}
								else {
									future.fail(storeResult.cause());
								}
							})
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
}
