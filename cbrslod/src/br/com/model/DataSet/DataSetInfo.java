package br.com.model.DataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

public class DataSetInfo {
	
	private String desc;
	private Model model;
	private Dataset dataset;
	private String pathTo;
	private List<String> pathFrom;
	private String pathDatasets = DataSetCopy.TO;
	
	/**
	 * Constructor
	 * 
	 * @param desc Description
	 * @param pathFrom Path from
	 * @param pathTo Path to
	 */
	public DataSetInfo(String desc, String pathTo) {
		this.desc = desc;
		pathFrom = new ArrayList<>();
		this.pathTo = pathDatasets + pathTo;
		
		File folder = new File(this.pathTo);
		folder.mkdir();
		
		File folderTDB = new File(this.pathTo + "/tdb");
		folderTDB.mkdir();
		
		this.dataset = TDBFactory.createDataset(this.pathTo + "/tdb");	
		this.model = this.dataset.getDefaultModel();		
	}
	
	/**
	 * Adds source of rdf files
	 * @param pathFrom
	 */
	public void addSource(String pathFrom){
		this.pathFrom.add(pathTo + "/" + pathFrom);
	}
	
	/**
	 * Get the description of this dataset
	 * 
	 * @return String
	 */
	public String getDesc() {
		return desc;
	}
	
	/**
	 * Gets the model
	 * 
	 * @return Model
	 */
	public Model getModel() {
		return model;
	}
	
	/**
	 * Gets the data set
	 * 
	 * @return Dataset
	 */
	public Dataset getDataset() {
		return dataset;
	}
	
	/**
	 * Gets the path to
	 * 
	 * @return String
	 */
	public String getPathTo() {
		return pathTo;
	}
		
	/**
	 * Gets the path from
	 * 
	 * @return String
	 */
	public List<String> getPathFrom() {
		return pathFrom;
	}
}
