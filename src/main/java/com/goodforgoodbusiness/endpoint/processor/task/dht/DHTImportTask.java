package com.goodforgoodbusiness.endpoint.processor.task.dht;

import java.io.InputStream;

import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerCollector;
import com.goodforgoodbusiness.endpoint.processor.TaskResult;
import com.goodforgoodbusiness.endpoint.processor.task.ImportStreamTask;
import com.goodforgoodbusiness.endpoint.processor.task.Importer;
import com.goodforgoodbusiness.model.StorableContainer;

import io.vertx.core.Future;

/**
 * Import data in a stream directly
 */
public class DHTImportTask implements Runnable {
	private final ContainerCollector collector;
	private final Importer importer;
	private final InputStream stream;
	private final String lang;
	private final Future<DHTPublishResult> future;
	
	public DHTImportTask(ContainerCollector collector, Importer importer, InputStream stream, String lang, Future<DHTPublishResult> future) {
		this.collector = collector;
		this.importer = importer;
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
				importer,
				stream,
				lang,
				false,
				Future.<TaskResult>future().setHandler(importResult -> {
					// change the TaskResult in to a DHTTaskResult
					if (importResult.succeeded()) {
						// now submit
						container.submit(
							Future.<StorableContainer>future().setHandler(storeResult -> {
								if (storeResult.succeeded()) {
									future.complete(new DHTPublishResult(
										storeResult.result(),
										importer.getModel().size()
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
