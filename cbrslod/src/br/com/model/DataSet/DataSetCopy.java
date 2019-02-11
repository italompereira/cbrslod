package br.com.model.DataSet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import br.com.model.DBPediaEndpoint;
import br.com.model.EndpointInterface;

public class DataSetCopy {
	
	public static final String TO = "C:/datasets/";
	private String pathTo;
	private Model model;
	private Dataset dataset;
	private String endpoint;
	private String domain;
	private String filter;
	
	public DataSetCopy(String endpoint, String domain, String filter){
		
		this.domain = domain;
		this.endpoint = endpoint;
		this.filter = filter;
		
		URL url = null;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.pathTo = TO + url.getHost();
		
		File folder = new File(this.pathTo);
		folder.mkdir();
		
		File folderTDB = new File(this.pathTo + "/tdb");
		folderTDB.mkdir();
		
		this.dataset = TDBFactory.createDataset(this.pathTo + "/tdb");	
		this.model = this.dataset.getDefaultModel();		
	}
	
	public void commitTransaction(){
		this.dataset.commit();
		this.dataset.begin(ReadWrite.WRITE);
	}
	
	public void copyDomain(){
		// Open the dataset for writing
		this.dataset.begin(ReadWrite.WRITE);
		try {
			// Loads result of query on tdb
			Model getModel;
			int level = 1;
			while (level <= 2) {
				int page = 0;
				do {
					if (page != 0 && page % 50 == 0) {
						System.out.println("Commit");
						this.commitTransaction();
					}
					
					System.out.print("Level " + level + " Page " + page);
					
					getModel = getDomainTriples(page++, level);
					System.out.print(" - Getted ");
					
					this.model.add(getModel);
					System.out.println(" - Added - " + getModel.size());
					
				} while (getModel.size() > 0);
				level++;
			}
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		finally {
			// commit transaction
			System.out.println("Commit");
			this.dataset.commit();
			
			this.dataset.end();
		}
	}
	
    public Model getDomainTriples(int page, int level) throws Exception {
    	
    	EndpointInterface endpointInterface = new EndpointInterface();
    	Model model = null;
    	
		// Query
		String szQuery = null;
		int limit = 10000;
		int offset = page * limit;
		
		if (level == 1) {
			szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
					+ " CONSTRUCT { ?s ?p ?o } "
					+ " WHERE { "
					+ "    ?s a "+ this.domain +" ; "
					+ "    ?p ?o "
					+ (this.filter == null ? null : " FILTER(?s IN ("+ this.filter +")) ")
					+ " } LIMIT " + limit + " OFFSET " + offset ;
		} else if(level == 2){
			szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
					+ " CONSTRUCT { ?s ?p ?o } "
					+ " WHERE { "
					+ " { "
					+ "    ?subject a "+ this.domain +" . "
					+ "    ?subject ?predicate ?s . "
					+ "    ?s ?p ?o "
					+ " } UNION { "
					+ "    ?subject a "+ this.domain +" . "
					+ "    ?subject ?predicate ?o . "
					+ "    ?s ?p ?o "
					+ " } "
					+ (this.filter == null ? null : " FILTER(?s IN ("+ this.filter +")) ")
					+ " } LIMIT " + limit + " OFFSET " + offset ;
		}

		// Construct graph based on Endpoint
		try {
			model = endpointInterface.constructEndpoint(szQuery, this.endpoint);
			
		} catch (Exception ex) {
			System.err.println(ex);
			//model = getDomainTriples(page, level);
			throw ex;
		}
		return model;
	} 
    
    public static void main(String args[]) {
    	
		File file = new File("museums.txt");
		String musems = null;
		try {
			List<String> lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
			musems = "<http://dbpedia.org/resource/"+StringUtils.join(lines, ">, <http://dbpedia.org/resource/") + ">";
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	DataSetCopy ds = new DataSetCopy(DBPediaEndpoint.ENDPOINT, "dbo:Museum", musems);
    	ds.copyDomain();
    }
}
