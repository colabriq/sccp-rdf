package com.goodforgoodbusiness.rdfjava.rdf;

import org.apache.jena.mem.GraphMem;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraphMaker;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class DatasetProvider implements Provider<Dataset> {
	@Inject
	public DatasetProvider() {
	}
	
	@Override
	public Dataset get() {
		var dataGraphMaker = new DatasetGraphMaker(new GraphMem());
		return DatasetFactory.create(dataGraphMaker);
	}
}
