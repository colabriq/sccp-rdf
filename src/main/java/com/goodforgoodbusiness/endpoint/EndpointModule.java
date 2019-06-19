package com.goodforgoodbusiness.endpoint;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.webapp.Resource.get;
import static com.goodforgoodbusiness.webapp.Resource.post;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.DHTAccessGovernor;
import com.goodforgoodbusiness.endpoint.dht.DHTContainerCollector;
import com.goodforgoodbusiness.endpoint.dht.DHTContainerStore;
import com.goodforgoodbusiness.endpoint.dht.DHTContainerSubmitter;
import com.goodforgoodbusiness.endpoint.dht.DHTDatasetProvider;
import com.goodforgoodbusiness.endpoint.dht.DHTEngineClient;
import com.goodforgoodbusiness.endpoint.dht.DHTGraphMaker;
import com.goodforgoodbusiness.endpoint.dht.DHTGraphProvider;
import com.goodforgoodbusiness.endpoint.graph.BaseDatasetProvider;
import com.goodforgoodbusiness.endpoint.graph.BaseDatasetProvider.Fetched;
import com.goodforgoodbusiness.endpoint.graph.BaseDatasetProvider.Inferred;
import com.goodforgoodbusiness.endpoint.graph.BaseDatasetProvider.Preloaded;
import com.goodforgoodbusiness.endpoint.graph.BaseGraph;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalReasonerManager;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalReasonerPlugin;
import com.goodforgoodbusiness.endpoint.plugin.internal.builtin.HermitReasonerPlugin;
import com.goodforgoodbusiness.endpoint.plugin.internal.builtin.ObjectCustodyChainReasonerPlugin;
import com.goodforgoodbusiness.endpoint.processor.ImportProcessor;
import com.goodforgoodbusiness.endpoint.processor.SparqlProcessor;
import com.goodforgoodbusiness.endpoint.route.SparqlRoute;
import com.goodforgoodbusiness.endpoint.route.UploadRoute;
import com.goodforgoodbusiness.endpoint.route.dht.DHTSparqlRoute;
import com.goodforgoodbusiness.endpoint.route.dht.DHTUploadRoute;
import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.webapp.Resource;
import com.goodforgoodbusiness.webapp.Webapp;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;

import spark.Route;

public class EndpointModule extends AbstractModule {
	private static final Logger log = Logger.getLogger(EndpointModule.class);
	
	
	private final Configuration config;
	private Webapp webapp;
	
	public EndpointModule(Configuration config) {
		this.config = config;
	}
	
	public boolean isDHTEnabled() {
		return config.getBoolean("dht.enabled", true);
	}
	
	@Override
	protected void configure() {
		binder().requireExplicitBindings();

		Properties props = getProperties(config);
		Names.bindProperties(binder(), props);
		
		if (isDHTEnabled()) {
			log.info("DHT-backed data store");
			
			// dht communication components
			bind(DHTEngineClient.class);
			bind(DHTContainerSubmitter.class);
			bind(DHTAccessGovernor.class);
			bind(DHTContainerStore.class);
			bind(DHTContainerCollector.class);
			bind(DHTGraphMaker.class);
		}
		else {
			log.info("Standalone data store");
		}
		
		bind(Graph.class).annotatedWith(Preloaded.class).to(BaseGraph.class);
		bind(Graph.class).annotatedWith(Inferred.class).to(BaseGraph.class);
		
		if (isDHTEnabled()) {
			bind(Graph.class).annotatedWith(Fetched.class).toProvider(DHTGraphProvider.class);
			bind(Dataset.class).toProvider(DHTDatasetProvider.class);
		}
		else {
			bind(Graph.class).annotatedWith(Fetched.class).to(BaseGraph.class);
			bind(Dataset.class).toProvider(BaseDatasetProvider.class);
		}
		
		bind(SparqlProcessor.class);
		bind(ImportProcessor.class);
		
		// add internal reasoner plugins (static for now)
		
		var plugins = newSetBinder(binder(), InternalReasonerPlugin.class);
		
		plugins.addBinding().to(HermitReasonerPlugin.class);
		plugins.addBinding().to(ObjectCustodyChainReasonerPlugin.class);
		
		bind(InternalReasonerManager.class);
		
		// add webapp routes
		
		var routes = newMapBinder(binder(), Resource.class, Route.class);
		
		if (isDHTEnabled()) {
			routes.addBinding(post("/sparql")).to(DHTSparqlRoute.class);
			routes.addBinding(get("/sparql")).to(DHTSparqlRoute.class);
			routes.addBinding(post("/upload")).to(DHTUploadRoute.class);
		}
		else {
			routes.addBinding(post("/sparql")).to(SparqlRoute.class);
			routes.addBinding(get("/sparql")).to(SparqlRoute.class);
			routes.addBinding(post("/upload")).to(UploadRoute.class);
		}
		
		// rebind specific port for webapp
		bind(Integer.class).annotatedWith(named("port")).to(Key.get(Integer.class, named("data.port")));
		bind(Webapp.class);
	}
	
	public void start() throws FileNotFoundException {
		var module = new EndpointModule(config);
		var injector = createInjector(module);
		
		// check for preload (if the DHT is disabled)
		if (!module.isDHTEnabled() && config.getBoolean("data.preload.enabled", false)) {
			log.info("Preloading data...");
			injector.getInstance(Key.get(ImportProcessor.class)).importPath(new File(config.getString("data.preload.path")));
		}
		
		// perform initial reasoner runs
		injector.getInstance(InternalReasonerManager.class).init();
		
		// start data endpoint
		this.webapp = injector.getInstance(Key.get(Webapp.class));
		this.webapp.start();
	}
	
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EndpointModule.class,  args.length > 0 ? args[0] : "env.properties");
		LogConfigurer.init(EndpointModule.class, config.getString("log.properties", "log4j.properties"));
		new EndpointModule(config).start();
	}
}
