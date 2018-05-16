package br.com.controller;

public class Teste {
	private Instance instance;
	private double score;
	
	public Teste(Instance instance, double score) {
		super();
		this.instance = instance;
		this.score = score;
	}

	public Instance getInstance() {
		return instance;
	}
	
	public void setInstance(Instance instance) {
		this.instance = instance;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "[Instance=" + instance + ", score=" + score + "]\n";
	}
	
	
	
}
