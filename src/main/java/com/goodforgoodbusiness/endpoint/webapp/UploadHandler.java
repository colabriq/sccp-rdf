package com.goodforgoodbusiness.endpoint.webapp;

import static org.apache.commons.io.FilenameUtils.getExtension;

import java.util.concurrent.ExecutorService;

import org.apache.jena.query.Dataset;

import com.goodforgoodbusiness.endpoint.MIMEMappings;
import com.goodforgoodbusiness.endpoint.processor.TaskResult;
import com.goodforgoodbusiness.endpoint.processor.task.ImportStreamTask;
import com.goodforgoodbusiness.webapp.ContentType;
import com.goodforgoodbusiness.webapp.error.BadRequestException;
import com.goodforgoodbusiness.webapp.stream.InputReadStream;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

@Singleton
public class UploadHandler implements Handler<RoutingContext> {
	private final Verticle parent;
	private final Dataset dataset;
	private final ExecutorService service;
	
	@Inject
	public UploadHandler(Verticle parent, Dataset dataset, ExecutorService service) {
		this.parent = parent;
		this.dataset = dataset;
		this.service = service;
	}
	
	@Override
	public void handle(RoutingContext ctx) {
		ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getContentTypeString());
		
		// we'll ingest any and all uploads regardless of name
		var uploads = ctx.fileUploads();
	    if (uploads.size() != 1) {
	    	ctx.fail(new BadRequestException("A single file upload must be specified as 'upload' element"));
	    	ctx.next();
	    	return;
	    }

	    var file = uploads.iterator().next();
	    var filename = file.fileName();
	    
	    var lang = MIMEMappings.FILE_TYPES.get(getExtension(filename.toLowerCase()));
	    if (lang == null) {
	    	ctx.fail(new BadRequestException("File extension not known: " + filename));
	    	ctx.next();
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
	    			service.submit(
	    			    new ImportStreamTask(
	    				    dataset,
	    				    new InputReadStream(asyncFileResult.result()),
	    				    lang,
	    					Future.<TaskResult>future().setHandler(result -> {
	    						asyncFileResult.result().close();
	    						
	    						if (result.failed()) {
	    							ctx.fail(result.cause());
	    						}
	    						else {
	    							// standard JSON result
	    							ctx.response().end(result.result().toJson());
	    							ctx.next();
	    						}
	    					})
	    				)
	    			);
    			}
    		}
    	);

	}
}
