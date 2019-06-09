package com.goodforgoodbusiness.endpoint;

import static com.github.jsonldjava.shaded.com.google.common.collect.Iterators.concat;
import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.webapp.Resource.get;
import static com.goodforgoodbusiness.webapp.Resource.post;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.name.Names.named;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.dht.DHTAccessGovernor;
import com.goodforgoodbusiness.endpoint.dht.DHTContainerCollector;
import com.goodforgoodbusiness.endpoint.dht.DHTContainerSubmitter;
import com.goodforgoodbusiness.endpoint.dht.DHTContextStore;
import com.goodforgoodbusiness.endpoint.dht.DHTEngineClient;
import com.goodforgoodbusiness.endpoint.graph.DHTGraph;
import com.goodforgoodbusiness.endpoint.graph.BaseGraph;
import com.goodforgoodbusiness.endpoint.processor.ImportProcessor;
import com.goodforgoodbusiness.endpoint.processor.ReadOnlySparqlProcessor;
import com.goodforgoodbusiness.endpoint.processor.SparqlProcessor;
import com.goodforgoodbusiness.endpoint.route.SparqlRoute;
import com.goodforgoodbusiness.endpoint.route.UploadRoute;
import com.goodforgoodbusiness.endpoint.route.dht.DHTSparqlRoute;
import com.goodforgoodbusiness.endpoint.route.dht.DHTUploadRoute;
import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.webapp.Resource;
import com.goodforgoodbusiness.webapp.Webapp;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import spark.Route;

public class EndpointModule extends AbstractModule {
	private static final Logger log = Logger.getLogger(EndpointModule.class);
	
	/**
	 * Refers to the graph representing preloaded data
	 */
	@BindingAnnotation @Target({ PARAMETER, METHOD, FIELD }) @Retention(RUNTIME)
	public static @interface Preloaded {
		// annotation, no content
	}
	
	/**
	 * Refers to the graph representing data retrieved from DHT
	 */
	@BindingAnnotation @Target({ PARAMETER, METHOD, FIELD }) @Retention(RUNTIME)
	public static @interface Fetched {
		// annotation, no content
	}
	
	/**
	 * Refers to the graph of inference
	 */
	@BindingAnnotation @Target({ PARAMETER, METHOD, FIELD }) @Retention(RUNTIME)
	public static @interface Inferred {
		// annotation, no content
	}
	
	/**
	 * Schema-related exposed components
	 */
	@BindingAnnotation @Target({ PARAMETER, METHOD, FIELD }) @Retention(RUNTIME)
	public static @interface SchemaRelated {
		// annotation, no content
	}
	
	/**
	 * Data-related exposed components
	 */
	@BindingAnnotation @Target({ PARAMETER, METHOD, FIELD }) @Retention(RUNTIME)
	public static @interface DataRelated {
		// annotation, no content
	}
	
	/**
	 * Create a union over some graphs 
	 */
	private static Graph newUnionView(Graph head, Graph... others) {
		var union = new MultiUnion(concat(singleton(head).iterator(), asList(others).iterator()));
		union.setBaseGraph(head);
		return union;
	}
	
	private final Configuration config;
	private Webapp schemaWebapp, dataWebapp;
	
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
			bind(DHTContextStore.class);
			bind(DHTContainerCollector.class);
		}
		else {
			log.info("Standalone data store");
		}
		
		// schema graph
		install(new PrivateModule() {
			@Override
			protected void configure() {
				bind(Graph.class).annotatedWith(Preloaded.class).to(BaseGraph.class);
				bind(Graph.class).annotatedWith(Inferred.class).to(BaseGraph.class);
				
				if (isDHTEnabled()) {
					bind(Graph.class).annotatedWith(Fetched.class).to(DHTGraph.class);
				}
				else {
					bind(Graph.class).annotatedWith(Fetched.class).to(BaseGraph.class);
				}
				
				bind(SparqlProcessor.class).to(ReadOnlySparqlProcessor.class);
				
				// rebind sparql processor with annotation + expose
				bind(SparqlProcessor.class).annotatedWith(SchemaRelated.class).to(Key.get(SparqlProcessor.class));
				expose(SparqlProcessor.class).annotatedWith(SchemaRelated.class);
				
				bind(ImportProcessor.class);
				
				// rebind import processor with annotation + expose.
				bind(ImportProcessor.class).annotatedWith(SchemaRelated.class).to(Key.get(ImportProcessor.class));
				expose(ImportProcessor.class).annotatedWith(SchemaRelated.class);
				
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
				
				bind(Integer.class).annotatedWith(named("port")).to(Key.get(Integer.class, named("schema.port")));
				bind(Webapp.class).annotatedWith(SchemaRelated.class).to(Webapp.class);
				expose(Webapp.class).annotatedWith(SchemaRelated.class);
			}
			
			@Provides @Singleton
			public Graph getUnion(@Preloaded Graph preloadedGraph, @Fetched Graph fetchedGraph, @Inferred Graph inferredGraph) {
				// fetched graph goes first because it's writeable
				return newUnionView(fetchedGraph, preloadedGraph, inferredGraph);
			}
		});
		
		// data graph
		install(new PrivateModule() {
			@Override
			protected void configure() {
				bind(Graph.class).annotatedWith(Preloaded.class).to(BaseGraph.class);
				bind(Graph.class).annotatedWith(Inferred.class).to(BaseGraph.class);
				
				if (isDHTEnabled()) {
					bind(Graph.class).annotatedWith(Fetched.class).to(DHTGraph.class);
				}
				else {
					bind(Graph.class).annotatedWith(Fetched.class).to(BaseGraph.class);
				}
				
				bind(SparqlProcessor.class);
				
				// rebind sparql processor with annotation + expose
				bind(SparqlProcessor.class).annotatedWith(DataRelated.class).to(Key.get(SparqlProcessor.class));
				expose(SparqlProcessor.class).annotatedWith(DataRelated.class);
				
				bind(ImportProcessor.class);
				
				// rebind import processor with annotation + expose.
				bind(ImportProcessor.class).annotatedWith(DataRelated.class).to(Key.get(ImportProcessor.class));
				expose(ImportProcessor.class).annotatedWith(DataRelated.class);
				
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
				bind(Webapp.class).annotatedWith(DataRelated.class).to(Webapp.class);
				expose(Webapp.class).annotatedWith(DataRelated.class);
			}
			
			@Provides @Singleton
			public Graph getUnion(@Preloaded Graph preloadedGraph, @Fetched Graph fetchedGraph, @Inferred Graph inferredGraph) {
				// fetched graph goes first because it's writeable
				return newUnionView(fetchedGraph, preloadedGraph, inferredGraph);
			}
		});
	}
	
	public void start() throws FileNotFoundException {
		var module = new EndpointModule(config);
		var injector = createInjector(module);
		
		// check for schema preload
		if (config.getBoolean("schema.preload.enabled", false)) {
			log.info("Preloading schema...");
			injector.getInstance(Key.get(ImportProcessor.class, SchemaRelated.class))
				.importPath(new File(config.getString("schema.preload.path")));
		}
		
		// start schema endpoint
		this.schemaWebapp = injector.getInstance(Key.get(Webapp.class, SchemaRelated.class));
		this.schemaWebapp.start();
		
		// check for data preload (if the DHT is disabled)
		if (!module.isDHTEnabled() && config.getBoolean("data.preload.enabled", false)) {
			log.info("Preloading data...");
			injector.getInstance(Key.get(ImportProcessor.class, DataRelated.class))
				.importPath(new File(config.getString("data.preload.path")));
		}
		
		// start data endpoint
		this.dataWebapp = injector.getInstance(Key.get(Webapp.class, DataRelated.class));
		this.dataWebapp.start();
	}
	
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EndpointModule.class,  args.length > 0 ? args[0] : "env.properties");
		LogConfigurer.init(EndpointModule.class, config.getString("log.properties", "log4j.properties"));
		new EndpointModule(config).start();
	}
}
