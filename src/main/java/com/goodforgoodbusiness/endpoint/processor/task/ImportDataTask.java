package com.goodforgoodbusiness.endpoint.processor.task;

import static com.goodforgoodbusiness.endpoint.processor.task.ImportCommon.read;
import static com.goodforgoodbusiness.shared.TimingRecorder.timer;
import static com.goodforgoodbusiness.shared.TimingRecorder.TimingCategory.RDF_IMPORTING;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.processor.TaskException;
import com.goodforgoodbusiness.endpoint.processor.TaskResult;

import io.vertx.core.Future;

/**
 * Import data in a buffer directly
 */
public class ImportDataTask implements Runnable {
	private static Logger log = Logger.getLogger(ImportDataTask.class);
	
	private final Dataset dataset;
	private final CharSequence data;
	private final String lang;
	private final Future<TaskResult> future;
	
	public ImportDataTask(Dataset dataset, CharSequence data, String lang, Future<TaskResult> future) {
		this.dataset = dataset;
		this.data = data;
		this.lang = lang;
		this.future = future;
	}
	
	@Override
	public void run() {
		var model = dataset.getDefaultModel();
		var sizeBefore = model.size();
		
		try (var timer = timer(RDF_IMPORTING)) {
			try (InputStream stream = new ByteArrayInputStream(data.toString().getBytes("UTF-8"))) {
				read(model, stream, lang);
				log.info("Loaded data (now " + model.size() + ")");
				future.complete(new TaskResult(model.size() - sizeBefore, 0, model.size()));
			}
			catch (IOException e) {
				future.fail(new TaskException("Error reading string", e));
			}
		}
	}
}
