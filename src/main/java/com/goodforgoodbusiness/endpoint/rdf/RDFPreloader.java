package com.goodforgoodbusiness.endpoint.rdf;

import static com.goodforgoodbusiness.shared.FileLoader.scan;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class RDFPreloader {
	private final RDFRunner runner;
	private Optional<File> preloadDir = Optional.empty();
	
	@Inject
	public RDFPreloader(
		RDFRunner runner, 
		@Named("preload.enabled") boolean preloadEnabled, 
		@Named("preload.path") String preloadPath) throws FileNotFoundException {
		
		this.runner = runner;
		
		if (preloadEnabled) {
			var preloadFile = new File(preloadPath);
			if (preloadFile.exists()) {
				this.preloadDir = Optional.of(preloadFile);
			}
			else {
				throw new FileNotFoundException("Preload path " + preloadPath + " specified but not found");
			}
		}
		else {
			this.preloadDir = Optional.empty();
		}
	}
	
	public void preload() {
		preloadDir.ifPresent(path -> scan(path, runner.fileConsumer()));
	}
}
