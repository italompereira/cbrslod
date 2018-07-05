package br.com.model.DataSet;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBLoader;

public class DataSetLoader {

	DataSetInfo datasetInfo;
	
	/**
	 * Constructor
	 */
	public DataSetLoader(DataSetInfo datasetInfo) {
		this.datasetInfo = datasetInfo;
	}

	/**
	 * Load all RDF files to TDB
	 * 
	 * @param pathFrom
	 */
	public DataSetLoader loadRDFFilesToTDB() {
		Dataset dataset = datasetInfo.getDataset();
		Model model = datasetInfo.getModel();

		// Open the dataset for writing
		dataset.begin(ReadWrite.WRITE);
		try {

			for (String source : datasetInfo.getPathFrom()) {
				
				// Loads folder and list of files sorted by modified date
				File folder = new File(source);
				File[] listOfFiles = folder.listFiles(new FilenameFilter() {
				    public boolean accept(File dir, String name) {
				        return name.toLowerCase().endsWith(".ttl") /*|| name.toLowerCase().endsWith(".json")*/;
				    }
				});
				//Arrays.sort(listOfFiles, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);

				for (File file : listOfFiles) {
					System.out.print("Imported: " + file.getName());
					System.out.print(" - File size: " + (double) (file.length() / (1024)) + "KB");
					try {
						TDBLoader.loadModel(model, file.getAbsolutePath());	
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
					
					System.out.println(" - Model Size: " + model.size());
					dataset.commit();
					System.out.println("Commit");
					
					if ((file.length() / (1024)) < 1000) {//10 seg
						Thread.sleep(10000);
					} else if ((file.length() / (1024)) < 1000000) {//20 min
						Thread.sleep(1200000);
					} else if ((file.length() / (1024)) < 3000000) {
						Thread.sleep(1800000);
					} else {
						Thread.sleep(3600000);
					}
					
					System.out.println("Begin");
					dataset.begin(ReadWrite.WRITE);
					
				}
			}
			
			// commit transaction
			dataset.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			dataset.end();
		}
		//this.calcDiscriminability();
		return this;
	}
		
	/**
	 * Checks if database is populated
	 * 
	 * @return boolean
	 */
	public boolean isPopulated() {

		Model model = this.datasetInfo.getModel();

		// Query
		String queryString = " SELECT *"
				+ " WHERE { " 
				+ " { ?s ?p ?o . } "
				+ "} LIMIT 1 ";

		ResultSet results = null;
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			results = qexec.execSelect();
			if (results.hasNext()) {
				return true;
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Gets persons instances
	 * 
	 * @return ResultSet
	 */
	public ResultSet getSetOfInstances(String domain) {

		Model model = this.datasetInfo.getModel();

		// Query
		String queryString;
		
		queryString = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " SELECT (count(?instance) as ?qt) "
				+ " WHERE { "
				+ "    ?instance a "+ domain +" ; "
				+ " } ";

		ResultSet results = null;
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			results = qexec.execSelect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}
	
	public static void main(String args[]){
		
		DataSetInfo ds = new DataSetInfo("DBPEDIA", "DBPEDIA");
		ds.addSource("core");
		DataSetLoader loader = new DataSetLoader(ds);
		loader.loadRDFFilesToTDB();
	}
}
