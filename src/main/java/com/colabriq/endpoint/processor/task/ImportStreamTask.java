package com.colabriq.endpoint.processor.task;

import java.io.InputStream;

import org.apache.log4j.Logger;

import com.colabriq.endpoint.processor.ModelTaskResult;
import com.colabriq.shared.executor.PrioritizedTask;

import io.vertx.core.Future;

/**
 * Import data in a stream directly
 */
public class ImportStreamTask implements Runnable, PrioritizedTask {
	private static Logger log = Logger.getLogger(ImportStreamTask.class);
	
	private final Importer importer;
	private final InputStream stream;
	private final String lang;
	private final boolean isPreload;
	private final Future<ModelTaskResult> future;
	
	public ImportStreamTask(Importer importer, InputStream stream, String lang, boolean isPreload, Future<ModelTaskResult> future) {
		this.importer = importer;
		this.stream = stream;
		this.lang = lang;
		this.isPreload = isPreload;
		this.future = future;
	}

	@Override
	public void run() {
		var sizeBefore = importer.getModel().size();
		
		try {
			importer.read(stream, lang, isPreload);
			var sizeAfter = importer.getModel().size();
			
			log.info("Loaded data (now " + sizeAfter + ")");
			future.complete(new ModelTaskResult(sizeAfter - sizeBefore, 0, sizeAfter));
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
