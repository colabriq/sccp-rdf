package com.goodforgoodbusiness.endpoint.processor.task;

import static com.goodforgoodbusiness.endpoint.processor.task.ImportCommon.read;

import java.io.InputStream;

import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.processor.TaskResult;

import io.vertx.core.Future;

/**
 * Import data in a stream directly
 */
public class ImportStreamTask implements Runnable {
	private static Logger log = Logger.getLogger(ImportStreamTask.class);
	
	private final Dataset dataset;
	private final InputStream stream;
	private final String lang;
	private final Future<TaskResult> future;
	
	public ImportStreamTask(Dataset dataset, InputStream stream, String lang, Future<TaskResult> future) {
		this.dataset = dataset;
		this.stream = stream;
		this.lang = lang;
		this.future = future;
	}

	@Override
	public void run() {
		var model = dataset.getDefaultModel();
		var sizeBefore = model.size();
		
		try {
			read(model, stream, lang);
			log.info("Loaded data (now " + model.size() + ")");
			future.complete(new TaskResult(model.size() - sizeBefore, 0, model.size()));
		}
		catch (Exception e) {
			future.fail(e);
		}
	}
}
