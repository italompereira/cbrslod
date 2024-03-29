package br.com.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import br.com.controller.Compare;
import br.com.controller.Rank;
import br.com.tools.Util;

public class Endpoint extends EndpointInterface{
	/**
	 * endPoint address and domain
	 */
	private String endPoint;
	private String graph;
	private String domain;
	private String domainSparql;
	
	public List<String> instanceFilter;
	public int limitInstanceFilter = 50;
	public int pagesInstanceFilter;
	
	public int qtInstances = 0;
	
	
	private int limitEndpoint = 10000;
	
	/**
	 * Instances and predicates
	 */
	private List<Predicate> predicateList = new ArrayList<>();
	public List<Predicate> getPredicateList() {
		return predicateList;
	}

	private HashSet<Predicate> predicateSet = new HashSet<Predicate>();
	private List<Instance> instanceList = new ArrayList<>();

	private List<Term> termList = new ArrayList<Term>();
	
	/**
	 * Parameters
	 */
	private int numberOfLevels;
	
	private double infThresholdCoverage, supThresholdCoverage, infThresholdDiscriminability, supThresholdDiscriminability;
		
	private boolean updatePredicateCache = false;
	private boolean updateInstanceCache = false;
	private boolean updateZoneIndexCache = false;
	
	/**
	 * Constructor
	 * 
	 * @param domain Domain of the instances
	 */
    public Endpoint(String endPoint, String graph, String domain,String domainSparql, List<String> instanceFilter, int numberOfLevels, double infThresholdCoverage, double supThresholdCoverage, double infThresholdDiscriminability, double supThresholdDiscriminability) {
		super();
		
		/**
		 * Parameters
		 */
		this.endPoint = endPoint;
		this.graph = graph;
		this.domain = domain;
		this.domainSparql = domainSparql;
		this.instanceFilter = instanceFilter;
		this.pagesInstanceFilter = instanceFilter.size()/limitInstanceFilter;
		
		this.numberOfLevels = numberOfLevels;
		this.infThresholdCoverage = infThresholdCoverage;
		this.supThresholdCoverage = supThresholdCoverage;
		this.infThresholdDiscriminability = infThresholdDiscriminability;
		this.supThresholdDiscriminability = supThresholdDiscriminability;
		
		//Make a cache of instances
		if (!getInstanceCache()) {
			this.getSetOfInstances();
			this.updateInstanceCache = true;
			this.saveInstanceCache();
		}
		
		//Make a cache of predicates
		if (!getPredicateCache()) {
			for (int i = 1; i <= this.numberOfLevels; i++) {
				this.getSetOfPredicates(i);	
			}
			
			for (Predicate predicate : predicateSet) {
				predicateList.add(predicate);
			}
			
			this.updatePredicateCache = true;
			this.savePredicateCache();
		}
	}
    
	public List<Instance> getInstanceList() {
		return instanceList;
	}

    public void getSetOfInstances() {
    	
    	String instances = null;
		for (int i = 0; i <= pagesInstanceFilter; i++) {
			instances = getInstanceFilter(i);
			this.getSetOfInstances(0, instances);
			System.out.println("");
		}
    }
    
	/**
     * Gets the set of instances
     * 
     * @param page Page number, this parameter is important because dbpedia does not allow retrieving more than 10000 rows in a search   
     * @return List<Instance> List of instances
     */
    public void getSetOfInstances(int page, String instances) {
    	
		// Query
		String szQuery = null;
		int limit = limitEndpoint;
		int offset = page * limit;
		ResultSet results;

		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				//+ " PREFIX : <http://dbpedia.org/resource/> "
				+ " SELECT DISTINCT ?i "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { "
				+ " 	?i a "+ this.domainSparql +" ; "
				+ (instances == null ? " " : " 	FILTER(?i IN ("+ instances +")) ")
				+ " } LIMIT " + limit + " OFFSET " + offset ;

		// Query Endpoint
		try {
			results = this.queryEndpoint(szQuery, endPoint);
			
			if (results.hasNext()) {
				int iCount = (page*limit) + 0;
				while (results.hasNext()) {
					QuerySolution qs = results.nextSolution();
					Resource instance = qs.getResource("i");
					instanceList.add(new Instance(instance));
					
					// Count
					iCount++;
					System.out.print("Result " + iCount + ": ");
					System.out.println("[ Instance ]: " + instance.toString());
				}
				this.close();
				this.getSetOfInstances(++page, instances);
			} else {
				this.close();
			}
				
			} catch (Exception ex) {
				System.err.println(ex);
			}
	}  
    
    private String getInstanceFilter(int page){
		String instances = null;
		int instanceFilterSize = instanceFilter.size();
		int limitInstance = limitInstanceFilter;
		int offset = limitInstance * page;
		int end = (offset+limitInstance>instanceFilterSize) ? instanceFilterSize : (offset+limitInstance);
		
		List<String> subList = instanceFilter.subList(offset, end);
		instances = "<http://dbpedia.org/resource/"+StringUtils.join(subList, ">, <http://dbpedia.org/resource/") + ">";
		return instances;
    }
  
    public void getSetOfPredicates(int level) {
    	
    	String instances = null;
		for (int i = 0; i <= pagesInstanceFilter; i++) {
			instances = getInstanceFilter(i);
			this.getSetOfPredicates(level, instances);
			System.out.println("");
		}
    }
    
	/**
	 * Get the set of predicates based on level
	 * 
	 * @param level
	 */
	public void getSetOfPredicates(int level, String instances) {
		String unions = this.buildUnionsQuery(level);
		String szQuery = null;
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				//+ " PREFIX : <http://dbpedia.org/resource/> "
				+ " SELECT DISTINCT ?p" + level + " "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE "
				+ " { "
				+ unions
				+ (instances == null ? "" : " 	FILTER(?i IN ("+ instances +")) ")
				+ (level == 1 ? "" : " 	FILTER(?p" + level + " != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) ")
				+ " } ";	
		
		this.getSetOfPredicatesFromLevel(0, level, szQuery);
	}
	
	/**
	 * Get the set of predicates based on level
	 * 
	 * @param page
	 * @param level
	 * @param query
	 */
	private void getSetOfPredicatesFromLevel(int page, int level, String query) {

		// Query
		int limit = limitEndpoint;
		int offset = page * limit;
		String szQuery = query;
		szQuery += " LIMIT " + limit + " OFFSET " + offset;

		// Query Endpoint
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			if (results.hasNext()) {
				int iCount = (page * limit) + 0;
				while (results.hasNext()) {
					// Get Result
					QuerySolution qs = results.next();
					Resource predicate = qs.getResource("p"+level);
					predicateSet.add(new Predicate(predicate.toString(), level));

					// Count
					iCount++;
					System.out.print("Result " + iCount + ": ");
					System.out.println("[ Predicate ]: " + predicate);
				}
				this.close();
				this.getSetOfPredicatesFromLevel(++page, level, query);
			} else {
				this.close();
			}
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}

	/**
	 * Builds the unions based on quantity of levels
	 * 
	 * @param level
	 * @return String with union
	 */
	private String buildUnionsQuery(int level){
		String subject = "?s";
		String predicate = "?p";
		String object = "?o";
		
		List<String> levels = new ArrayList<String>();
		
		if (level == 1) {
			levels.add("?i a "+this.domainSparql+" . "
					+ "?i ?p1 ?leaf . ");
			levels.add("?i a "+this.domainSparql+" . "
					+ "?leaf ?p1 ?i . ");	
		} else {
			levels.add("?i a "+this.domainSparql+" . "
					+ "?i ?p1 ?o1 . ");
			levels.add("?i a "+this.domainSparql+" . "
					+ "?s1 ?p1 ?i . ");
		}
		
		for (int i = 2; i <= level; i++) {
			List<String> levelsAux = new ArrayList<String>();
			for (int j = 0; j < levels.size(); j++) {
				String query = levels.get(j);
		
				if (j%2==0) {
					if (level == i) {
						levelsAux.add(query + object+(i-1) +" "+ predicate+(i) + " ?leaf . ");	
						levelsAux.add(query + " ?leaf " + predicate+(i) +" "+ object+(i-1) +" . "
								+ "FILTER(!isLiteral("+ object+(i-1) +"))"
								//+ "FILTER(regex("+ predicate+(i-1) +", \"^http://dbpedia.org/ontology/\") || ("+ predicate+(i-1) +" = \"http://purl.org/dc/terms/subject\"))"
								);	
					} else {
						levelsAux.add(query + object+(i-1) +" "+ predicate+(i) +" "+ object+(i) +" . ");
						levelsAux.add(query + subject+(i) +" "+ predicate+(i) +" "+ object+(i-1) +" . ");
					}
				} else {
					if (level == i) {
						levelsAux.add(query + subject+(i-1) +" "+ predicate+(i) + " ?leaf . ");
						levelsAux.add(query + " ?leaf " + predicate+(i) +" "+ subject+(i-1) +" . "
								+ "FILTER(!isLiteral("+ subject+(i-1) +"))"
								//+ "FILTER(regex("+ predicate+(i-1) +", \"^http://dbpedia.org/ontology/\") || ("+ predicate+(i-1) +" = \"http://purl.org/dc/terms/subject\"))"
								);
					} else {
						levelsAux.add(query + subject+(i-1) +" "+ predicate+(i) +" "+ object+(i) +" . ");
						levelsAux.add(query + subject+(i) +" "+ predicate+(i) +" "+ subject+(i-1) +" . ");
					}
				}
			}
			levels = new ArrayList<>(levelsAux);
			levelsAux.clear();
		}
		
		String unions = " { " + StringUtils.join(levels, " } UNION {") + " } ";
		
		return unions;  
	}
    
    /**
     * Gets neighborhoods for all instances
     */
    public void getInstanceNeighborhood(){
    	
//    	this.numberOfLevels = 1;
//    	int index = instanceList.indexOf(new Instance("http://dbpedia.org/resource/John_C._Freeman_Weather_Museum"));
//    	Instance instanceT = instanceList.get(index);
//    	this.getInstanceNeighborhood(instanceT.getURI(), null, null, instanceT.getInstanceNeighborhoodList(), new ArrayList<>(), 1);
//		System.out.print(instanceT.toString());
//			for (InstanceNeighborhood instanceNeighborhood : instanceT.getInstanceNeighborhoodList()) {
//				for (Node node : instanceNeighborhood.getNeighborhood()) {
//					System.out.print(" \t" + node.getNode().substring(0, (node.getNode().length() > 70 ? 70: node.getNode().length())));
//				}
//				System.out.println();
//			}
//		System.out.print(" - " +instanceT.getInstanceNeighborhoodList().size()+"\n");
//		instanceT.getInstanceNeighborhoodList();
    	
    	int i = 0;
    	for (Instance instance : instanceList) {
			
		
    	//instanceList.stream().parallel().forEach( instance -> {
    		if (instance.getInstanceNeighborhoodList().size() > 0) {
    			
    			System.out.println();
//    			for (InstanceNeighborhood instanceNeighborhood : instance.getInstanceNeighborhoodList()) {
//    				for (Node node : instanceNeighborhood.getNeighborhood()) {
//    					System.out.print(" \t" + node.getNode().substring(0, (node.getNode().length() > 70 ? 70: node.getNode().length())));
//    				}
//    				System.out.println();
//    			}
    			System.out.print(instance + " - " +instance.getInstanceNeighborhoodList().size()+"\n");	
    			
				continue;
    			//return;
			}
    		this.updateInstanceCache = true;
    		this.updatePredicateCache = true;
    		
	    	instance.getInstanceNeighborhoodList().clear();
	    	System.out.print(i++ + "-" + instance.toString());
			this.getInstanceNeighborhood(instance.getURI(), null, null, instance.getInstanceNeighborhoodList(), new ArrayList<>(), 1);
				
//			System.out.println();
//			for (InstanceNeighborhood instanceNeighborhood : instance.getInstanceNeighborhoodList()) {
//				for (Node node : instanceNeighborhood.getNeighborhood()) {
//					System.out.print(" \t" + node.getNode().substring(0, (node.getNode().length() > 70 ? 70: node.getNode().length())));
//				}
//				System.out.println();
//			}
			System.out.print(" - " +instance.getInstanceNeighborhoodList().size()+"\n");	
		//});
    	}
    	
    	//Updates the cache of Instances
    	saveInstanceCache();
    	
    	for (Predicate predicate : predicateList) {
    		List<Instance> instanceListAux = new ArrayList<Instance>(predicate.getTFInstances().keySet());
    		for (Instance instance : instanceListAux) {
    			predicate.getTFInstances().remove(instance);
    			predicate.getTFInstances().put(instanceList.get(instanceList.indexOf(instance)), 0.0);
			}
		}
    	
    	savePredicateCache();
    }
    
    /**
     * Gets the neighborhood until the max level
     * 
     * @param resource Resource to find neighborhood
     * @param previousResource Resource used in the previous level
     * @param previousPredicate Predicate of the previous resource
     * @param instanceNeighborhoodList  List of neighborhoods for the instance(Resource)
     * @param previousNeighborhood List of neighborhoods for the instance(Resource) in the previous level
     * @param level Number of the level
     */
    public boolean getInstanceNeighborhood(String resource, String previousResource, Resource previousPredicate, List<InstanceNeighborhood> instanceNeighborhoodList, List<Node> previousNeighborhood, int level){
    	List<QuerySolution> qsList = this.queryInstanceNeighborhood(resource, previousResource, previousPredicate,0);
    
    	List<Node> neighborhood;

    	//if (level == 1) {
			//System.out.print("("+qsList.size()+")");
		//}
    	
		boolean added = false;
    	for (QuerySolution qs : qsList) {
			neighborhood = new ArrayList<>(previousNeighborhood);	
			
			//if (level == 1) {
				//System.out.print(" " + qsList.indexOf(qs));
			//}
			
			Resource predicate = qs.getResource("predicate");
			RDFNode object = qs.get("object");
			
			//Check if the predicate is present on predicates list
			if (!predicateSet.contains(new Predicate(predicate.toString(), level)) /*&& (level == this.numberOfLevels)*/) {
				continue;
			} 
			
			/**
			 * Clean literal values
			 */
			String node = null;
			if (object instanceof Literal) {		
				node = object.asLiteral().getLexicalForm();
				if (NumberUtils.isCreatable(node)) {
					continue;
				}
				
				node = Util.removeStopWords(node);
				node = Util.removePunctuation(node);
				node = Util.stemming(node);
				
				
			} else {
				node = object.toString();
			}
			
			
			neighborhood.add(new Node(predicate.getURI(), predicate.getClass().getSimpleName())); 
			neighborhood.add(new Node(node, object.getClass().getSimpleName()));
			
			if (object instanceof Literal || level == this.numberOfLevels || predicate.toString().equals("http://dbpedia.org/ontology/location")) {
				
//				if (!predicateSet.contains(new Predicate(predicate.toString(), level))) {
//					continue;
//				} 
				
				instanceNeighborhoodList.add(new InstanceNeighborhood(neighborhood));
				added = true;
			} else			
			if (level < this.numberOfLevels && !object.toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
				//int size = neighborhood.size(); 
				boolean addedR = this.getInstanceNeighborhood(object.toString(), resource, predicate, instanceNeighborhoodList, neighborhood, level+1);
				if (!addedR /*&& (size != neighborhood.size())*/) {
					instanceNeighborhoodList.add(new InstanceNeighborhood(neighborhood));
					added = true;
				}
			}
		}
    	return added;
    }
    
    /**
     * Queries the instance neighborhood in ENDPOINT.
     * 
     * @param resource Resource to find neighborhood
     * @param previousResource Resource used in the previous level
     * @param previousPredicate Predicate of the previous resource
     * @return List<QuerySolution> List of neighborhood for the instance
     */
    public List<QuerySolution> queryInstanceNeighborhood(String resource, String previousResource, Resource previousPredicate, int page){
    	
    	
		// Query
		String szQuery = null;
		int limit = limitEndpoint;
		int offset = page * limit;
		
		// Query
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " SELECT ?predicate ?object "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { " 
				+ " { <" + resource + "> ?predicate ?object . } "
				+ " UNION " 
				+ " { "
					+ "?object ?predicate <" + resource + "> . "
					+ (previousPredicate == null ? "" : " FILTER(str(?predicate) != \""+ previousPredicate.toString() +"\") ")
				+ " }  "
				//+ " FILTER (!regex(?object, \"http://www.wikidata.org/entity\",\"i\")) . "
				//+ " FILTER(regex(?predicate, \"^http://dbpedia.org/ontology/\") || (?predicate = \"http://purl.org/dc/terms/subject\")) "
				//+ " FILTER(str(?predicate) != \"http://dbpedia.org/ontology/wikiPageWikiLink\" ) "
				+ " FILTER(!isLiteral(?object) || lang(?object) = \"\" || langMatches(lang(?object), \"EN\"))"
				+ (previousResource == null ? "" : " FILTER(str(?object) != \""+ previousResource.toString() +"\" ) ")
				+ " } LIMIT " + limit + " OFFSET " + offset ;

		List<QuerySolution> querySolutionList = new ArrayList<>();
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			if (results.hasNext()) {
				while (results.hasNext()) {
					querySolutionList.add(results.nextSolution());
				}
				//this.close();
				querySolutionList.addAll(this.queryInstanceNeighborhood(resource, previousResource, previousPredicate, ++page));
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(this.getQuery());
		}

		return querySolutionList;
    }
    
    private int countInstance() {
    	HashSet<Resource> qt = new HashSet<Resource>();
    	String instances = null;
    	System.out.print("Counting instances");
		for (int i = 0; i <= pagesInstanceFilter; i++) {
			instances = getInstanceFilter(i);
			this.countInstance(instances, qt, 0);
			System.out.print(".");
		}
		System.out.println();
    	return qt.size();
    }
	
	/**
	 * Count all instances in domain
	 * 
	 * @return Integer Number of instances
	 */
	private void countInstance(String instances, HashSet<Resource> qt, int page) {

		// Query
		String szQuery = null;
		
		int limit = 10000;
		int offset = page * limit;
		
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " SELECT ?i "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { "
				+ " 	?i a "+ this.domainSparql +" ; "
				+ (instances == null ? "" : " 	FILTER(?i IN ("+ instances +")) ")
				+ " } LIMIT " + limit + " OFFSET " + offset ;

		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			if (results.hasNext()) {
				while (results.hasNext()) {
					QuerySolution qsol = results.nextSolution();
					Resource r = qsol.getResource("i");
					qt.add(r);
				}
				this.close();
				this.countInstance(instances, qt, ++page);
			} else {
				this.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private void countTripesWithPredicate(Predicate predicate, HashSet<RDFNode> qtD, List<RDFNode> qtND) throws Exception {
    	String instances = null;
		for (int i = 0; i <= pagesInstanceFilter; i++) {
			instances = getInstanceFilter(i);
			this.countTripesWithPredicate(predicate, instances, qtD, qtND, 0);
			
		}
    }
    
	/**
	 * Count triples with this predicate
	 * 
	 * @param predicate The predicate
	 * @param distinct If will be distinct or not
	 * @return double Number of triples
	 * @throws Exception 
	 */
	private void countTripesWithPredicate(Predicate predicate, String instances, HashSet<RDFNode> qtD, List<RDFNode> qtND, int page) throws Exception {

		// Query
		String szQuery = null;
		
		int limit = 10000;
		int offset = page * limit;
		
		int level = predicate.getLevel();
		String unions = this.buildUnionsQuery(level);
		unions = unions.replace("?p"+level, "<"+predicate.getURI()+">");
				
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " SELECT ?leaf "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { "
				+ unions
				+ (instances == null ? "" : " 	FILTER(?i IN ("+ instances +")) ")
				+ " } LIMIT " + limit + " OFFSET " + offset ;

		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			if (results.hasNext()) {
				while (results.hasNext()) {
					QuerySolution qsol = results.nextSolution();
					RDFNode qtNode = qsol.get("leaf");
					qtD.add(qtNode);
					qtND.add(qtNode);
				}
				this.close();
				countTripesWithPredicate(predicate, instances, qtD, qtND, ++page);
			} else {
				this.close();	
			}
			
		} catch (Exception e) {
			System.out.println("Exception: " + predicate);
			System.out.println(this.getQuery());
			throw e;
		}
	}
	
	
    private HashSet<Instance> getInstancesWithPredicate(Predicate predicate) throws Exception {
    	HashSet<Instance> instanceWithPredicate = new HashSet<>();
    	String instances = null;
		for (int i = 0; i <= pagesInstanceFilter; i++) {
			instances = getInstanceFilter(i);
			instanceWithPredicate.addAll(this.getInstancesWithPredicate(predicate, instances, 0));
		}
		return instanceWithPredicate;
    }
    
	/**
	 * Gets only instances with this predicate
	 * 
	 * @param predicate The predicate
	 * @param page Number of the page
	 * @return List<Instance> Instances
	 * @throws Exception 
	 */
	private HashSet<Instance> getInstancesWithPredicate(Predicate predicate, String instances, int page) throws Exception  {

		// Query
		String szQuery = null;
		
		int limit = 10000;
		int offset = page * limit;
		
		int level = predicate.getLevel();
		String unions = this.buildUnionsQuery(level);
		unions = unions.replace("?p"+level, "<"+predicate.getURI()+">");
		
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " SELECT DISTINCT ?i "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { "
				+ unions
				+ (instances == null ? "" : " 	FILTER(?i IN ("+ instances +")) ")
				+ " } LIMIT " + limit + " OFFSET " + offset ;

		HashSet<Instance> instanceWithPredicate = new HashSet<>();
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			
			if (results.hasNext()) {
				while (results.hasNext()) {
					QuerySolution qs = results.nextSolution();
					Resource instance = qs.getResource("i");
					
					if (instance == null) {
						System.out.println("BUG");
					}
					
					instanceWithPredicate.add(new Instance(instance));
				}
				this.close();	
				instanceWithPredicate.addAll(this.getInstancesWithPredicate(predicate, instances, ++page));
			} else {
				this.close();
			}
		} catch (Exception e) {
			System.out.println(predicate);
			System.out.println(instances);
			System.out.println(this.getQuery());
			throw e;
		}

		return instanceWithPredicate;
	}
	
	/**
	 * Calculates the predicate frequency for each predicate on instance
	 */
	public void calcPredicateFrequencyOnInstance() {
		List<Instance> isntanceURIList = null;
		
		//predicateList.stream().parallel().forEach( predicate -> {
		for (Predicate predicate : predicateList) {
			if (predicate.isCalcTermFreq()) {
				//return;
				continue;
			}
			this.updatePredicateCache = true;
			
			/**
			 * Calculates the Term Frequency in documents
			 */
			double qtPredicate = 0.0;
			double qtPredicates = 0.0;
			
			isntanceURIList = new ArrayList<Instance>(predicate.getTFInstances().keySet());
			for (Instance instance : isntanceURIList) {
				qtPredicate = instance.countPredicateOnNeighborhoodList(predicate);
				qtPredicates = instance.getInstanceNeighborhoodList().size();
				predicate.getTFInstances().put(instance, qtPredicate/qtPredicates);
			}
			
			predicate.setCalcTermFreq(true);
		}
		//});
		
		//Updates the cache of Instances
		savePredicateCache();
	}
	
	/**
	 * Calculates the predicates statistics.
	 * 
	 * @return List<Predicate> The predicates list
	 */
	public List<Predicate> calcPredicateStats() {
		int qtInstances;
		if (this.qtInstances == 0) {
			this.qtInstances = this.countInstance();			
		}
		qtInstances = this.qtInstances;

//		Double maxDiscrinability = 0.0;
//		Double maxPredicateFrequency = 0.0;
		
		Comparator<Predicate> comparator = Comparator.comparingInt(Predicate::getLevel)
	    		.thenComparing(Predicate::getPredicateFrequency, Comparator.reverseOrder())
	    		.thenComparing(Predicate::getDiscriminability, Comparator.reverseOrder());
		 
		//Sort by level and discriminability
		Collections.sort(predicateList, comparator);
		
//		for (Predicate predicate : predicateList) {
//			System.out.println(predicate);
//		}
		
		System.out.println("Starting calc");
		int count = 0;
		for (Predicate predicate : predicateList) {
		//predicateList.stream().parallel().forEach(	predicate -> {
		
			
			if ((predicate.getURI().contains("http://purl.org") && predicate.getLevel() > 1) || predicate.getURI().contains("http://www.w3.org")  || predicate.getURI().contains("http://dbpedia.org/property/wordnet_type")) {
				continue;
			}
			
			
			try {
				
				if (predicate.isCalcStats()) {
					System.out.println(predicate);
					continue;
	//				return;
				}
				Thread.sleep(500);
				
				//predicate = predicateList.get(predicateList.indexOf(new Predicate("http://www.w3.org/1999/02/22-rdf-syntax-ns#type",1)));
				
				//System.out.print("Starting - " + predicate.getURI() + ". Level - " + predicate.getLevel());
				this.updatePredicateCache = true;
				
				System.out.println(" --> " + predicate.getURI());
				
		       	/**
		    	 * Calculates the discriminability
		    	 */
		    	HashSet<RDFNode> qtD = new HashSet<RDFNode>();
		    	List<RDFNode> qtND = new ArrayList<RDFNode>();
		    	this.countTripesWithPredicate(predicate, qtD, qtND);
		    	
				double qtWithPredicate = qtND.size();
				double qtDistinctWithPredicate = qtD.size();
				double discriminability = qtDistinctWithPredicate / qtWithPredicate;
				predicate.setDiscriminability(discriminability);
				qtD = null;
				qtND = null;
	//			if (discriminability > maxDiscrinability) {
	//				maxDiscrinability = discriminability;
	//			}
				
		    	/**
		    	 * Calculates the predicate frequency
		    	 */
				HashSet<Instance> instanceWithPredicate = this.getInstancesWithPredicate(predicate);
				int qtInstancesWithPredicate = instanceWithPredicate.size();
				double predicateFrequency = (double)qtInstancesWithPredicate / qtInstances;
				predicate.setPredicateFrequency(predicateFrequency);
	//			if (predicateFrequency > maxPredicateFrequency) {
	//				maxPredicateFrequency = predicateFrequency;
	//			}
				
		    	/**
		    	 * Calculates the Inverse Document Frequency IDF
		    	 */
				double idf;
				if (qtInstancesWithPredicate > 0) {
					idf = 1.0 + Math.log((double)qtInstances/qtInstancesWithPredicate);	
				} else {
					idf = 1.0;
				}
				predicate.setIdf(idf);
				
				/**
				 * Assign the instances that the predicate is present to calculate the term frequency
				 */
				for (Instance instanceAux : instanceWithPredicate) {
					
					int index = instanceList.indexOf(instanceAux);
					
					Instance instance = null;
					try {
						instance = instanceList.get(index);
					} catch (Exception e) {
						System.out.println(index);
					}
	
					predicate.getTFInstances().put(instance, 0.0);
				}
				
				predicate.setCalcStats(true);
				
				System.out.println(" --> " + predicate);
			
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			count++;
			if (count % 10 == 0) {
				System.out.println("Saving");
				savePredicateCache();
			}
			
//		});
		}

//		// Normalize discriminability and coverage by max
//		for (Predicate predicate : predicateList) {
//			predicate.setDiscriminability(predicate.getDiscriminability()/Predicate.maxDiscrinability);
//			predicate.setCoverage(predicate.getCoverage()/Predicate.maxCoverage);
//		}

		//Updates the cache of predicates
		savePredicateCache();
		
		//Filter the predicates list based on coverage and discriminability
		predicateList = predicateList.stream()
			.filter(
					x -> 
					x.getPredicateFrequency() >= this.infThresholdCoverage &&
					x.getPredicateFrequency() <= this.supThresholdCoverage &&
					x.getDiscriminability() >= this.infThresholdDiscriminability &&
					x.getDiscriminability() <= this.supThresholdDiscriminability &&
					x.getLevel() <= this.numberOfLevels
					)
			.collect(Collectors.toList());
		
		/*Comparator<Predicate>*/ comparator = Comparator.comparingInt(Predicate::getLevel)
	    		.thenComparing(Predicate::getPredicateFrequency, Comparator.reverseOrder())
	    		.thenComparing(Predicate::getDiscriminability, Comparator.reverseOrder());
		
		//Sort by level and discriminability
		Collections.sort(predicateList, comparator);
		
		int i = 0;
		for (Predicate predicate : predicateList) {
			System.out.println((++i) + " \t| " + predicate.toString());
			predicateSet.add(predicate);
		}
		
		return predicateList;
	}
	
	
	
	/**
	 * Builds the inverted index list by zone
	 */
	public void calcZoneIndex(){
		
		if (getZoneIndexCache()) {
			return;
		}
		
		for (Instance instance : instanceList) {
			List<InstanceNeighborhood> lN = instance.getInstanceNeighborhoodList();
			
			for (InstanceNeighborhood n : lN) {
				Node lastElement = n.getLastElement();
				Node lastPredicate = n.getLastPredicate();
				
				if (lastElement.getTypeOfNode().equals("LiteralImpl")) {
					String[] pieces = Util.split(lastElement.getNode(), " "); 
					
					for (String piece : pieces) {
						
						Term term = new Term(piece);
						if (!this.termList.contains(term)) {
							termList.add(term);
						} else {
							int index = termList.indexOf(term);
							term = termList.get(index);
						}
						
						List<PredicateTerm> predicateList = term.getPredicateList();
						PredicateTerm predicate = new PredicateTerm(lastPredicate.toString());
						if (!predicateList.contains(predicate)) {
							predicateList.add(predicate);
						} else {
							int index = predicateList.indexOf(predicate);
							predicate = predicateList.get(index);
						}
						
						List<Instance> instanceList = predicate.getInstanceList();
						if (!instanceList.contains(instance)) {
							instanceList.add(instance);
						} 
					}
				} else {
					String piece = lastElement.getNode();
					Term term = new Term(piece);
					if (!this.termList.contains(term)) {
						termList.add(term);
					} else {
						int index = termList.indexOf(term);
						term = termList.get(index);
					}
					
					List<PredicateTerm> predicateList = term.getPredicateList();
					PredicateTerm predicate = new PredicateTerm(lastPredicate.toString());
					if (!predicateList.contains(predicate)) {
						predicateList.add(predicate);
					} else {
						int index = predicateList.indexOf(predicate);
						predicate = predicateList.get(index);
					}
					
					List<Instance> instanceList = predicate.getInstanceList();
					if (!instanceList.contains(instance)) {
						instanceList.add(instance);
					} 
				}
			}
		}
		
		this.updateZoneIndexCache = true;
		saveZoneIndexCache();
	}
	
	
	
	
	
	/**
	 * Compare - This method compares based on predicates
	 */
	public void compareBasedOnPredicates(){
		
		double[][] simMatrixCache = getSimMatrixCache("SimMatrix.ser");
		double[][] simMatrix;
		if (simMatrixCache != null) {
			simMatrix = simMatrixCache;
		} else {
			int numberInstances = instanceList.size();
			simMatrix = new double[numberInstances][numberInstances];
			
			long startTime = System.currentTimeMillis();
			
			for (Predicate predicate : predicateList) {
				System.out.println(predicate.getURI());
				List<Instance> instanceListAux = new ArrayList<Instance>(predicate.getTFInstances().keySet());
				
				
				for (int i = 0; i < instanceListAux.size(); i++) {
				
				//IntStream.range(0, instanceListAux.size()).parallel().forEach( i -> {
					Instance a = instanceListAux.get(i);
					int indexA = this.instanceList.indexOf(a);
					
					for (int j = i+1; j < instanceListAux.size(); j++) {
						Instance b = instanceListAux.get(j);
						int indexB = this.instanceList.indexOf(b);
						
						if (simMatrix[indexA][indexB] != 0) {
							continue;
						}
						System.out.println("Compare " + a + " with " + b);
						
						double score = Compare.compareCosine(a, b, predicateList, true);
						
						simMatrix[indexA][indexB] += score;
						simMatrix[indexB][indexA] = simMatrix[indexA][indexB];
					}
				//});
				}
			}
			
			long stopTime = System.currentTimeMillis();
			long elapsedTime = (stopTime - startTime)/1000/60;
			System.out.println(elapsedTime + "m");
			
			saveSimMatrixCache(simMatrix);
		}
		
		System.out.println("Building Rank list...");		
		try {
			File file = new File("museums.txt");
	
			List<String> lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
			
			int[] indexArray = new int[lines.size()];
			for (int i = 0; i < lines.size(); i++) {
				String instance = lines.get(i).replace(' ', '_');
				int index = this.instanceList.indexOf(new Instance("http://dbpedia.org/resource/"+instance));
				indexArray[i] = index;
			}
			
			List<Rank> rankList = new ArrayList<>();
			for (int i = 0; i < indexArray.length; i++) {
				Rank rank = new Rank(this.instanceList.get(indexArray[i]).getShortURI());
				
				for (int j = 0; j < indexArray.length; j++) {
					if(i == j)  continue;
					
					rank.addInstanceSim(this.instanceList.get(indexArray[j]).getShortURI(), simMatrix[indexArray[i]][indexArray[j]]);
				}
				
				System.out.println(rank.getInstance());
				System.out.println("\t"+rank.getUnrankedInstances());
				System.out.println("\t"+rank.getRankedInstances()+"\n");
				
				rankList.add(rank);
			}
			
			saveRank(rankList,"Rank.ser");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Compare - This method compares the instances one by one
	 */
	public void compareBasedOnInstances(String method){
		
		double[][] simMatrixCache = getSimMatrixCache("SimMatrix.ser");
		double[][] simMatrix;
		if (simMatrixCache != null) {
			simMatrix = simMatrixCache;
		} else {
			int numberInstances = instanceList.size();
			simMatrix = new double[numberInstances][numberInstances];
			long startTime = System.currentTimeMillis();
				
			for (int i = 0; i < instanceList.size(); i++) {
			//IntStream.range(0, instanceList.size()).parallel().forEach( i -> {
				Instance a = instanceList.get(i);
				System.out.println("Compare [" + a + "(" + a.getInstanceNeighborhoodList().size() +")]");
				for (int j = i+1; j < instanceList.size(); j++) {
					Instance b = instanceList.get(j);
					
					if (simMatrix[i][j] != 0) {
						continue;
					}
					//System.out.println("Compare [" + a + "(" + a.getInstanceNeighborhoodList().size() +")] with [" + b + "(" + b.getInstanceNeighborhoodList().size() +")]");
					
					double score = 0.0;
					switch (method) {
					case "NeighborhoodCosine":
						score = Compare.compareCosine(a, b, predicateList, false);
						break;
					case "NeighborhoodSoftTF-IDF":
						score = Compare.compareSoftTFIDF(a, b, predicateList, false);
						break;
					case "NeighborhoodCosineJaccard":
						score = Compare.compareCosine(a, b, predicateList, true);
						break;
					case "NeighborhoodSoftTF-IDFJaccard":
						score = Compare.compareSoftTFIDF(a, b, predicateList, true);
						break;
					case "TextCosine":
						score = Compare.compareByTextCosine(a, b);
						break;
					case "TextSoftTF-IDF":
						score = Compare.compareByTextSoftTFIDF(a, b);
						break;

					default:
						break;
					}
					//double score = Compare.compareSoftTFIDF(a, b, predicateList);
					//double score = Compare.compareByText(a, b);
					//double score = Compare.compareCosine(a, b, predicateList);
									
					simMatrix[i][j] += score;
					simMatrix[j][i] = simMatrix[i][j];
				}
			//});
			}
			
			long stopTime = System.currentTimeMillis();
			long elapsedTime = (stopTime - startTime)/1000/60;
			System.out.println(elapsedTime + "seconds");
			
			saveSimMatrixCache(simMatrix);
		}
		
		System.out.println("Building Rank list...");		
		try {
			String fileNameI = "./src/br/com/model/DataSet/"+domain.replace(":", "").substring(3)+"/instances.txt";
			File file = new File(fileNameI);
	
			List<String> lines = FileUtils.readLines(file, StandardCharsets.UTF_8);

			
			lines.remove(0);
			
			int[] indexArray = new int[lines.size()];
			for (int i = 0; i < lines.size(); i++) {
				String instance = lines.get(i).replace(' ', '_');
				int index = this.instanceList.indexOf(new Instance("http://dbpedia.org/resource/"+instance ));
				if (index == -1) {
					continue;
				}
				indexArray[i] = index;
			}
			
			List<Rank> rankList = new ArrayList<>();
			for (int i = 0; i < indexArray.length; i++) {
				Rank rank = new Rank(this.instanceList.get(indexArray[i]).toString().substring(28));
				
				for (int j = 0; j < indexArray.length; j++) {
					if(i == j)  continue;
					
					rank.addInstanceSim(this.instanceList.get(indexArray[j]).toString().substring(28), simMatrix[indexArray[i]][indexArray[j]]);
				}
				
				System.out.println(rank.getInstance());
				System.out.println("\t"+rank.getUnrankedInstances()+"\n");
				System.out.println("\t"+rank.getRankedInstances()+"\n");
				
				rankList.add(rank);
			}
			
			saveRank(rankList,"Rank.ser");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	/**
//	 * Compare - Method 2
//	 */
//	public void compare2(){
//		double[][] simMatrixCache = getSimMatrixCache("dboMuseumSimMatrix.compare1B.1124m.ser");
//		double[][] simMatrix;
//		if (simMatrixCache != null) {
//			simMatrix = simMatrixCache;
//		} else {
//			int numberInstances = instanceList.size();
//			simMatrix = new double[numberInstances][numberInstances];
//			
//			long startTime = System.currentTimeMillis();
//			
//			for (Predicate predicate : predicateList) {
//				System.out.println(predicate.getURI());
//				List<Instance> instanceListAux = new ArrayList<Instance>(predicate.getTFInstances().keySet());
//				
//				IntStream.range(0, instanceListAux.size()).parallel().forEach( i -> {
//					Instance a = instanceListAux.get(i);
//					int indexA = this.instanceList.indexOf(a);
//					
//					for (int j = i+1; j < instanceListAux.size(); j++) {
//						Instance b = instanceListAux.get(j);
//						int indexB = this.instanceList.indexOf(b);
//						
//						double tFIDFA = predicate.getTFIDF(a);
//						double tFIDFB = predicate.getTFIDF(b);
//						double average = (tFIDFA+tFIDFB)/2;
//						double score = average*Compare.compare2A(a, b, predicate);
//						
//						simMatrix[indexA][indexB] += score;
//						simMatrix[indexB][indexA] = simMatrix[indexA][indexB];
//					}
//				});
//			}
//			
//			long stopTime = System.currentTimeMillis();
//			long elapsedTime = (stopTime - startTime)/1000;
//			System.out.println(elapsedTime + "s");
//			
//			saveSimMatrixCache(simMatrix);
//		}
//		
//		System.out.println("Building Rank list...");		
//		try {
//			File file = new File("museums.txt");
//	
//			List<String> lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
//			
//			int[] indexArray = new int[lines.size()];
//			for (int i = 0; i < lines.size(); i++) {
//				String instance = lines.get(i).replace(' ', '_');
//				int index = this.instanceList.indexOf(new Instance("http://dbpedia.org/resource/"+instance));
//				indexArray[i] = index;
//			}
//			
//			List<Rank> rankList = new ArrayList<>();
//			for (int i = 0; i < indexArray.length; i++) {
//				Rank rank = new Rank(this.instanceList.get(indexArray[i]).getShortURI());
//				
//				for (int j = 0; j < indexArray.length; j++) {
//					if(i == j)  continue;
//					
//					rank.addInstanceSim(this.instanceList.get(indexArray[j]).getShortURI(), simMatrix[indexArray[i]][indexArray[j]]);
//				}
//				
//				System.out.println(rank.getInstance());
//				System.out.println("\t"+rank.getUnrankedInstances());
//				System.out.println("\t"+rank.getRankedInstances()+"\n");
//				
//				rankList.add(rank);
//			}
//			
//			saveRank(rankList,"Rank2.ser");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
		
	/**
	 * Compare - Method 3
	 */
	public void compareBasedOnInvertedIndex(){
		double[][] simMatrixCache = getSimMatrixCache("dboMuseumSimMatrix.compare2A.644m.ser");
		double[][] simMatrix;
		if (simMatrixCache != null) {
			simMatrix = simMatrixCache;
		} else {
			int numberInstances = instanceList.size();
			simMatrix = new double[numberInstances][numberInstances];
			
			long startTime = System.currentTimeMillis();
	
			
			for (Term term : termList) {
				
				System.out.println(term);
				List<PredicateTerm> predicateTermList = term.getPredicateList();
				for (PredicateTerm predicateTerm : predicateTermList) {
					
					
					List<Instance> instanceListAux = predicateTerm.getInstanceList();
					
					System.out.println("\t" + predicateTerm + " " + instanceListAux.size());
					
					//IntStream.range(0, instanceListAux.size()).parallel().forEach( i -> {
					for (int i = 0; i < instanceListAux.size(); i++) {
						
						Instance a = instanceListAux.get(i);
						int indexA = this.instanceList.indexOf(a);
	
						for (int j = i+1; j < instanceListAux.size(); j++) {
							Instance b = instanceListAux.get(j);
							int indexB = this.instanceList.indexOf(b);
							
							if (simMatrix[indexA][indexB] != 0) {
								continue;
							}
							System.out.println("Compare " + a + " with " + b);
							
							double score = Compare.compareSoftTFIDF(a, b, predicateList, true);
							
							simMatrix[indexA][indexB] += score;
							simMatrix[indexB][indexA] = simMatrix[indexA][indexB];
						}
					}
					//});	
				}
			}
			
			long stopTime = System.currentTimeMillis();
			long elapsedTime = (stopTime - startTime)/1000/60;
			System.out.println(elapsedTime + "m");
			
			saveSimMatrixCache(simMatrix);
		}
		
		System.out.println("Building Rank list...");		
		try {
			File file = new File("museums.txt");
	
			List<String> lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
			
			int[] indexArray = new int[lines.size()];
			for (int i = 0; i < lines.size(); i++) {
				String instance = lines.get(i).replace(' ', '_');
				int index = this.instanceList.indexOf(new Instance("http://dbpedia.org/resource/"+instance));
				indexArray[i] = index;
			}
			
			List<Rank> rankList = new ArrayList<>();
			for (int i = 0; i < indexArray.length; i++) {
				Rank rank = new Rank(this.instanceList.get(indexArray[i]).getShortURI());
				
				for (int j = 0; j < Rank.qtItensRank; j++) {
					if(i == j)  continue;
					
					rank.addInstanceSim(this.instanceList.get(indexArray[j]).getShortURI(), simMatrix[indexArray[i]][indexArray[j]]);
				}
				
				System.out.println(rank.getInstance());
				System.out.println("\t"+rank.getUnrankedInstances());
				System.out.println("\t"+rank.getRankedInstances()+"\n");
				
				rankList.add(rank);
			}
			
			saveRank(rankList,"Rank1B.ser");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
    /**
     * Gets the set of museums filtered
     * 
     * @return List<Instance>
     */
    public static List<Instance> getSetOfMuseumsFiltered() {
    	
    	List<Instance> list = new ArrayList<>();
    	File file = new File("museums.txt");
		List<String> lines;
		try {
			lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
			for (int i = 0; i < lines.size(); i++) {
				String instance = lines.get(i).replace(' ', '_');
				list.add(new Instance(instance));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return list;
    }

    /**
	 * Get the cache of predicates list
	 * @return boolean True if there is a cache
	 */
	@SuppressWarnings({"unchecked"})
	private boolean getPredicateCache() {
		try {
			FileInputStream fis = new FileInputStream(this.domain.replace(":", "")+"Predicate.original.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.predicateList = (List<Predicate>) ois.readObject();
			ois.close();
			fis.close();
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Save the predicate cache
	 */
	public void savePredicateCache(){
		
		if (!updatePredicateCache) {
			return;
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(this.domain.replace(":", "")+"Predicate.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(predicateList);
			oos.close();
			fos.close();
			System.out.printf("Serialized predicate list data is saved in " + this.domain.replace(":", "")+"Predicate.ser\n");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.updatePredicateCache = false;
	}
		
	/**
	 * Get the cache of discriminability and coverage
	 * @return boolean True if there is a cache
	 */
	@SuppressWarnings({"unchecked"})
	private boolean getInstanceCache() {
		try {
			FileInputStream fis = new FileInputStream(this.domain.replace(":", "")+"Instance.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.instanceList = (List<Instance>) ois.readObject();
			ois.close();
			fis.close();
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Save isntance cache
	 */
	public void saveInstanceCache(){
		
		if (!updateInstanceCache) {
			return;
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(this.domain.replace(":", "")+"Instance.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(instanceList);
			oos.close();
			fos.close();
			System.out.printf("Serialized instance list data is saved in " + this.domain.replace(":", "")+"Instance.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		this.updateInstanceCache = false;
	}
	
	/**
	 * Gets the similarity matrix cache
	 * @return double[][]
	 */
	private double[][] getSimMatrixCache(String name) {
		double[][] simMatrix = null;
		try {
			FileInputStream fis = new FileInputStream(name);
			ObjectInputStream ois = new ObjectInputStream(fis);
			simMatrix = (double[][]) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
		return simMatrix;
	}
	
	/**
	 * Saves the similarity matrix cache
	 * @param simMatrix
	 */
	public void saveSimMatrixCache(double[][] simMatrix){
		try {
			FileOutputStream fos = new FileOutputStream("SimMatrix.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(simMatrix);
			oos.close();
			fos.close();
			System.out.printf("Serialized matrix similarity data is saved in SimMatrix.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Gets the zone index cache
	 * @return
	 */
	@SuppressWarnings({"unchecked"})
	private boolean getZoneIndexCache() {
		try {
			FileInputStream fis = new FileInputStream(this.domain.replace(":", "")+"ZoneIndex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.termList = (List<Term>) ois.readObject();
			ois.close();
			fis.close();
			return true;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Saves the zone index cache
	 */
	public void saveZoneIndexCache(){
		if (!updateZoneIndexCache) {
			return;
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(this.domain.replace(":", "")+"ZoneIndex.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(termList);
			oos.close();
			fos.close();
			System.out.printf("Serialized zone index data is saved in " + this.domain.replace(":", "")+"ZoneIndex.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.updateZoneIndexCache = false;
	}
	
	/**
	 * Gets the similarity matrix cache
	 * @return double[][]
	 */
	@SuppressWarnings({"unchecked"})
	public static List<Rank> getRank(String name) {
		List<Rank> rankList = null;
		try {
			FileInputStream fis = new FileInputStream(name);
			ObjectInputStream ois = new ObjectInputStream(fis);
			rankList = (List<Rank>) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
		}
		return rankList;
	}
	
	/**
	 * Saves the similarity matrix cache
	 * @param simMatrix
	 */
	public void saveRank(List<Rank> rankList, String name){
		try {
			FileOutputStream fos = new FileOutputStream(name);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(rankList);
			oos.close();
			fos.close();
			System.out.printf("Serialized Rank.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
