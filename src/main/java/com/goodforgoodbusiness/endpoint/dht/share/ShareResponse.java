package com.goodforgoodbusiness.endpoint.dht.share;

import org.apache.jena.graph.Triple;

import com.goodforgoodbusiness.endpoint.crypto.key.EncodeableShareKey;
import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the detail of a share request
 */
public class ShareResponse extends ShareRequest {
	public static ShareResponse createFrom(ShareRequest request, EncodeableShareKey key) {
		var response = new ShareResponse();
		
		response.setStart(request.getStart().orElse(null));
		response.setEnd(request.getEnd().orElse(null));
		response.setSubject(request.getSubject().orElse(null));
		response.setPredicate(request.getPredicate().orElse(null));
		response.setObject(request.getObject().orElse(null));
		response.setKey(key);
		
		return response;
	}
	
	@Expose
	@SerializedName("key")
	private EncodeableShareKey key;
	
	public ShareResponse() {
	}	
	
	/**
	 * Set s/p/o from {@link Triple}
	 */
	@Override
	public ShareResponse setTriple(Triple triple) {
		super.setTriple(triple);
		return this;
	}

	public EncodeableShareKey getKey() {
		return key;
	}

	public ShareResponse setKey(EncodeableShareKey key) {
		this.key = key;
		return this;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(super.getSubject(), super.getObject(), super.getPredicate(), key);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof ShareResponse) {
			ShareResponse oo = (ShareResponse)o;
			return super.equals(oo) && Objects.equal(this.key, oo.key);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "ShareResponse(s=" + getSubject() + " p=" + getPredicate() + " o=" + getObject() + " key=" + key.toString() + ")";
	}
}


		