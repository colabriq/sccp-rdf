package com.goodforgoodbusiness.endpoint;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.goodforgoodbusiness.shared.GuiceUtil.o;
import static com.goodforgoodbusiness.webapp.BaseVerticle.HandlerProvider.createBodyHandler;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraphFactory.GraphMaker;
import org.apache.log4j.Logger;
import org.rocksdb.RocksDBException;

import com.goodforgoodbusiness.endpoint.crypto.Identity;
import com.goodforgoodbusiness.endpoint.dht.DHT;
import com.goodforgoodbusiness.endpoint.dht.DHTWarpDriver;
import com.goodforgoodbusiness.endpoint.dht.DHTWeftDriver;
import com.goodforgoodbusiness.endpoint.dht.backend.DHTBackend;
import com.goodforgoodbusiness.endpoint.dht.backend.impl.DHTMemBackend;
import com.goodforgoodbusiness.endpoint.dht.keys.ShareKeyStore;
import com.goodforgoodbusiness.endpoint.dht.keys.impl.MemKeyStore;
import com.goodforgoodbusiness.endpoint.graph.base.BaseDatasetProvider;
import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerBuilder;
import com.goodforgoodbusiness.endpoint.graph.containerized.ContainerCollector;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTGraphMaker;
import com.goodforgoodbusiness.endpoint.graph.dht.DHTPersistentGraph;
import com.goodforgoodbusiness.endpoint.plugin.ContainerListenerManager;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPlugin;
import com.goodforgoodbusiness.endpoint.plugin.internal.InternalPluginManager;
import com.goodforgoodbusiness.endpoint.plugin.internal.builtin.ObjectCustodyChainReasonerPlugin;
import com.goodforgoodbusiness.endpoint.plugin.internal.builtin.reasoner.HermitReasonerPlugin;
import com.goodforgoodbusiness.endpoint.processor.ModelTaskResult;
import com.goodforgoodbusiness.endpoint.processor.task.ImportPathTask;
import com.goodforgoodbusiness.endpoint.processor.task.Importer;
import com.goodforgoodbusiness.endpoint.storage.PersistentGraph;
import com.goodforgoodbusiness.endpoint.storage.ShareManager;
import com.goodforgoodbusiness.endpoint.storage.TripleContexts;
import com.goodforgoodbusiness.endpoint.storage.rocks.context.TripleContextStore;
import com.goodforgoodbusiness.endpoint.webapp.SparqlGetHandler;
import com.goodforgoodbusiness.endpoint.webapp.SparqlPostHandler;
import com.goodforgoodbusiness.endpoint.webapp.SparqlTaskLauncher;
import com.goodforgoodbusiness.endpoint.webapp.UploadHandler;
import com.goodforgoodbusiness.endpoint.webapp.admin.StopHandler;
import com.goodforgoodbusiness.endpoint.webapp.dht.DHTTaskLauncher;
import com.goodforgoodbusiness.rocks.RocksManager;
import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.shared.executor.ExecutorProvider;
import com.goodforgoodbusiness.shared.executor.PrioritizedExecutor;
import com.goodforgoodbusiness.webapp.BaseServer;
import com.goodforgoodbusiness.webapp.BaseVerticle;
import com.goodforgoodbusiness.webapp.BaseVerticle.HandlerProvider;
import com.goodforgoodbusiness.webapp.VertxProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * Main module for launching the RDF endpoint.
 */
public class EndpointModule extends AbstractModule {
	private static final Logger log = Logger.getLogger(EndpointModule.class);
	
	protected final Configuration config;
	protected final Injector injector;
	
	private BaseServer server = null;
	
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
		
		try {
			// create and start the database 
			var rocksManager = new RocksManager(props.getProperty("storage.path"));
			rocksManager.start();
			
			bind(RocksManager.class).toInstance(rocksManager);
		}
		catch (RocksDBException e) {
			throw new RuntimeException("RocksDB failed to start", e);
		}
		
		if (isDHTEnabled()) {
			log.info("DHT-backed data store");
			
			// dht graph
			bind(Graph.class).to(DHTPersistentGraph.class);
			bind(GraphMaker.class).to(DHTGraphMaker.class);
			bind(Dataset.class).toProvider(BaseDatasetProvider.class);
			
			// dht helpers
			bind(ContainerCollector.class);
			
			bind(Identity.class);
			bind(ContainerBuilder.class);
			
			bind(ShareManager.class);
			bind(ShareKeyStore.class).to(MemKeyStore.class);
			
			bind(DHT.class);
			bind(DHTWarpDriver.class);
			bind(DHTWeftDriver.class);
			
			bind(DHTBackend.class).to(DHTMemBackend.class);
//			bind(WebClient.class).toProvider(DHTRPCWebClientProvider.class); // might want to annotate this.
		}
		else {
			log.info("Standalone data store");
			
			bind(Graph.class).to(PersistentGraph.class);
			bind(GraphMaker.class).toInstance(BaseDatasetProvider.NONE);
			bind(Dataset.class).toProvider(BaseDatasetProvider.class);
		}
		
		bind(TripleContextStore.class);
		bind(TripleContexts.class);
		
		// bind a thread pool for actual query execution
		// since these depend on synchronous ops against
		// the database or upstream, but can themselves be
		// executed progressively at least
		bind(ExecutorService.class).toProvider(ExecutorProvider.class).in(SINGLETON);
		bind(Importer.class);
		
		bind(ContainerListenerManager.class);
		
		// add internal reasoner plugins (static for now)
		var plugins = newSetBinder(binder(), InternalPlugin.class);
		
		plugins.addBinding().to(HermitReasonerPlugin.class);
		plugins.addBinding().to(ObjectCustodyChainReasonerPlugin.class);
		
		bind(InternalPluginManager.class);
		
		// rebind specific port for webapp
		bind(Integer.class).annotatedWith(named("port")).to(Key.get(Integer.class, named("data.port")));
		
		// bind Vert.x components 
		bind(Vertx.class).toProvider(VertxProvider.class);
		bind(BaseServer.class);
		bind(BaseVerticle.class);
		bind(Verticle.class).to(BaseVerticle.class);
		
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
			
			// body handler that can do file uploads
			router.post("/upload").handler(createBodyHandler());
			router.post("/upload").handler(o(injector, UploadHandler.class));
			
			router.post("/admin/stop").handler(new StopHandler(EndpointModule.this));
		});
	}
	
	public void start() throws Exception {
		// check for preload (if the DHT is disabled)
		if (config.getBoolean("data.preload.enabled", false)) {
			log.info("Preloading data...");
			
			injector.getInstance(ExecutorService.class).submit(
				new ImportPathTask(
					injector.getInstance(Importer.class),
					new File(config.getString("data.preload.path")),
					true,
					Future.<ModelTaskResult>future().setHandler(result -> {
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
		this.server = injector.getInstance(Key.get(BaseServer.class));
		this.server.start();
	}
	
	public void shutdown() {
		if (this.server != null) {
			// carefully close those pieces that need shutting down
			this.server.stop();
			this.server = null;
			
			var vx = injector.getInstance(Vertx.class);
			vx.close(voidResult -> {
				log.info("Vert.x closed");
				
				var es = injector.getInstance(PrioritizedExecutor.class);
				es.safeStop();
				
				var rm = injector.getInstance(RocksManager.class);
				rm.close();
			});
		}
	}
	
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EndpointModule.class,  args.length > 0 ? args[0] : "env.properties");
		LogConfigurer.init(EndpointModule.class, config.getString("log.properties", "log4j.properties"));
		
		var module = new EndpointModule(config);
		module.start();
		
		// add shutdown hook to catch SIGTERM
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			module.shutdown();
		}));
	}
}
