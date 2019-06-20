package com.goodforgoodbusiness.endpoint.route;

import static org.apache.commons.io.FilenameUtils.getExtension;

import javax.servlet.MultipartConfigElement;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.goodforgoodbusiness.endpoint.graph.container.ContainerCollector;
import com.goodforgoodbusiness.endpoint.processor.ImportProcessor;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class UploadRoute implements Route {
	private final ContainerCollector collector;
	private final ImportProcessor runner;
	
	@Inject
	public UploadRoute(ContainerCollector collector, ImportProcessor runner) {
		this.collector = collector;
		this.runner = runner;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		res.type(ContentType.json.getContentTypeString());
	    req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
	    
	    var uploadPart = req.raw().getPart("upload");
	    if (uploadPart == null) {
	    	throw new BadRequestException("File upload must be specified as 'upload' element");
	    }
	    
	    String filename = uploadPart.getSubmittedFileName();
	    var lang = MIMEMappings.FILE_TYPES.get(getExtension(filename.toLowerCase()));
	    if (lang == null) {
	    	throw new BadRequestException("File extension not known: " + filename);
	    }
	    
	    var container = collector.begin();
	    
	    try {
			try (var is = uploadPart.getInputStream()) {
				runner.importStream(is, lang);
			}
	    }
	    finally {
	    	collector.clear();
	    }
	    
		if (container.isEmpty()) {			
			return "{}";
		}
		else {
			// return created container
			JsonObject o = new JsonObject();
			
			o.addProperty("added", container.getAdded().size());
			o.addProperty("removed", container.getRemoved().size());
			
			return o.toString();
		}
	}
}
