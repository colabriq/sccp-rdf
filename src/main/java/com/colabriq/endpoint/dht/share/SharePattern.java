package com.colabriq.endpoint.dht.share;

import java.util.Optional;

import org.apache.jena.graph.Triple;

import com.colabriq.shared.TripleUtil;
import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Details of triple patterns made available to others over the network.
 * We work on values rather than including type information.
 * @author ijmad
 */
public class SharePattern {
	private static final byte [] ANY_BYTES = new byte [] { 0, 0, 0 };
	
	@Expose
	@SerializedName("sub")
	private String subject;
	
	@Expose
	@SerializedName("pre")
	private String predicate;
	
	@Expose
	@SerializedName("obj")
	private String object;
	
	public SharePattern() {
	}
	
	/**
	 * Set s/p/o from {@link Triple}
	 */
	public SharePattern(Triple triple) {
		this.subject = TripleUtil.valueOf(triple.getSubject()).orElse(null);
		this.predicate = TripleUtil.valueOf(triple.getPredicate()).orElse(null);
		this.object = TripleUtil.valueOf(triple.getObject()).orElse(null);
	}
	
	public Optional<String> getSubject() {
		return Optional.ofNullable(subject);
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Optional<String> getPredicate() {
		return Optional.ofNullable(predicate);
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public Optional<String> getObject() {
		return Optional.ofNullable(object);
	}

	public void setObject(String object) {
		this.object = object;
	}
	
	/**
	 * Encode triple, values only (since sharekeys are shared with us as values only).
	 */
	public byte [] toByteArray() {
		var sBytes = getSubject().map(String::getBytes).orElse(ANY_BYTES);
		var pBytes = getPredicate().map(String::getBytes).orElse(ANY_BYTES);
		var oBytes = getObject().map(String::getBytes).orElse(ANY_BYTES);
		
		var tBytes = new byte[sBytes.length + pBytes.length + oBytes.length];
		
		var pos = 0;
		
		System.arraycopy(sBytes, 0, tBytes, pos, sBytes.length);
		pos += sBytes.length;
		
		System.arraycopy(pBytes, 0, tBytes, pos, pBytes.length);
		pos += pBytes.length;
		
		System.arraycopy(oBytes, 0, tBytes, pos, oBytes.length);
		
		return tBytes;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.subject, this.predicate, this.object);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof SharePattern) {
			SharePattern oo = (SharePattern)o;
			return 
				Objects.equal(this.subject, oo.subject) && 
				Objects.equal(this.predicate, oo.predicate) && 
				Objects.equal(this.object, oo.object)
			;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "SharePattern(s=" + subject + " p=" + predicate + " o=" + object + ")";
	}
}
