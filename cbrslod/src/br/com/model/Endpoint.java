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
	public String instanceFilter;
	private int limitEndpoint = 10000;
	
	/**
	 * Instances and predicates
	 */
	private List<Predicate> predicateList = new ArrayList<>();
	private HashSet<Predicate> predicateSet = new HashSet<Predicate>();
	private List<Instance> instanceList = new ArrayList<>();
	private List<Term> termList = new ArrayList<Term>();
	
	/**
	 * Parameters
	 */
	private int numberOfLevels;
	private double thresholdCoverage;
	private double thresholdDiscriminability;
		
	private boolean updatePredicateCache = false;
	private boolean updateInstanceCache = false;
	private boolean updateZoneIndexCache = false;
	
	/**
	 * Constructor
	 * 
	 * @param domain Domain of the instances
	 */
    public Endpoint(String endPoint, String graph, String domain, String instanceFilter, int numberOfLevels, double thresholdCoverage, double thresholdDiscriminability) {
		super();
		
		/**
		 * Parameters
		 */
		this.endPoint = endPoint;
		this.graph = graph;
		this.domain = domain;
		this.instanceFilter = instanceFilter;
		this.numberOfLevels = numberOfLevels;
		this.thresholdCoverage = thresholdCoverage;
		this.thresholdDiscriminability = thresholdDiscriminability;
		
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
			this.updatePredicateCache = true;
			this.savePredicateCache();
		}
	}

    public void getSetOfInstances() {
    	getSetOfInstances(0);
    }
    
	/**
     * Gets the set of instances
     * 
     * @param page Page number, this parameter is important because dbpedia does not allow retrieving more than 10000 rows in a search   
     * @return List<Instance> List of instances
     */
    public void getSetOfInstances(int page) {
    	
		// Query
		String szQuery = null;
		int limit = limitEndpoint;
		int offset = page * limit;
		
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " PREFIX : <http://dbpedia.org/resource/> "
				+ " SELECT ?instance "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { "
				+ " 	?instance a "+ this.domain +" ; "
				+ (this.instanceFilter == null ? null : " 	FILTER(?instance IN ("+ this.instanceFilter +")) ")
				+ " } LIMIT " + limit + " OFFSET " + offset ;

		// Query Endpoint
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			
			if (results.hasNext()) {
				int iCount = (page*limit) + 0;
				while (results.hasNext()) {
					QuerySolution qs = results.nextSolution();
					Resource instance = qs.getResource("instance");
					instanceList.add(new Instance(instance));
					
					// Count
					iCount++;
					System.out.print("Result " + iCount + ": ");
					System.out.println("[ Instance ]: " + instance.toString());
				}
				this.close();
				this.getSetOfInstances(++page);
			} else {
				this.close();
			}
			
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}  
  
	/**
	 * Get the set of predicates based on level
	 * 
	 * @param level
	 */
	public void getSetOfPredicates(int level) {
		String unions = this.buildUnionsQuery(level);
		String szQuery = null;
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " PREFIX : <http://dbpedia.org/resource/> "
				+ " SELECT DISTINCT ?p" + level + " "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE "
				+ " { "
				+ unions
				+ (this.instanceFilter == null ? "" : " 	FILTER(?i IN ("+ this.instanceFilter +")) ")
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
					predicateList.add(new Predicate(predicate.toString(), level));

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
			levels.add("?i a "+this.domain+" . "
					+ "?i ?p1 ?leaf . ");
			levels.add("?i a "+this.domain+" . "
					+ "?leaf ?p1 ?i . ");	
		} else {
			levels.add("?i a "+this.domain+" . "
					+ "?i ?p1 ?o1 . ");
			levels.add("?i a "+this.domain+" . "
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
    	
    	
    	for (Instance instance : instanceList) {
			
		
    	//instanceList.stream().parallel().forEach( instance -> {
    		if (instance.getInstanceNeighborhoodList().size() > 0) {
				continue;
    			//return;
			}
    		this.updateInstanceCache = true;
    		
	    	instance.getInstanceNeighborhoodList().clear();
	    	System.out.println(instance.toString());
			this.getInstanceNeighborhood(instance.getURI(), null, null, instance.getInstanceNeighborhoodList(), new ArrayList<>(), 1);
				
			System.out.println();
			for (InstanceNeighborhood instanceNeighborhood : instance.getInstanceNeighborhoodList()) {
				for (Node node : instanceNeighborhood.getNeighborhood()) {
					System.out.print(" \t" + node.getNode().substring(0, (node.getNode().length() > 70 ? 70: node.getNode().length())));
				}
				System.out.println();
			}
			System.out.print(instance + " - " +instance.getInstanceNeighborhoodList().size()+"\n");	
		//});
    	}
    	
    	//Updates the cache of Instances
    	saveInstanceCache();
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
    public void getInstanceNeighborhood(String resource, String previousResource, Resource previousPredicate, List<InstanceNeighborhood> instanceNeighborhoodList, List<Node> previousNeighborhood, int level){
    	List<QuerySolution> qsList = this.queryInstanceNeighborhood(resource, previousResource, previousPredicate,0);
    
    	List<Node> neighborhood;

    	if (level == 1) {
			System.out.print(qsList.size());
		}
    	
    	for (QuerySolution qs : qsList) {
			neighborhood = new ArrayList<>(previousNeighborhood);	
			
			if (level == 1) {
				System.out.print(" " + qsList.indexOf(qs));
			}
			
			Resource predicate = qs.getResource("predicate");
			RDFNode object = qs.get("object");
			
			//Check if the predicate is present on predicates list
			if (!predicateSet.contains(new Predicate(predicate.toString(), level))) {
				continue;
			} 
			
			/**
			 * Clean literal values
			 */
			String node = null;
			if (object instanceof Literal) {
				node = object.asLiteral().getLexicalForm();
				node = Util.removeStopWords(node);
				node = Util.removePunctuation(node);
				node = Util.stemming(node);
			} else {
				node = object.toString();
			}
			
			
			neighborhood.add(new Node(predicate.getURI(), predicate.getClass().getSimpleName())); 
			neighborhood.add(new Node(node, object.getClass().getSimpleName()));
			
			if (object instanceof Literal || level == this.numberOfLevels || predicate.toString().equals("http://dbpedia.org/ontology/location")) {
				instanceNeighborhoodList.add(new InstanceNeighborhood(neighborhood));
			} else			
			if (level < this.numberOfLevels && !object.toString().equals("http://www.w3.org/2002/07/owl#Thing")) {
				this.getInstanceNeighborhood(object.toString(), resource, predicate, instanceNeighborhoodList, neighborhood, level+1);
			}
		}
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
				+ " FILTER (!regex(?object, \"http://www.wikidata.org/entity\",\"i\")) . "
				//+ " FILTER(regex(?predicate, \"^http://dbpedia.org/ontology/\") || (?predicate = \"http://purl.org/dc/terms/subject\")) "
				+ " FILTER(str(?predicate) != \"http://dbpedia.org/ontology/wikiPageWikiLink\" ) "
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
		}

		return querySolutionList;
    }
	
	/**
	 * Count all instances in domain
	 * 
	 * @return Integer Number of instances
	 */
	private int countInstance() {

		// Query
		String szQuery = null;
		
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " PREFIX : <http://dbpedia.org/resource/> "
				+ " SELECT (COUNT(?i)  as ?qt)"
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { "
				+ " 	?i a "+ this.domain +" ; "
				+ (this.instanceFilter == null ? "" : " 	FILTER(?i IN ("+ this.instanceFilter +")) ")
				+ " } ";

		int qt = 0;
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			if (results.hasNext()) {
				QuerySolution qsol = results.nextSolution();
				Literal qtNode = qsol.getLiteral("qt");
				qt = Integer.parseInt(qtNode.getString());
			}
			//this.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return qt;
	}
	
	/**
	 * Count triples with this predicate
	 * 
	 * @param predicate The predicate
	 * @param distinct If will be distinct or not
	 * @return double Number of triples
	 */
	private double countTripesWithPredicate(Predicate predicate, boolean distinct) {

		// Query
		String szQuery = null;
		
		int level = predicate.getLevel();
		String unions = this.buildUnionsQuery(level);
		unions = unions.replace("?p"+level, "<"+predicate.getURI()+">");
				
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " PREFIX : <http://dbpedia.org/resource/> "
				+ " SELECT (COUNT(" + (distinct ? "DISTINCT" : "") + " ?leaf) as ?qt) "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { "
				+ unions
				+ (this.instanceFilter == null ? "" : " 	FILTER(?i IN ("+ this.instanceFilter +")) ")
				+ " }";

		double qt = 0;
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			if (results.hasNext()) {
				QuerySolution qsol = results.nextSolution();
				Literal qtNode = qsol.getLiteral("qt");
				qt = Double.parseDouble(qtNode.getString());
			}
			//this.close();
		} catch (Exception e) {
			System.out.println("Exception: " + predicate);
			e.printStackTrace();
		}

		return qt;
	}
	
	/**
	 * Gets only instances with this predicate
	 * 
	 * @param predicate The predicate
	 * @param page Number of the page
	 * @return List<Instance> Instances
	 */
	private List<Instance> getInstancesWithPredicate(Predicate predicate, int page) {

		// Query
		String szQuery = null;
		
		int limit = 10000;
		int offset = page * limit;
		
		int level = predicate.getLevel();
		String unions = this.buildUnionsQuery(level);
		unions = unions.replace("?p"+level, "<"+predicate.getURI()+">");
		
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " PREFIX : <http://dbpedia.org/resource/> "
				+ " SELECT DISTINCT ?i "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { "
				+ unions
				+ (this.instanceFilter == null ? "" : " 	FILTER(?i IN ("+ this.instanceFilter +")) ")
				+ " } LIMIT " + limit + " OFFSET " + offset ;

		List<Instance> instanceWithPredicate = new ArrayList<>();
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			
			if (results.hasNext()) {
				while (results.hasNext()) {
					QuerySolution qs = results.nextSolution();
					Resource instance = qs.getResource("i");
					instanceWithPredicate.add(new Instance(instance));
				}	
				//this.close();
				instanceWithPredicate.addAll(this.getInstancesWithPredicate(predicate, ++page));
			} else {
				//this.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		
		int qtInstances = this.countInstance();
//		Double maxDiscrinability = 0.0;
//		Double maxPredicateFrequency = 0.0;
		
		for (Predicate predicate : predicateList) {
//		predicateList.stream().parallel().forEach(	predicate -> {
			if (predicate.isCalcStats()) {
				continue;
//				return;
			}
			System.out.print("Starting - " + predicate.getURI() + ". Level - " + predicate.getLevel());
			this.updatePredicateCache = true;
			
	       	/**
	    	 * Calculates the discriminability
	    	 */
			double qtWithPredicate = this.countTripesWithPredicate(predicate, false);
			double qtDistinctWithPredicate = this.countTripesWithPredicate(predicate, true);
			double discriminability = qtDistinctWithPredicate / qtWithPredicate;
			predicate.setDiscriminability(discriminability);
//			if (discriminability > maxDiscrinability) {
//				maxDiscrinability = discriminability;
//			}
			
	    	/**
	    	 * Calculates the predicate frequency
	    	 */
			List<Instance> instanceWithPredicate = this.getInstancesWithPredicate(predicate,0);
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
				Instance instance = instanceList.get(index);
				
				predicate.getTFInstances().put(instance, 0.0);
			}
			
			predicate.setCalcStats(true);
			
			System.out.println(" --> " + predicate);
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
			.filter(x -> x.getPredicateFrequency() >= this.thresholdCoverage && x.getDiscriminability() <= this.thresholdDiscriminability)
			.collect(Collectors.toList());
		
	    Comparator<Predicate> comparator = Comparator.comparingInt(Predicate::getLevel).reversed();
	    comparator = comparator.thenComparingDouble(Predicate::getIdf).reversed();

		//Sort by level and discriminability
		Collections.sort(predicateList, comparator/*new Comparator<Predicate>() {
			public int compare(Predicate a, Predicate b) {
				int la = a.getLevel();
				int lb = b.getLevel();
				Double da = a.getIdf();
				Double db = b.getIdf();
				int level = lb-la;
				double diff = db.doubleValue()-da.doubleValue();
				return diff > 0 ? +1 : (diff < 0 ? -1 : 0);
			}
		}*/);
		
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
	 * Compare - Method 1
	 */
	public void compare1A(){
		
		double[][] simMatrixCache = getSimMatrixCache(this.domain.replace(":", "")+"SimMatrix.ser");
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
				
				IntStream.range(0, instanceListAux.size()).parallel().forEach( i -> {
					Instance a = instanceListAux.get(i);
					int indexA = this.instanceList.indexOf(a);
					
					for (int j = i+1; j < instanceListAux.size(); j++) {
						Instance b = instanceListAux.get(j);
						int indexB = this.instanceList.indexOf(b);
						
						if (simMatrix[indexA][indexB] != 0) {
							continue;
						}
						double score = Compare.compare1A(a, b, predicateList);
						
						simMatrix[indexA][indexB] += score;
						simMatrix[indexB][indexA] = simMatrix[indexA][indexB];
					}
				});
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
	public void compare1B(){
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
							
							double score = Compare.compare1B(a, b, predicateList);
							
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
				
				for (int j = 0; j < indexArray.length; j++) {
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
			FileInputStream fis = new FileInputStream(this.domain.replace(":", "")+"Predicate.ser");
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
			System.out.printf("Serialized predicate list data is saved in " + this.domain.replace(":", "")+"Predicate.ser");
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
			FileOutputStream fos = new FileOutputStream(this.domain.replace(":", "")+"SimMatrix.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(simMatrix);
			oos.close();
			fos.close();
			System.out.printf("Serialized matrix similarity data is saved in " + this.domain.replace(":", "")+"SimMatrix.ser");
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
