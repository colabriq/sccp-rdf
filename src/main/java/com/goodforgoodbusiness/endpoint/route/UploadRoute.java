package com.goodforgoodbusiness.endpoint.route;

import static org.apache.commons.io.FilenameUtils.getExtension;

import javax.servlet.MultipartConfigElement;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.goodforgoodbusiness.endpoint.processor.ImportProcessor;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import spark.Request;
import spark.Response;
import spark.Route;

@Singleton
public class UploadRoute implements Route {
	private final ImportProcessor runner;
	
	@Inject
	public UploadRoute(ImportProcessor runner) {
		this.runner = runner;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
		res.type(ContentType.json.getContentTypeString());
		
	    req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
	    
	    var uploadPart = req.raw().getPart("upload");
	    if (uploadPart != null) {
	    	String filename = uploadPart.getSubmittedFileName();
	    	
	    	var lang = MIMEMappings.FILE_TYPES.get(getExtension(filename.toLowerCase()));
			if (lang != null) {
				try (var is = uploadPart.getInputStream()) {
					runner.importStream(is, lang);
				}
			}
			else {
				throw new BadRequestException("File extension not known: " + filename);
			}
	    }
	    else {
	    	throw new BadRequestException("File upload must be specified as 'upload' element");
	    }
		
		return "{}";
	}
}
