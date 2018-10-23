package br.com.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PredicateTerm implements Serializable {
	private static final long serialVersionUID = 1L;
	private String predicate;
	private List<Instance> instanceList = new ArrayList<Instance>();
	
	public PredicateTerm(String predicate) {
		super();
		this.predicate = predicate;
	}

	public List<Instance> getInstanceList() {
		return instanceList;
	}

	public String getPredicate() {
		return predicate;
	}
	
	@Override
	public boolean equals(Object o){
	    if(o instanceof PredicateTerm){
	    	PredicateTerm toCompare = (PredicateTerm) o;
	        return this.predicate.equals(toCompare.predicate);
	    }
	    return false;
	}
	
	@Override
	public int hashCode() {
	    return predicate.hashCode();
	}

	@Override
	public String toString() {
		return predicate;
	}
	
	
}
