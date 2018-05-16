package br.com.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Predicate implements Serializable{

	private static final long serialVersionUID = 1L;
	private String uRI;
	private double discriminability;
	private double predicateFrequency;
	private double idf;
	private Map<Instance,Double> tFInstances = new HashMap<Instance,Double>();
	private boolean calcStats = false;
	private boolean calcTermFreq = false;
		
	public Predicate(String predicate) {
		super();
		this.uRI = predicate;
	}

	public String getURI() {
		return uRI;
	}
	
	public void setURI(String predicate) {
		this.uRI = predicate;
	}
	
	public double getDiscriminability() {
		return discriminability;
	}
	
	public void setDiscriminability(double discriminability) {
		this.discriminability = discriminability;
	}
	
	public double getPredicateFrequency() {
		return predicateFrequency;
	}

	public void setPredicateFrequency(double coverage) {
		this.predicateFrequency = coverage;
	}
	
	public double getIdf() {
		return idf;
	}

	public void setIdf(double idf) {
		this.idf = idf;
	}
	
	public Map<Instance, Double> getTFInstances() {
		return tFInstances;
	}
	
	public double getTFIDF(Instance instance){
		return tFInstances.get(instance)*idf;
	}
	
	public boolean isCalcStats() {
		return calcStats;
	}

	public void setCalcStats(boolean calcStats) {
		this.calcStats = calcStats;
	}

	public boolean isCalcTermFreq() {
		return calcTermFreq;
	}

	public void setCalcTermFreq(boolean calcTermFreq) {
		this.calcTermFreq = calcTermFreq;
	}

	@Override
	public boolean equals(Object o){
	    if(o instanceof Predicate){
	    	Predicate toCompare = (Predicate) o;
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
		return "Dis: " + String.format("%.6f", this.discriminability) 
			+ "  |  PrF: " + String.format("%.6f", this.predicateFrequency) 
			+ "  |  IDF: " +  String.format("%.6f", this.idf)
			+ "  |  " + this.uRI;
		
		
	}

}
