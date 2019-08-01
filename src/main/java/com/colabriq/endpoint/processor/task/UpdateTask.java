package com.colabriq.endpoint.processor.task;

import static com.colabriq.shared.TimingRecorder.timer;
import static com.colabriq.shared.TimingRecorder.TimingCategory.RDF_UPDATING;

import org.apache.jena.query.Dataset;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;

import com.colabriq.endpoint.processor.ModelTaskResult;
import com.colabriq.shared.executor.PrioritizedTask;

import io.vertx.core.Future;

/**
 * Processes a SPARQL update statement
 */
public class UpdateTask implements Runnable, PrioritizedTask {	
	private final Dataset dataset;
	private final String stmt;
	private final Future<ModelTaskResult> future;

	public UpdateTask(Dataset dataset, String stmt, Future<ModelTaskResult> future) {
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
			
			future.complete(new ModelTaskResult(
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

	@Override
	public Priority getPriority() {
		return Priority.REAL;
	}
}
