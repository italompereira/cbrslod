package br.com.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class InstanceNeighborhood implements Serializable {
	private static final long serialVersionUID = 1L;
	List<Node> neighborhood = new ArrayList<>();
	
	public InstanceNeighborhood(List<Node> neighborhood){
		this.neighborhood = neighborhood;
	}
		
	public List<Node> getNeighborhood() {
		return neighborhood;
	}
	
	public Node getLastElement() {
		return neighborhood.get(neighborhood.size()-1);
	}
	
	public Node getLastPredicate() {
		return neighborhood.get(neighborhood.size()-2);
	}
	
	@Override
	public String toString() {
		return this.getNeighborhood().toString()+"\n";
	}
	
	
}
