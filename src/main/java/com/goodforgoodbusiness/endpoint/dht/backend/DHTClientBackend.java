//package com.goodforgoodbusiness.endpoint.dht.backend;
//
//import java.util.Optional;
//import java.util.stream.Stream;
//
//import io.vertx.core.Future;
//
///**
// * Responsible for talking to the remote DHT manager.
// * Only encrypted information should be exchanged with it.
// */
//public class DHTClientBackend implements DHTBackend {
//	@Override
//	public void publishPointer(String hashPattern, byte[] data, Future<Void> future) {
//	}
//
//	@Override
//	public void searchForPointers(String hashPattern, Future<Stream<byte[]>> future) {
//	}
//
//	@Override
//	public void publishContainer(String id, byte[] data, Future<String> future) {
//	}
//
//	@Override
//	public void searchForContainer(String id, Future<Stream<String>> future) {
//	}
//
//	@Override
//	public void fetchContainer(String location, Future<Optional<byte[]>> future) {
//	}
//}
