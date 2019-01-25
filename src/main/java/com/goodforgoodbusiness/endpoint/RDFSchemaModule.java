package com.goodforgoodbusiness.endpoint;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.webapp.Resource.get;
import static com.goodforgoodbusiness.webapp.Resource.post;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.io.File;
import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraphMaker;

import com.goodforgoodbusiness.endpoint.rdf.RDFRunner;
import com.goodforgoodbusiness.endpoint.route.SparqlRoute;
import com.goodforgoodbusiness.shared.FileLoader;
import com.goodforgoodbusiness.webapp.Resource;
import com.goodforgoodbusiness.webapp.Webapp;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import spark.Route;

public class RDFSchemaModule extends AbstractModule {
	private final Configuration config;
	
	public RDFSchemaModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		binder().requireExplicitBindings();
		
		Properties props = getProperties(config);
		Names.bindProperties(binder(), props);
		
		bind(RDFRunner.class);
		bind(Webapp.class);
		
		var routes = newMapBinder(binder(), Resource.class, Route.class);
		
		routes.addBinding(get("/sparql")).to(SparqlRoute.class);
		routes.addBinding(post("/sparql")).to(SparqlRoute.class);
	}
	
	@Provides @Singleton
	protected Dataset getDataset() {
		return DatasetFactory.create(
			new DatasetGraphMaker(new GraphMem())
		);
	}
	
	@Provides @Singleton
	protected Model getModel(Dataset dataset) {
		return dataset.getDefaultModel();
	}
	
	public static void main(String[] args) throws Exception {
		var config = loadConfig(RDFDataModule.class, "schema.properties");
		var injector = createInjector(new RDFSchemaModule(config));
		var preload = config.getString("preload.path");
		
		// preload of any specified turtle files
		if (preload != null) {
			File preloadDir = new File(preload);
			if (preloadDir.exists()) {
				FileLoader.scan(preloadDir, injector.getInstance(RDFRunner.class).fileConsumer());
			}
			else {
				throw new Exception(preloadDir + " specified but not found");
			}
		}
		
		// now start webapp
		injector.getInstance(Webapp.class).start();
	}
}