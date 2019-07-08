package com.goodforgoodbusiness.endpoint.processor.task;

import static com.goodforgoodbusiness.endpoint.MIMEMappings.FILE_TYPES;
import static com.goodforgoodbusiness.endpoint.processor.task.ImportCommon.read;
import static com.goodforgoodbusiness.shared.FileLoader.scan;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.processor.TaskException;
import com.goodforgoodbusiness.endpoint.processor.TaskResult;

import io.vertx.core.Future;

/**
 * Import a local file or directory from storage
 * @throws FileNotFoundException 
 */
public class ImportPathTask implements Runnable {
	private static Logger log = Logger.getLogger(ImportPathTask.class);
	
	private final Dataset dataset;
	private final File path;
	private final Future<TaskResult> future;
	
	public ImportPathTask(Dataset dataset, File path, Future<TaskResult> future) {
		this.dataset = dataset;
		this.path = path;
		this.future = future;
	}
	
	@Override
	public void run() {
		var model = dataset.getDefaultModel();
		var sizeBefore = model.size();
		
		try {
			scan(path, file -> {
				var lang = FILE_TYPES.get(getExtension(file.getName().toLowerCase()));
				if (lang != null) {
					log.info("Loading file " + file);
					try (var stream = new FileInputStream(file)) {
						read(model, stream, lang);
						log.info("Loaded file " + file + " (now " + model.size() + ")");
					}
					catch (IOException e) {
						throw new TaskException("Error reading file " + file, e);
					}
				}
				else {
					log.info("Skipped file " + file);
				}
			});
			
			future.complete(new TaskResult(model.size() - sizeBefore, 0, model.size()));
		}
		catch (Exception e) {
			future.fail(e);
		}
	}
}
