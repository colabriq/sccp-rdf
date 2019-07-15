package com.goodforgoodbusiness.endpoint.processor.task;

import static com.goodforgoodbusiness.endpoint.MIMEMappings.FILE_TYPES;
import static com.goodforgoodbusiness.shared.FileLoader.scan;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.processor.ModelTaskResult;
import com.goodforgoodbusiness.endpoint.processor.PrioritizedTask;

import io.vertx.core.Future;

/**
 * Import a local file or directory from storage
 * @throws FileNotFoundException 
 */
public class ImportPathTask implements Runnable, PrioritizedTask {
	private static Logger log = Logger.getLogger(ImportPathTask.class);
	
	private final Importer importer;
	private final File path;
	private final boolean isPreload;
	private final Future<ModelTaskResult> future;
	
	public ImportPathTask(Importer importer, File path, boolean isPreload, Future<ModelTaskResult> future) {
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
						throw new RuntimeException("Error reading file " + file, e);
					}
				}
				else {
					log.info("Skipped file " + file);
				}
			});
			
			var sizeAfter = importer.getModel().size();
			future.complete(new ModelTaskResult(sizeAfter - sizeBefore, 0, sizeAfter));
		}
		catch (Exception e) {
			future.fail(e);
		}
	}
	
	@Override
	public Priority getPriority() {
		return Priority.NORMAL;
	}
}
