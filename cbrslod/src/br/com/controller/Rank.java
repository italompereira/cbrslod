package br.com.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Rank implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final int qtItensRank = 21;
	private String instance;
	private List<InstanceSim> unrankedInstances;
	private List<InstanceSim> rankedInstances;
	
	public class InstanceSim implements Serializable {
		private static final long serialVersionUID = -935134910409271240L;
		public String instance;
		public double similarity;
		
		public InstanceSim(String instance, double similarity){
			this.instance = instance;
			this.similarity = similarity;
		}

		@Override
		public String toString() {
			return instance+" ("+similarity+") ";
		}

		public double getSimilarity() {
			return similarity;
		}

		public void setSimilarity(double similarity) {
			this.similarity = similarity;
		}
		
		
	}
	
	public Rank(String instance){
		this.instance = instance;
		this.rankedInstances = new ArrayList<>();
		this.unrankedInstances = new ArrayList<>();
	}
	
	public String getInstance(){
		return this.instance;
	}

	public void addInstanceSim(String instance, double similarity){
		this.unrankedInstances.add(new InstanceSim(instance, similarity));
	}
	
	public List<InstanceSim> getUnrankedInstances() {
		return unrankedInstances;
	}

	public List<InstanceSim> getRankedInstances(){
		if (this.rankedInstances.size() == 0) {
			//Sort by similarity
			rankedInstances = new ArrayList<>(unrankedInstances);
			
			Collections.sort(rankedInstances, new Comparator<InstanceSim>() {
				public int compare(InstanceSim a, InstanceSim b) {
					Double da = a.similarity;
					Double db = b.similarity;
					double diff = db.doubleValue()-da.doubleValue();
					return diff>0 ? +1 : (diff<0? -1 : 0);
				}
			});
		}
		
		return this.rankedInstances;
	}
	
	public List<String> getRankedInstancesLS(){
		List<String> list = new ArrayList<>();
		rankedInstances = getRankedInstances();
		for (InstanceSim instanceSim : rankedInstances) {
			list.add(instanceSim.instance);
		}
		return list;
	}

	@Override
	public boolean equals(Object o){
	    if(o instanceof Rank){
	    	Rank toCompare = (Rank) o;
	        return this.instance.equals(toCompare.instance);
	    }
	    return false;
	}
	
	@Override
	public int hashCode() {
	    return instance.hashCode();
	}
	
	@Override
	public String toString() {
		return instance;
	}
	

	
}
