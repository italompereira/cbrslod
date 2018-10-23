package br.com.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import br.com.model.DBPediaEndpoint;

public class Main {

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		
		String graph = "http://dbpedia.org";
		File file = new File("museums.txt");
		String musems = null;
		try {
			List<String> lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
			musems = "<http://dbpedia.org/resource/"+StringUtils.join(lines, ">, <http://dbpedia.org/resource/") + ">";
		} catch (IOException e) {
			e.printStackTrace();
		}
		int numberOfLevels = 2;
		double thresholdCoverage = 0.01;
		double thresholdDiscriminability = 1;
		
		
		System.out.println("Building endpoint...");
		DBPediaEndpoint dbpedia = new DBPediaEndpoint(graph, "dbo:Museum", musems, numberOfLevels, thresholdCoverage, thresholdDiscriminability);
		
		System.out.println("Calculating predicates stats...");
		dbpedia.calcPredicateStats();
		
		System.out.println("Gets the instance neighborhood...");
		dbpedia.getInstanceNeighborhood();
		
		System.out.println("Calculating predicate term frequency...");
		dbpedia.calcPredicateFrequencyOnInstance();
		
		System.out.println("Calculating Zone Index...");
		dbpedia.calcZoneIndex();
		
		System.out.println("Comparing...");
		dbpedia.compare1B();
	}
		
}
