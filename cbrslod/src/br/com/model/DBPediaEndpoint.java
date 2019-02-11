package br.com.model;

import java.util.List;

public class DBPediaEndpoint extends Endpoint {
	/**
	 * ENDPOINT address
	 */
	//public static final String ENDPOINT = "http://192.168.0.120:8890/sparql";
	//public static final String ENDPOINT = "http://live.DBpedia.org/sparql";
	public static final String ENDPOINT = "http://DBpedia.org/sparql";
	//public static final String ENDPOINT = "http://200.239.138.93:8890/sparql/";
	
	public DBPediaEndpoint(String graph, String domain, List<String> instanceFilter, int numberOfLevels, double thresholdCoverage, double thresholdDiscriminability) {
		super(ENDPOINT, graph, domain, instanceFilter, numberOfLevels, thresholdCoverage, thresholdDiscriminability);
	}

}
