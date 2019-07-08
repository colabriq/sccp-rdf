package com.goodforgoodbusiness.endpoint;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.shared.GuiceUtil.o;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider;
import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider.Fetched;
import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider.Inferred;
import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider.Preloaded;
import com.goodforgoodbusiness.endpoint.graph.base.BaseGraph;
import com.goodforgoodbusiness.endpoint.graph.container.ContainerCollector;
import com.goodforgoodbusiness.endpoint.graph.container.ContainerStore;
import com.goodforgoodbusiness.endpoint.graph.container.ContainerizedGraph;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTAccessGovernor;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTBlacklist;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTContainerSubmitter;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTDatasetProvider;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTEngineClient;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTGraph;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTGraphMaker;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPlugin;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPluginManager;
import com.goodforgoodbusiness.endpoint.processor.TaskResult;
import com.goodforgoodbusiness.endpoint.processor.task.ImportPathTask;
import com.goodforgoodbusiness.endpoint.webapp.SparqlCommon;
import com.goodforgoodbusiness.endpoint.webapp.SparqlGetHandler;
import com.goodforgoodbusiness.endpoint.webapp.SparqlPostHandler;
import com.goodforgoodbusiness.endpoint.webapp.UploadHandler;
import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.webapp.VerticleRunner;
import com.goodforgoodbusiness.webapp.VerticleServer;
import com.goodforgoodbusiness.webapp.VerticleServer.HandlerProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import io.vertx.core.Future;
import io.vertx.core.Verticle;

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
		
		bind(Graph.class).annotatedWith(Preloaded.class).toInstance(new BaseGraph());
		bind(Graph.class).annotatedWith(Inferred.class).toInstance(new BaseGraph());
		
		if (isDHTEnabled()) {
			log.info("DHT-backed data store");
			
			bind(Graph.class).annotatedWith(Fetched.class).to(DHTGraph.class);
			bind(Dataset.class).toProvider(DHTDatasetProvider.class).in(Scopes.SINGLETON);
			
			// dht comms
			bind(DHTBlacklist.class);
			bind(DHTEngineClient.class);
			bind(DHTContainerSubmitter.class);
			bind(DHTAccessGovernor.class);
			bind(DHTGraphMaker.class);
		}
		else {
			log.info("Standalone data store");
			
			bind(Graph.class).annotatedWith(Fetched.class).to(ContainerizedGraph.class);
			bind(Dataset.class).toProvider(BaseDatasetProvider.class).in(Scopes.SINGLETON);
		}
		
		bind(ContainerStore.class);
		bind(ContainerCollector.class);
		
		// bind a thread pool for actual query execution
		// since these depend on synchronous ops against
		// the database or upstream, but can themselves be
		// executed progressively at least
		bind(ExecutorService.class).toInstance(Executors.newFixedThreadPool(10));
		
		// add internal reasoner plugins (static for now)
		var plugins = newSetBinder(binder(), InternalPlugin.class);
		
//		plugins.addBinding().to(HermitReasonerPlugin.class);
//		plugins.addBinding().to(ObjectCustodyChainReasonerPlugin.class);
		
		bind(InternalPluginManager.class);
		
		// rebind specific port for webapp
		bind(Integer.class).annotatedWith(named("port")).to(Key.get(Integer.class, named("data.port")));
		
		// bind Vert.x components 
		bind(VerticleRunner.class);
		bind(VerticleServer.class);
		bind(Verticle.class).to(VerticleServer.class);
		
		// bind appropriate handlers
		if (isDHTEnabled()) {
			throw new UnsupportedOperationException();
		}
		else {
			bind(SparqlCommon.class);
			bind(SparqlGetHandler.class);
			bind(SparqlPostHandler.class);
			bind(UploadHandler.class);
		}
		
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
		if (!isDHTEnabled() && config.getBoolean("data.preload.enabled", false)) {
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
		this.injector.getInstance(InternalPluginManager.class).init();
		
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
