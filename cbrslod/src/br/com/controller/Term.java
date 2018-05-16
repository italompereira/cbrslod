package br.com.controller;

import java.io.Serializable;
import java.util.HashSet;

public class Term implements Serializable{
	private static final long serialVersionUID = 1L;
	private String term;
	private HashSet<TermZone> invertedIndex = new HashSet<TermZone>();
	
	public Term(String term) {
		super();
		this.term = term;
	}

	public String getTerm() {
		return term;
	}

	public HashSet<TermZone> getInvertedIndex() {
		return invertedIndex;
	}

	@Override
	public String toString() {
		return term;
	}
	
	
}
