package br.com.model.DataSet.FilmCL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankCL {
	public String resource;
	public Map<String, Integer> associatedResources = new HashMap<>();
	
	public RankCL(String resource) {
		super();
		this.resource = resource;
	}

	public Map<String, Integer> getAssociatedResources() {
		return associatedResources;
	}

	@Override
	public String toString() {
		return resource.toString();
	}
	
	
	
	
}
