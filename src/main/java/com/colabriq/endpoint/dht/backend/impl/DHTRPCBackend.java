package com.colabriq.endpoint.dht.backend.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import com.colabriq.endpoint.dht.backend.DHTBackend;
import com.colabriq.proto.DHTProto.ContainerFetchRequest;
import com.colabriq.proto.DHTProto.ContainerFetchResponse;
import com.colabriq.proto.DHTProto.ContainerPublishRequest;
import com.colabriq.proto.DHTProto.ContainerPublishResponse;
import com.colabriq.proto.DHTProto.ContainerSearchRequest;
import com.colabriq.proto.DHTProto.ContainerSearchResponse;
import com.colabriq.proto.DHTProto.PointerPublishRequest;
import com.colabriq.proto.DHTProto.PointerPublishResponse;
import com.colabriq.proto.DHTProto.PointerSearchRequest;
import com.colabriq.proto.DHTProto.PointerSearchResponse;
import com.colabriq.rpclib.client.RPCClient;
import com.colabriq.rpclib.client.RPCClientException;
import com.colabriq.rpclib.client.response.RPCResponse;
import com.colabriq.rpclib.client.response.RPCSingleResponseHandler;
import com.colabriq.rpclib.client.response.RPCStreamResponseHandler;
import com.colabriq.shared.URIModifier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.protobuf.ByteString;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;

/**
 * Implements {@link DHTBackend} using the RPC client to go across to dhtengine
 */
@Singleton
public class DHTRPCBackend implements DHTBackend {
	private static final Logger log = Logger.getLogger(DHTRPCBackend.class);
	private final RPCClient client;
	
	@Inject
	public DHTRPCBackend(Vertx vertx, WebClient client, @Named("dht.uri") String dhtURI) throws URISyntaxException {
		this.client = new RPCClient(URIModifier.from(new URI(dhtURI)).appendPath("/rpc").build());
	}
	
	@Override
	public void publishPointer(String pattern, byte[] data, Future<Void> future) {
		var blocker = new CompletableFuture<AsyncResult<RPCResponse<PointerPublishResponse>>>();
		
		client.send(
			PointerPublishRequest.newBuilder()
				.setPattern(pattern)
				.setData(ByteString.copyFrom(data))
				.build()
			,
			new RPCSingleResponseHandler<>(
				PointerPublishResponse.class,
				Future.<RPCResponse<PointerPublishResponse>>future().setHandler(result -> blocker.complete(result))
			)
		);
		
		try {
			blocker.get();
			future.complete();
		}
		catch (ExecutionException | InterruptedException e) {
			future.fail(e);
		}
	}

	@Override
	public void searchForPointers(String pattern, Future<Stream<byte[]>> future) {
		var blocker = new CompletableFuture<AsyncResult<Stream<RPCResponse<PointerSearchResponse>>>>();
		
		client.send(
			PointerSearchRequest.newBuilder()
				.setPattern(pattern)
				.build()
			,
			new RPCStreamResponseHandler<>(
				PointerSearchResponse.class,
				Future.<Stream<RPCResponse<PointerSearchResponse>>>future().setHandler(result -> blocker.complete(result))
			)
		);
		
		try {
			var result = blocker.get();
			if (result.succeeded()) {
				future.complete(result.result().map(rr -> {
					try {
						return rr.get().getResponse().toByteArray();
					}
					catch (RPCClientException e) {
						log.error(e);
						return null;
					}
				}).filter(Objects::nonNull));
			}
			else {
				future.fail(result.cause());
			}
		}
		catch (ExecutionException | InterruptedException e) {
			future.fail(e);
		}		
	}

	@Override
	public void publishContainer(String id, byte[] data, Future<String> future) {
		var blocker = new CompletableFuture<AsyncResult<RPCResponse<ContainerPublishResponse>>>();
		
		client.send(
			ContainerPublishRequest.newBuilder()
				.setId(id)
				.setData(ByteString.copyFrom(data))
				.build()
			,
			new RPCSingleResponseHandler<>(
				ContainerPublishResponse.class,
				Future.<RPCResponse<ContainerPublishResponse>>future().setHandler(result -> blocker.complete(result))
			)
		);

		try {
			var result = blocker.get();
			if (result.succeeded()) {
				future.complete(result.result().get().getLocation());
			}
			else {
				future.fail(result.cause());
			}
		}
		catch (RPCClientException | ExecutionException | InterruptedException e) {
			future.fail(e);
		}
	}

	@Override
	public void searchForContainer(String id, Future<Stream<String>> future) {
		var blocker = new CompletableFuture<AsyncResult<Stream<RPCResponse<ContainerSearchResponse>>>>();
		
		client.send(
			ContainerSearchRequest.newBuilder()
				.setId(id)
				.build()
			,
			new RPCStreamResponseHandler<>(
				ContainerSearchResponse.class,
				Future.<Stream<RPCResponse<ContainerSearchResponse>>>future().setHandler(result -> blocker.complete(result))
			)
		);
		
		try {
			var result = blocker.get();
			if (result.succeeded()) {
				future.complete(result.result().map(rr -> {
					try {
						return rr.get().getLocation();
					}
					catch (RPCClientException e) {
						log.error(e);
						return null;
					}
				}).filter(Objects::nonNull));
			}
			else {
				future.fail(result.cause());
			}
		}
		catch (ExecutionException | InterruptedException e) {
			future.fail(e);
		}
	}

	@Override
	public void fetchContainer(String location, Future<Optional<byte[]>> future) {
		var blocker = new CompletableFuture<AsyncResult<RPCResponse<ContainerFetchResponse>>>();
		
		client.send(
			ContainerFetchRequest.newBuilder()
				.setLocation(location)
				.build()
			,
			new RPCSingleResponseHandler<>(
				ContainerFetchResponse.class,
				Future.<RPCResponse<ContainerFetchResponse>>future().setHandler(result -> blocker.complete(result))
			)
		);
		
		try {
			var result = blocker.get();
			if (result.succeeded()) {
				future.complete(Optional.of(result.result().get().getData().toByteArray()));
			}
			else {
				future.fail(result.cause());
			}
		}
		catch (RPCClientException | ExecutionException | InterruptedException e) {
			future.fail(e);
		}
	}
}

