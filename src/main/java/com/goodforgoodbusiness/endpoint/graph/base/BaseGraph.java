package com.goodforgoodbusiness.endpoint.graph.base;

import org.apache.jena.graph.GraphEvents;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.graph.impl.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.goodforgoodbusiness.endpoint.graph.base.store.AdvanceMapTripleStore;

public class BaseGraph<STORE_TYPE extends TripleStore> extends GraphBase {
	public static BaseGraph<AdvanceMapTripleStore> newGraph() {
		return new BaseGraph<>(new AdvanceMapTripleStore());
	}
	
	private final STORE_TYPE store;
	
	protected BaseGraph(STORE_TYPE store) { 
		this.store = store;
    }
	
	protected STORE_TYPE getStore() {
		return store;
	}
	
	@Override
	public void close() {
		store.clear();
		super.close();
   }

    @Override 
    public void performAdd( Triple t ) { 
    	store.add( t ); 
    }

    @Override 
    public void performDelete( Triple t ) { 
    	store.delete( t );
    }

    @Override 
    public int graphBaseSize() { 
    	return store.size(); 
    }
    
    @Override
    public ExtendedIterator<Triple> graphBaseFind( Triple m )  { 
    	return store.find( m );
    }
    
    @Override
    public boolean graphBaseContains( Triple t ) { 
    	return t.isConcrete() ? store.contains( t ) : super.graphBaseContains( t ); 
    }
    
    @Override
    public void clear() { 
    	store.clear();
        getEventManager().notifyEvent(this, GraphEvents.removeAll ) ;   
    }
}
