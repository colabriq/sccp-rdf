package com.goodforgoodbusiness.endpoint.webapp;

import static org.apache.commons.io.FilenameUtils.getExtension;

import org.apache.log4j.Logger;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class UploadHandler implements Handler<RoutingContext> {
	private static final Logger log = Logger.getLogger(UploadHandler.class);
	
	private final Verticle parent;
	private final SparqlTaskLauncher sparql;
	
	@Inject
	public UploadHandler(Verticle parent, SparqlTaskLauncher sparql) {
		this.parent = parent;
		this.sparql = sparql;
	}
	
	@Override
	public void handle(RoutingContext ctx) {
		ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getContentTypeString());
		
		// we'll ingest any and all uploads regardless of name
		var uploads = ctx.fileUploads();
	    if (uploads.size() != 1) {
	    	ctx.fail(new BadRequestException("A single file upload must be specified as 'upload' element"));
	    	return;
	    }

	    var file = uploads.iterator().next();
	    var filename = file.fileName();
	    
	    log.info("Processing upload " + filename + " of length " + file.size() + " at " + file.uploadedFileName());
	    
	    // XXX process content-type here??
	    var lang = MIMEMappings.FILE_TYPES.get(getExtension(filename.toLowerCase()));
	    if (lang == null) {
	    	ctx.fail(new BadRequestException("File extension not known: " + filename));
	    	return;
	    }
	    
    	parent.getVertx().fileSystem().open(
    		file.uploadedFileName(),
    		new OpenOptions()
    			.setCreate(false)
    			.setDeleteOnClose(true)
    			.setRead(true)
    			.setWrite(false)
    			.setAppend(false)
    		,
    		asyncFileResult -> {
    			if (asyncFileResult.succeeded()) {
	    			sparql.importFile(ctx, lang, asyncFileResult.result());
    			}
    			else {
    				ctx.fail(asyncFileResult.cause());
    			}
    		}
    	);
	}
}
