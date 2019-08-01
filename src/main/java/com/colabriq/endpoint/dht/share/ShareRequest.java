package com.colabriq.endpoint.dht.share;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents the detail of a share request
 */
public class ShareRequest {
	@Expose
	@SerializedName("pattern")
	private SharePattern pattern;
	
	@Expose
	@SerializedName("start")
	private ZonedDateTime start;
		
	@Expose
	@SerializedName("end")
	private ZonedDateTime end;
	
	
	public ShareRequest() {
	}
	
	public SharePattern getPattern() {
		return pattern;
	}
	
	public void setPattern(SharePattern pattern) {
		this.pattern = pattern;
	}
	
	public Optional<ZonedDateTime> getStart() {
		return Optional.ofNullable(start);
	}

	public void setStart(ZonedDateTime start) {
		this.start = start;
	}
	
	public Optional<ZonedDateTime> getEnd() {
		return Optional.ofNullable(end);
	}

	public void setEnd(ZonedDateTime end) {
		this.end = end;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(this.pattern);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof ShareRequest) {
			ShareRequest oo = (ShareRequest)o;
			return Objects.equal(this.pattern, oo.pattern);
		}
		
		return false;
	}
}


		