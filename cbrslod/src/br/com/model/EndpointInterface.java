package br.com.model;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class EndpointInterface {
	
	private Query query;
	private QueryExecution qexec;
	private int attempts = 0; 
	private int limitAttempts = 3; 
	
	/**
     * Query an Endpoint using the given SPARQl query
     * @param szQuery
     * @param szEndpoint
     * @throws Exception
     */
    public ResultSet queryEndpoint(String szQuery, String szEndpoint) throws Exception
    {
        query = QueryFactory.create(szQuery);
        //qexec = QueryExecutionFactory.sparqlService(szEndpoint, query);
        qexec = new QueryEngineHTTP (szEndpoint, query);
        ((QueryEngineHTTP)qexec).addParam("timeout", "1000000");

        ResultSet result = null;
        try {
        	result = qexec.execSelect();
		} catch (Exception e) {
			this.attempts++;
			
			if (this.attempts <= this.limitAttempts) {
				System.out.println("Query Fail, trying again...");
				Thread.sleep(40000);
				result = this.queryEndpoint(szQuery, szEndpoint);
			} else {
				throw e;
			}
		}
        
        //result = ResultSetFactory.copyResults(result) ;
        //close();
        return result;
    } 
    
    public Query getQuery(){
    	return query;
    }
    
    public void close(){
    	qexec.close();
    	this.attempts = 0;
    }
    
	/**
     * Query an Endpoint using the given SPARQl query
     * @param szQuery
     * @param szEndpoint
     * @throws Exception
     */
    public Model constructEndpoint(String szQuery, String szEndpoint) throws Exception
    {
		query = QueryFactory.create(szQuery);
		QueryEngineHTTP qexec = new QueryEngineHTTP(szEndpoint, query);
		qexec.setTimeout(60000L);
		Model model = null;
		try {
			model =  qexec.execConstruct();	
		} catch (Exception e) {
			throw e;
		} finally {
			qexec.close();	
		}
	 
		return model;
    } 
}
