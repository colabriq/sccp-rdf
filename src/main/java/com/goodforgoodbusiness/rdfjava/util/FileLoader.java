package com.goodforgoodbusiness.rdfjava.util;

import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.goodforgoodbusiness.rdfjava.RDFException;
import com.goodforgoodbusiness.shared.MIMEMappings;

public class FileLoader {
	private static void scanInto(List<File> output, final File path) {
		if (!path.getName().startsWith(".")) {
			if (path.isDirectory()) {
				for (final File entry : path.listFiles()) {
					scanInto(output, entry);
				}
			}
			else {
				output.add(path);
			}
		}
	}
	
	public static void scan(File folder, Consumer<File> consumer) throws RDFException {
		var files = new LinkedList<File>();
		scanInto(files, folder);
		
		for (File file : files) {
			var lang = MIMEMappings.FILE_TYPES.get(getExtension(file.getName().toLowerCase()));
			if (lang != null) {
				consumer.accept(file);
			}
		}
	}
}
