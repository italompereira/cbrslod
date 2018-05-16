package br.com.controller;

import java.io.Serializable;

public class TermZone implements Serializable {
	private static final long serialVersionUID = 1L;
	private Instance instance;
	private Predicate predicate;
	
	public TermZone(Instance instance, Predicate predicate) {
		super();
		this.instance = instance;
		this.predicate = predicate;
	}

	public Instance getInstance() {
		return instance;
	}

	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public String toString() {
		return "TermZone [instance=" + instance + ", predicate=" + predicate + "]\n";
	}
	
	
}
