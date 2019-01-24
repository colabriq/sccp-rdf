package com.goodforgoodbusiness.rdfjava.service.route;

import static org.apache.commons.io.FilenameUtils.getExtension;

import javax.servlet.MultipartConfigElement;

import com.goodforgoodbusiness.rdfjava.RDFRunner;
import com.goodforgoodbusiness.shared.web.MIMEMappings;
import com.goodforgoodbusiness.shared.web.error.BadRequestException;

import spark.Request;
import spark.Response;
import spark.Route;

public class UploadRoute implements Route {
	private final RDFRunner runner;
	
	public UploadRoute(RDFRunner runner) {
		this.runner = runner;
	}
	
	@Override
	public Object handle(Request req, Response res) throws Exception {
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
		
		return "OK";
	}
}
