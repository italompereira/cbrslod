package br.com.model;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.rdf.model.Resource;

public class Instance implements Serializable {
	private static final long serialVersionUID = 1L;
	private String uRI;
	private List<InstanceNeighborhood> instanceNeighborhoodList;
	
	public Instance(Resource instance){
		this.uRI = instance.toString();
		this.instanceNeighborhoodList = new ArrayList<>();
	}
	
	public Instance(String instance){
		this.uRI = instance;
		this.instanceNeighborhoodList = new ArrayList<>();
	}
		
	public List<InstanceNeighborhood> getInstanceNeighborhoodList() {
		return instanceNeighborhoodList;
	}
	
	public String getURI() {
		return uRI;
	}

	public void setURI(String uri) {
		this.uRI = uri;
	}
	
	public String getShortURI(){
		try {
			URI myURI = new URI(this.uRI);
			return FilenameUtils.getName(myURI.getPath()); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int countPredicateOnNeighborhoodList(Predicate predicate){
		int qt = 0;
		int index = (predicate.getLevel()*2)-2;
		for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodList) {
			int size = instanceNeighborhood.getNeighborhood().size();
			
			if (index < size && instanceNeighborhood.getNeighborhood().get(index).toString().equals(predicate.getURI())) {
				qt++;
			}
		}
		return qt;
	}
	
	@Override
	public boolean equals(Object o){
	    if(o instanceof Instance){
	    	Instance toCompare = (Instance) o;
	        return this.uRI.equals(toCompare.uRI);
	    }
	    return false;
	}
	
	@Override
	public int hashCode() {
	    return uRI.hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.uRI;
	}
}
