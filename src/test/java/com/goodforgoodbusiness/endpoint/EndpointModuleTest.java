package com.goodforgoodbusiness.endpoint;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.google.inject.Guice.createInjector;

import com.goodforgoodbusiness.webapp.Webapp;

public class EndpointModuleTest {
	public static void main(String[] args) throws Exception {
		var configFile = args.length > 0 ? args[0] : "withdht.properties";
		var injector = createInjector(new EndpointModule(loadConfig(EndpointModuleTest.class, configFile)));
		injector.getInstance(Webapp.class).start();
	}
}
