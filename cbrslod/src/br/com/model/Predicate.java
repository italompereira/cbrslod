package br.com.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Predicate implements Serializable{

	private static final long serialVersionUID = 1L;
	private String uRI;
	private int level;
	private double discriminability;
	private double predicateFrequency;
	private double idf;
	private Map<Instance,Double> tFInstances = new HashMap<Instance,Double>();
	private boolean calcStats = false;
	private boolean calcTermFreq = false;
		
	public Predicate(String predicate, int level) {
		super();
		this.uRI = predicate;
		this.level = level;
	}

	public String getURI() {
		return uRI;
	}
	
	public void setURI(String predicate) {
		this.uRI = predicate;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
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
	        return (this.uRI.equals(toCompare.uRI) && (this.level == toCompare.level));
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
			+ "  |  PrF: " + String.format("%.3f", this.predicateFrequency) 
			+ "  |  IDF: " +  String.format("%.6f", this.idf)
			+ "  |  Level: " + this.level
			+ "  |  " + this.uRI;
		
		
	}

}
