package com.goodforgoodbusiness.rdfjava;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.google.inject.Guice.createInjector;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraphMaker;

import com.goodforgoodbusiness.rdfjava.dht.ClaimCollector;
import com.goodforgoodbusiness.rdfjava.dht.ClaimContextMap;
import com.goodforgoodbusiness.rdfjava.dht.DHTClient;
import com.goodforgoodbusiness.rdfjava.dht.DHTRDFRunner;
import com.goodforgoodbusiness.rdfjava.dht.DHTTripleStore;
import com.goodforgoodbusiness.rdfjava.rdf.RDFRunner;
import com.goodforgoodbusiness.rdfjava.service.DataService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class RDFDataModule extends AbstractModule {
	private final Configuration config;
	
	public RDFDataModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		binder().requireExplicitBindings();
		
		Properties props = getProperties(config);
		Names.bindProperties(binder(), props);
		
		bind(DHTClient.class);
		
		bind(RDFRunner.class).to(DHTRDFRunner.class);
		
		bind(ClaimCollector.class);
		bind(ClaimContextMap.class);
		
		bind(DataService.class);
	}
	
	@Provides @Singleton
	protected Dataset getDataset(DHTClient client, ClaimContextMap contextMap, ClaimCollector collector) {
		return DatasetFactory.create(
			new DatasetGraphMaker(
				new GraphMem() {
					@Override
					protected TripleStore createTripleStore() {
						return new DHTTripleStore(this, client, contextMap, collector);
					}
				}
			)
		);
	}
	
	@Provides @Singleton
	protected Model getModel(Dataset dataset) {
		return dataset.getDefaultModel();
	}
	
	public static void main(String[] args) throws Exception {
		createInjector(new RDFDataModule(loadConfig(RDFDataModule.class, "data.properties")))
			.getInstance(DataService.class)
			.start()
		;
	}
}
