package br.com.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import br.com.model.DBPediaEndpoint;

public class Main {

//	static double thresholdCoverage = 0.9;
//	static double thresholdDiscriminability = 0.9;
//	static String method = "compareCosine";
	private static String domain = "dbo:Film";//*/"dbo:Museum";
	
	private static String fileNameI;
	
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		
		fileNameI = "./src/br/com/model/DataSet/"+domain.replace(":", "").substring(3)+"/instances.txt";
		
		double thresholdCoverage;
		double thresholdDiscriminability;
		
		String methods[] = {"TextCosine"/*,"TextSoftTF-IDF","NeighborhoodCosine*,"NeighborhoodSoftTF-IDF"*/};
		
		Double coverage = 9.0;
		while (coverage > 4) {
			
			Double discriminability = 9.0;
			while (discriminability > 4) {
				
				//new File(domain.replace(":", "") + "Instance.ser").delete(); 
				new File(domain.replace(":", "") + "Predicate.ser").delete(); 
				
				for (String method : methods) {
					
					//new File("SimMatrix.ser").delete(); 
					//new File("Rank.ser").delete(); 
					
					System.out.println(coverage/10 + " " + discriminability/10 + " " + method);
					thresholdCoverage = coverage/10;
					thresholdDiscriminability = discriminability/10;
					
					//Starts here
					String to = thresholdCoverage + " " + thresholdDiscriminability + " " + method;
					
					String graph = "http://dbpedia.org";
					File file = new File(fileNameI);
					//String instances = null;
					List<String> lines = new ArrayList<>();
					try {
						lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
						lines.remove(0);
						//instances = "<http://dbpedia.org/resource/"+StringUtils.join(lines, ">, <http://dbpedia.org/resource/") + ">";
					} catch (IOException e) {
						e.printStackTrace();
					}
					int numberOfLevels = 2;
					//instances = null;
					
					System.out.println("Building endpoint...");
					DBPediaEndpoint dbpedia = new DBPediaEndpoint(graph, domain, lines, numberOfLevels, thresholdCoverage, thresholdDiscriminability);
					
					System.out.println("Calculating predicates stats...");
					dbpedia.calcPredicateStats();
					
					System.out.println("Gets the instance neighborhood...");
					dbpedia.getInstanceNeighborhood();
					
					System.out.println("Calculating predicate term frequency...");
					dbpedia.calcPredicateFrequencyOnInstance();
					
					//System.out.println("Calculating Zone Index...");
					//dbpedia.calcZoneIndex();
					
					System.out.println("Comparing...");
					dbpedia.compareBasedOnInstances(method);
					
					EvaluateRanks.evaluateFilm(to, lines);
				}
				//Ends here
				discriminability = discriminability - 1;
			}
			coverage = coverage - 1;
		}
		System.out.println(EvaluateRanks.toMax + " " + Arrays.toString(EvaluateRanks.p1TotalMax));
	}
}
