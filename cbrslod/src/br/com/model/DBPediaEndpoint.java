package br.com.model;

public class DBPediaEndpoint extends Endpoint {
	/**
	 * ENDPOINT address
	 */
	public static final String ENDPOINT = "http://DBpedia.org/sparql";
	
	public DBPediaEndpoint(String domain) {
		super(domain, ENDPOINT);
		// TODO Auto-generated constructor stub
	}

}
