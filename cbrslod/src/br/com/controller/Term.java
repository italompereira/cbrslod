package br.com.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Term implements Serializable{
	private static final long serialVersionUID = 1L;
	private String term;
	private List<PredicateTerm> predicateList = new ArrayList<PredicateTerm>();
	
	public Term(String term) {
		super();
		this.term = term;
	}

	public String getTerm() {
		return term;
	}

	public List<PredicateTerm> getPredicateList() {
		return predicateList;
	}
	
	@Override
	public boolean equals(Object o){
	    if(o instanceof Term){
	    	Term toCompare = (Term) o;
	        return this.term.equals(toCompare.term);
	    }
	    return false;
	}
	
	@Override
	public int hashCode() {
	    return term.hashCode();
	}

	@Override
	public String toString() {
		return term;
	}
	
}
