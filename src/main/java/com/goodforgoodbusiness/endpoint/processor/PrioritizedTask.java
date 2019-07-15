package com.goodforgoodbusiness.endpoint.processor;

/**
 * Holds priority for analysis by thread pool.
 */
public interface PrioritizedTask {
	public enum Priority {
		REAL(10), 
		NORMAL(5),
		NICED(1);
		
		public final int ordinal;
		
		private Priority(int ordinal) {
			this.ordinal = ordinal;
		}
	}
	
	public Priority getPriority();
}
