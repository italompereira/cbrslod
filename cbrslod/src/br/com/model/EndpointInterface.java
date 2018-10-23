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
	/**
     * Query an Endpoint using the given SPARQl query
     * @param szQuery
     * @param szEndpoint
     * @throws Exception
     */
    public ResultSet queryEndpoint(String szQuery, String szEndpoint) throws Exception
    {
        query = QueryFactory.create(szQuery);
        qexec = QueryExecutionFactory.sparqlService(szEndpoint, query);
        ((QueryEngineHTTP)qexec).addParam("timeout", "1000000");
        
        ResultSet result = qexec.execSelect();
        return result;
    } 
    
    public void close(){
    	qexec.close();
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
