package com.goodforgoodbusiness.endpoint;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.shared.GuiceUtil.o;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.name.Names.named;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.crypto.Identity;
import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTDatasetProvider;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTGraphMaker;
import com.goodforgoodbusiness.endpoint.graph.persistent.PersistentGraph;
import com.goodforgoodbusiness.endpoint.graph.persistent.container.ContainerBuilder;
import com.goodforgoodbusiness.endpoint.graph.persistent.container.ContainerCollector;
import com.goodforgoodbusiness.endpoint.graph.persistent.container.ContainerPersistentGraph;
import com.goodforgoodbusiness.endpoint.processor.TaskResult;
import com.goodforgoodbusiness.endpoint.processor.task.ImportPathTask;
import com.goodforgoodbusiness.endpoint.webapp.SparqlGetHandler;
import com.goodforgoodbusiness.endpoint.webapp.SparqlPostHandler;
import com.goodforgoodbusiness.endpoint.webapp.SparqlTaskLauncher;
import com.goodforgoodbusiness.endpoint.webapp.UploadHandler;
import com.goodforgoodbusiness.endpoint.webapp.dht.DHTTaskLauncher;
import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.webapp.VerticleRunner;
import com.goodforgoodbusiness.webapp.VerticleServer;
import com.goodforgoodbusiness.webapp.VerticleServer.HandlerProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import io.vertx.core.Future;
import io.vertx.core.Verticle;

/**
 * Main module for launching the RDF endpoint.
 */
public class EndpointModule extends AbstractModule {
	private static final Logger log = Logger.getLogger(EndpointModule.class);
	
	private final Configuration config;
	private final Injector injector;
	
	private VerticleServer server = null;
	
	public EndpointModule(Configuration config) {
		this.config = config;
		this.injector = createInjector(this);
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
			
			// dht graph
			bind(Graph.class).to(ContainerPersistentGraph.class);
			bind(DHTGraphMaker.class);
			bind(Dataset.class).toProvider(DHTDatasetProvider.class);
			
			// dht helpers
			bind(ContainerCollector.class);
			
			bind(Identity.class);
			bind(ContainerBuilder.class);
			
			// dht comms
//			bind(DHTBlacklist.class);
//			bind(DHTEngineClient.class);
//			bind(DHTContainerSubmitter.class);
//			bind(DHTAccessGovernor.class);
		}
		else {
			log.info("Standalone data store");
			bind(Graph.class).to(PersistentGraph.class);
			bind(Dataset.class).toProvider(BaseDatasetProvider.class);
		}
		
		// bind a thread pool for actual query execution
		// since these depend on synchronous ops against
		// the database or upstream, but can themselves be
		// executed progressively at least
		bind(ExecutorService.class).toProvider(ThreadPoolProvider.class).in(SINGLETON);
		
		// add internal reasoner plugins (static for now)
//		var plugins = newSetBinder(binder(), InternalPlugin.class);
		
//		plugins.addBinding().to(HermitReasonerPlugin.class);
//		plugins.addBinding().to(ObjectCustodyChainReasonerPlugin.class);
		
//		bind(InternalPluginManager.class);
		
		// rebind specific port for webapp
		bind(Integer.class).annotatedWith(named("port")).to(Key.get(Integer.class, named("data.port")));
		
		// bind Vert.x components 
		bind(VerticleRunner.class);
		bind(VerticleServer.class);
		bind(Verticle.class).to(VerticleServer.class);
		
		// bind appropriate handlers
		if (isDHTEnabled()) {
			bind(SparqlTaskLauncher.class).to(DHTTaskLauncher.class);
		}
		else {
			bind(SparqlTaskLauncher.class);
		}
		
		bind(SparqlGetHandler.class);
		bind(SparqlPostHandler.class);
		bind(UploadHandler.class);
		
		// configure route mappings
		// fine to use getProvider here because it won't be called until the injector is created
		bind(HandlerProvider.class).toInstance((router) -> {
			router.get ("/sparql").handler(o(injector, SparqlGetHandler.class));
			router.post("/sparql").handler(o(injector, SparqlPostHandler.class));
			router.post("/upload").handler(o(injector, UploadHandler.class));
		});
	}
	
	public void start() {
		// check for preload (if the DHT is disabled)
		if (config.getBoolean("data.preload.enabled", false)) {
			log.info("Preloading data...");
			
			injector.getInstance(ExecutorService.class).submit(
				new ImportPathTask(
					injector.getInstance(Dataset.class),
					new File(config.getString("data.preload.path")),
					Future.<TaskResult>future().setHandler(result -> {
						if (result.succeeded()) {
							log.info("Import loaded " + result.result().getSize() + " triples");
							this.boot();
						}
						else {
							log.error("Import failed", result.cause());
						}
					})
				)
			);
		}
		else {
			this.boot();
		}
	}
	
	public void boot() {
		log.info("Booting services...");
		
		// perform initial reasoner runs
//		this.injector.getInstance(InternalPluginManager.class).init();
		
		// start data endpoint
		this.server = injector.getInstance(Key.get(VerticleServer.class));
		this.server.start();
	}
	
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EndpointModule.class,  args.length > 0 ? args[0] : "env.properties");
		LogConfigurer.init(EndpointModule.class, config.getString("log.properties", "log4j.properties"));
		new EndpointModule(config).start();
	}
}
