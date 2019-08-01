package com.colabriq.endpoint.dht.share;

import com.colabriq.kpabe.KPABEKeyManager;
import com.colabriq.kpabe.key.KPABEKeyPair;
import com.colabriq.kpabe.key.KPABEPublicKey;
import com.colabriq.kpabe.key.KPABESecretKey;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Provides the {@link KPABEKeyPair} we'll use to encrypt our own containers
 */
@Singleton
public class ShareKeyPairProvider implements Provider<KPABEKeyPair> {
	private KPABEKeyPair keyPair;

	@Inject
	public ShareKeyPairProvider(@Named("kpabe.publicKey") String publicKey, @Named("kpabe.secretKey") String secretKey) {
		this.keyPair = KPABEKeyManager.ofKeys(new KPABEPublicKey(publicKey), new KPABESecretKey(secretKey));
	}
	
	@Override @Singleton
	public KPABEKeyPair get() {
		return keyPair;
	}
}
