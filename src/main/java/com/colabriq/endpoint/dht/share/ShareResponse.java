package com.colabriq.endpoint.dht.share;

import com.colabriq.endpoint.crypto.key.EncodeableShareKey;
import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the detail of a share request
 */
public class ShareResponse extends ShareRequest {
	public static ShareResponse createFrom(ShareRequest request, EncodeableShareKey key) {
		var response = new ShareResponse();
		
		response.setPattern(request.getPattern());
		response.setStart(request.getStart().orElse(null));
		response.setEnd(request.getEnd().orElse(null));
		response.setKey(key);
		
		return response;
	}
	
	@Expose
	@SerializedName("key")
	private EncodeableShareKey key;
	
	public ShareResponse() {
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
		return Objects.hashCode(super.getPattern(), key);
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
		return "ShareResponse(pattern=" + getPattern().toString() + " key=" + key.toString() + ")";
	}
}


		