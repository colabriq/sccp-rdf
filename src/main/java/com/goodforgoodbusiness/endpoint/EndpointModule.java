package com.goodforgoodbusiness.endpoint;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.webapp.Resource.get;
import static com.goodforgoodbusiness.webapp.Resource.post;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraphMaker;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.ClaimCollector;
import com.goodforgoodbusiness.endpoint.dht.ClaimContext;
import com.goodforgoodbusiness.endpoint.dht.DHTAccessGovernor;
import com.goodforgoodbusiness.endpoint.dht.DHTEngineClient;
import com.goodforgoodbusiness.endpoint.dht.DHTGraphProvider;
import com.goodforgoodbusiness.endpoint.dht.DHTSubmitter;
import com.goodforgoodbusiness.endpoint.rdf.RDFPreloader;
import com.goodforgoodbusiness.endpoint.rdf.RDFRunner;
import com.goodforgoodbusiness.endpoint.rdf.StandaloneGraphProvider;
import com.goodforgoodbusiness.endpoint.route.SparqlRoute;
import com.goodforgoodbusiness.endpoint.route.UploadRoute;
import com.goodforgoodbusiness.endpoint.route.dht.DHTSparqlRoute;
import com.goodforgoodbusiness.endpoint.route.dht.DHTUploadRoute;
import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.webapp.Resource;
import com.goodforgoodbusiness.webapp.Webapp;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import spark.Route;

public class EndpointModule extends AbstractModule {
	private static final Logger log = Logger.getLogger(EndpointModule.class);
	
	private final Configuration config;
	
	public EndpointModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		binder().requireExplicitBindings();
		
		Properties props = getProperties(config);
		Names.bindProperties(binder(), props);
		
		bind(RDFRunner.class);
		bind(RDFPreloader.class);
		bind(Webapp.class);
		
		var routes = newMapBinder(binder(), Resource.class, Route.class);
		
		if (config.getBoolean("dht.enabled")) {
			log.info("Configuring DHT-backed endpoint");
			bind(Graph.class).toProvider(DHTGraphProvider.class);
			
			bind(DHTEngineClient.class);
			bind(DHTSubmitter.class);
			bind(DHTAccessGovernor.class);
			
			bind(ClaimContext.class);
			bind(ClaimCollector.class);
			
			routes.addBinding(post("/sparql")).to(DHTSparqlRoute.class);
			routes.addBinding(get("/sparql")).to(DHTSparqlRoute.class);
			routes.addBinding(post("/upload")).to(DHTUploadRoute.class);
		}
		else {
			log.info("Configuring standalone endpoint");
			bind(Graph.class).toProvider(StandaloneGraphProvider.class);
			
			routes.addBinding(post("/sparql")).to(SparqlRoute.class);
			routes.addBinding(get("/sparql")).to(SparqlRoute.class);
			routes.addBinding(post("/upload")).to(UploadRoute.class);
		}
	}
	
	@Provides @Singleton
	protected Dataset getDataset(Graph graph) {
		return DatasetFactory.create(new DatasetGraphMaker(graph));
	}
	
	@Provides @Singleton
	protected Model getModel(Dataset dataset) {
		return dataset.getDefaultModel();
	}
	
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EndpointModule.class,  args.length > 0 ? args[0] : "env.properties");
		LogConfigurer.init(EndpointModule.class, config.getString("log.properties", "log4j.properties"));
		
		var injector = createInjector(new EndpointModule(config));
		
		log.info("Checking for preload...");
		injector.getInstance(RDFPreloader.class).preload();
		injector.getInstance(Webapp.class).start();
	}
}
