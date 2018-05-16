package br.com.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

public class Instance implements Serializable {
	private static final long serialVersionUID = 1L;
	private String uRI;
	private List<InstanceNeighborhood> instanceNeighborhoodList;
	
	public Instance(Resource instance){
		this.uRI = instance.toString();
		this.instanceNeighborhoodList = new ArrayList<>();
	}
	
	public Instance(String instance){
		this.uRI = instance;
		this.instanceNeighborhoodList = new ArrayList<>();
	}
		
	public List<InstanceNeighborhood> getInstanceNeighborhoodList() {
		return instanceNeighborhoodList;
	}
	
	public String getURI() {
		return uRI;
	}

	public void setURI(String uri) {
		this.uRI = uri;
	}

	public int countPredicateOnNeighborhoodList(String predicate){
		int qt = 0;
		for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodList) {
			for (Node node : instanceNeighborhood.getNeighborhood()) {
				if (node.getNode().equals(predicate)) {
					qt++;
				}
			}
		}
		return qt;
	}
	
	@Override
	public boolean equals(Object o){
	    if(o instanceof Instance){
	    	Instance toCompare = (Instance) o;
	        return this.uRI.equals(toCompare.uRI);
	    }
	    return false;
	}
	
	@Override
	public int hashCode() {
	    return uRI.hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.uRI;
	}
}
