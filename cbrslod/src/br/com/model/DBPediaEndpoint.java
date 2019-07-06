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
	
	public DBPediaEndpoint(String graph, String domain, String domainSparql, List<String> instanceFilter, int numberOfLevels, double infThresholdCoverage, double supThresholdCoverage, double infThresholdDiscriminability, double supThresholdDiscriminability) {
		super(ENDPOINT, graph, domain, domainSparql, instanceFilter, numberOfLevels, infThresholdCoverage, supThresholdCoverage, infThresholdDiscriminability, supThresholdDiscriminability);
	}

}
