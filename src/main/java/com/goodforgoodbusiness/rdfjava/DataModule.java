package com.goodforgoodbusiness.rdfjava;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.google.inject.Guice.createInjector;
import static org.apache.commons.configuration2.ConfigurationConverter.getProperties;

import java.util.Properties;

import org.apache.commons.configuration2.Configuration;
import org.apache.jena.query.Dataset;

import com.goodforgoodbusiness.rdfjava.dht.ClaimCollector;
import com.goodforgoodbusiness.rdfjava.dht.ClaimContextMap;
import com.goodforgoodbusiness.rdfjava.dht.DHTDatasetProvider;
import com.goodforgoodbusiness.rdfjava.dht.DHTRDFRunner;
import com.goodforgoodbusiness.rdfjava.rdf.RDFRunner;
import com.goodforgoodbusiness.rdfjava.service.DataService;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class DataModule extends AbstractModule {
	private final Configuration config;
	
	protected DataModule(Configuration config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		Properties props = getProperties(config);
		Names.bindProperties(binder(), props);
		
		bind(DataService.class);
		bind(RDFRunner.class).to(DHTRDFRunner.class);
		bind(Dataset.class).toProvider(DHTDatasetProvider.class);
		
		bind(ClaimCollector.class);
		bind(ClaimContextMap.class);
	}
	
	public static void main(String[] args) throws Exception {
		createInjector(new DataModule(loadConfig(DataModule.class, "rdf.properties")))
			.getInstance(DataService.class)
			.start()
		;
	}
}
