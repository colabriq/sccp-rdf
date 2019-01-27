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
	public RDFPreloader(RDFRunner runner) {
		this.runner = runner;
	}
	
	@Inject(optional=true)
	public void setPreloadPath(@Named("preload.path") String preloadPath) throws FileNotFoundException {
		File preloadDir = new File(preloadPath);
		
		if (preloadDir.exists()) {
			this.preloadDir = Optional.of(preloadDir);
		}
		else {
			throw new FileNotFoundException("Preload path " + preloadPath + " specified but not found");
		}
	}
	
	public void preload() {
		preloadDir.ifPresent(path -> scan(path, runner.fileConsumer()));
	}
}
