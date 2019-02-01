package com.goodforgoodbusiness.endpoint;

import static com.goodforgoodbusiness.shared.ConfigLoader.loadConfig;
import static com.google.inject.Guice.createInjector;

import com.goodforgoodbusiness.shared.LogConfigurer;
import com.goodforgoodbusiness.webapp.Webapp;

public class EndpointModuleTest {
	public static void main(String[] args) throws Exception {
		var config = loadConfig(EndpointModuleTest.class, args.length > 0 ? args[0] : "withdht.properties");
		LogConfigurer.init(EndpointModuleTest.class, config.getString("log.properties", "log4j.properties"));
		
		var injector = createInjector(new EndpointModule(config));
		injector.getInstance(Webapp.class).start();
	}
}
