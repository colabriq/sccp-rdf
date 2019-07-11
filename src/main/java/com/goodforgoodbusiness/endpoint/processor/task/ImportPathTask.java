package com.goodforgoodbusiness.endpoint.processor.task;

import static com.goodforgoodbusiness.endpoint.MIMEMappings.FILE_TYPES;
import static com.goodforgoodbusiness.shared.FileLoader.scan;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
	
	private final Importer importer;
	private final File path;
	private final boolean isPreload;
	private final Future<TaskResult> future;
	
	public ImportPathTask(Importer importer, File path, boolean isPreload, Future<TaskResult> future) {
		this.importer = importer;
		this.path = path;
		this.isPreload = isPreload;
		this.future = future;
	}
	
	@Override
	public void run() {
		var sizeBefore = importer.getModel().size();
		
		try {
			scan(path, file -> {
				var lang = FILE_TYPES.get(getExtension(file.getName().toLowerCase()));
				if (lang != null) {
					log.info("Loading file " + file);
					try (var stream = new FileInputStream(file)) {
						importer.read(stream, lang, isPreload);
						log.info("Loaded file " + file + " (now " + importer.getModel().size() + ")");
					}
					catch (IOException e) {
						throw new TaskException("Error reading file " + file, e);
					}
				}
				else {
					log.info("Skipped file " + file);
				}
			});
			
			var sizeAfter = importer.getModel().size();
			future.complete(new TaskResult(sizeAfter - sizeBefore, 0, sizeAfter));
		}
		catch (Exception e) {
			future.fail(e);
		}
	}
}
