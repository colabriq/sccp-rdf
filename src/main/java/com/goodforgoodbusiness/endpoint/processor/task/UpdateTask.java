package com.goodforgoodbusiness.endpoint.processor.task;

import static com.goodforgoodbusiness.shared.TimingRecorder.timer;
import static com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory.RDF_UPDATING;

import org.apache.jena.query.Dataset;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;

import com.goodforgoodbusiness.endpoint.processor.TaskResult;

import io.vertx.core.Future;

/**
 * Processes a SPARQL update statement
 */
public class UpdateTask implements Runnable {	
	private final Dataset dataset;
	private final String stmt;
	private final Future<TaskResult> future;

	public UpdateTask(Dataset dataset, String stmt, Future<TaskResult> future) {
		this.dataset = dataset;
		this.stmt = stmt;
		this.future = future;
	}
	
	@Override
	public void run() {
		long sizeBefore = dataset.getDefaultModel().size();
		
		try (var timer = timer(RDF_UPDATING)) {
			var update = UpdateFactory.create(stmt);
			
			var processor = UpdateExecutionFactory.create(update, dataset);
			processor.execute();
			
			long sizeAfter = dataset.getDefaultModel().size();
			
			future.complete(new TaskResult(
				// XXX this calculation is a temporary
				sizeAfter > sizeBefore ? (sizeAfter - sizeBefore) : 0,
				sizeAfter < sizeBefore ? (sizeBefore - sizeAfter) : 0,
				sizeAfter
			));
		}
		catch (Exception e) {
			future.fail(e);
		}
	}
}
