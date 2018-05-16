package br.com.controller;

import br.com.model.DBPediaEndpoint;

public class Main {

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		System.out.println("Building endpoint...");
		DBPediaEndpoint dbpedia = new DBPediaEndpoint("dbo:Museum");
		
		System.out.println("Calculating predicates stats...");
		dbpedia.calcPredicateStats();
		
		System.out.println("Gets the instance neighborhood...");
		dbpedia.getInstanceNeighborhood();
		
		System.out.println("Calculating predicate term frequency...");
		dbpedia.calcPredicateFrequencyOnInstance();
		
		System.out.println("Calculating Zone Index...");
		dbpedia.calcZoneIndex();
		
		System.out.println("Comparing...");
		dbpedia.compareInstances();
		
		
		
	}
}
