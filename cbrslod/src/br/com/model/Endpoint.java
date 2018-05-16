package br.com.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import br.com.controller.Compare;
import br.com.controller.Instance;
import br.com.controller.InstanceNeighborhood;
import br.com.controller.Node;
import br.com.controller.Predicate;
import br.com.controller.Term;
import br.com.controller.TermZone;
import br.com.controller.Teste;
import br.com.tools.Util;

public class Endpoint extends EndpointInterface{
	/**
	 * endPoint address and domain
	 */
	private String endPoint;
	private String domain;
	
	/**
	 * Instances and predicates
	 */
	private List<Predicate> predicateList = new ArrayList<>();
	private HashSet<String> predicateSet = new HashSet<String>();
	private List<Instance> instanceList = new ArrayList<>();
	private HashSet<Term> termList = new HashSet<Term>();
	
	/**
	 * Parameters
	 */
	private int numberOfLevels;
	private double thresholdCoverage;
	private double thresholdDiscriminability;
	private int limitEndpoint = 10000;
	
	private boolean updatePredicateCache = false;
	private boolean updateInstanceCache = false;
	private boolean updateZoneIndexCache = false;
	
	/**
	 * Constructor
	 * 
	 * @param domain Domain of the instances
	 */
    public Endpoint(String domain, String endPoint) {
		super();
		this.domain = domain;
		this.endPoint = endPoint;
		
		//Make a cache of instances
		if (!getInstanceCache()) {
			this.getSetOfInstances(0);
			this.updateInstanceCache = true;
			this.saveInstanceCache();
		}
		
		//Make a cache of predicates
		if (!getPredicateCache()) {
			this.getSetOfPredicates(0);
			this.updatePredicateCache = true;
			this.savePredicateCache();
		}
		
		/**
		 * Parameters
		 */
		this.numberOfLevels = 1;
		this.thresholdCoverage = 0.01;
		this.thresholdDiscriminability = 1;
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
				+ " SELECT ?instance "
				+ " WHERE { "
				+ "    ?instance a "+ this.domain +" ; "
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
				this.getSetOfInstances(++page);
			} 
			
		} catch (Exception ex) {
			System.err.println(ex);
		}
	}    
    
	/**
	 * Get all predicates inside the RDF graph by the domain (Books, movies, museums, etc)
	 * 
	 * @param page Get the page base on limit and offset
	 * @return ResultSet
	 */
	public void getSetOfPredicates(int page) {
		
		// Query
		String szQuery = null;
		int limit = limitEndpoint;
		int offset = page * limit;
		
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " SELECT distinct ?predicate "
				+ " WHERE { "
				+ "  { "
				+ "    ?instance a "+ this.domain +" . "
				+ "    ?instance ?predicate ?o1 . "
				+ "  } "
				+ "  UNION "
				+ "  { "
				+ "    ?instance a "+ this.domain +" . "
				+ "    ?s1 ?predicate ?instance . "
				+ "  } "
				//+ " FILTER(regex(?predicate, \"^http://dbpedia.org/ontology/\")  || (?predicate = \"http://purl.org/dc/terms/subject\")) "
				+ " } LIMIT " + limit + " OFFSET " + offset ;
		
		// Query Endpoint
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			if (results.hasNext()) {
				int iCount = (page*limit) + 0;
				while (results.hasNext()) {
					// Get Result
					QuerySolution qs = results.next();
					Resource predicate = qs.getResource("predicate");
					
					predicateList.add(new Predicate(predicate.toString()));
					
					// Count
					iCount++;
					System.out.print("Result " + iCount + ": ");
					System.out.println("[ Predicate ]: " + predicate);
				}
				getSetOfPredicates(++page);
			}
		} catch (Exception ex) {
			System.err.println(ex);
		}
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
    	
    	instanceList.stream().parallel().forEach( instance -> {
    		if (instance.getInstanceNeighborhoodList().size() > 0) {
				return;
			}
    		this.updateInstanceCache = true;
    		
	    	instance.getInstanceNeighborhoodList().clear();
	    	System.out.println(instance.toString());
			this.getInstanceNeighborhood(instance.getURI(), null, null, instance.getInstanceNeighborhoodList(), new ArrayList<>(), 1);
			
//				for (InstanceNeighborhood instanceNeighborhood : instance.getInstanceNeighborhoodList()) {
//					for (Node node : instanceNeighborhood.getNeighborhood()) {
//						System.out.print(" \t" + node.getNode().substring(0, (node.getNode().length() > 70 ? 70: node.getNode().length())));
//					}
//					System.out.println();
//				}
			//System.out.print(" - " +instance.getInstanceNeighborhoodList().size()+"\n");	
		});
    	
    	//Updates the cache of Instances
    	saveInstanceCache();
    }
    
    /**
     * Gets the neighborhood until the max level
     * 
     * @param resource Resource to find neighborhood
     * @param previousResource Resource used in the previous level
     * @param instanceNeighborhoodList List of neigborhoods for the instance(Resource)
     * @param previousNeighborhoodList List of neigborhoods for the instance(Resource) in the previous level
     * @param level Number of the level
     */
    public void getInstanceNeighborhood(String resource, String previousResource, Resource previousPredicate, List<InstanceNeighborhood> instanceNeighborhoodList, List<Node> previousNeighborhood, int level){
    	List<QuerySolution> qsList = this.queryInstanceNeighborhood(resource, previousResource, previousPredicate,0);
    
    	List<Node> neighborhood;

    	for (QuerySolution qs : qsList) {
			neighborhood = new ArrayList<>(previousNeighborhood);	
			
			Resource predicate = qs.getResource("predicate");
			RDFNode object = qs.get("object");
			
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
			
			if (level == 1 && !predicateSet.contains(predicate.toString())) {
				continue;
			} 

			neighborhood.add(new Node(predicate.getURI(), predicate.getClass().getName())); 
			neighborhood.add(new Node(node, object.getClass().getName()));
			
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
     * @return List<QuerySolution> List of neighborhood for the instance
     */
    public List<QuerySolution> queryInstanceNeighborhood(String resource, String previousResource, Resource previousPredicate, int page){

		// Query
		String szQuery = null;
		int limit = limitEndpoint;
		int offset = page * limit;
		
		// Query
		szQuery = " SELECT ?predicate ?object "
				+ " WHERE { " 
				+ " { <" + resource + "> ?predicate ?object . } "
				+ " UNION " 
				+ " { "
					+ "?object ?predicate <" + resource + "> . "
					+ (previousPredicate == null ? "" : " FILTER(str(?predicate) != \""+ previousPredicate.toString() +"\") ")
				+ " }  "
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
				querySolutionList.addAll(this.queryInstanceNeighborhood(resource, previousResource, previousPredicate, ++page));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return querySolutionList;
    }
	
	/**
	 * Count triples with this predicate
	 * 
	 * @param predicate The predicate
	 * @param distinct If will be distinct or not
	 * @return double Number of triples
	 */
	private double countTripesWithPredicate(String predicate, boolean distinct) {

		// Query
		String szQuery = null;
		
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " SELECT (COUNT(" + (distinct ? "DISTINCT" : "") + " ?o1) as ?qt) "
				+ " WHERE { "
				+ "   { "
				+ "     ?instance a "+ this.domain +" . "
				+ "     ?instance <" + predicate + "> ?o1 . "
				+ "   } "
				+ "   UNION "
				+ "   { "
				+ "     ?instance a "+ this.domain +" . "
				+ "     ?o1 <" + predicate + "> ?instance . "
				+ "   } "
				+ " }";

		double qt = 0;
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			if (results.hasNext()) {
				QuerySolution qsol = results.nextSolution();
				Literal qtNode = qsol.getLiteral("qt");
				qt = Double.parseDouble(qtNode.getString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return qt;
	}
	
	/**
	 * Gets only instances with this predicate
	 * 
	 * @param predicate The predicate
	 * @return List<Instance> Instances
	 */
	private List<Instance> getInstancesWithPredicate(String predicate, int page) {

		// Query
		String szQuery = null;
		
		int limit = 10000;
		int offset = page * limit;
		
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " SELECT distinct ?instance "
				+ " WHERE { "
				+ "   { "
				+ "     ?instance a "+ this.domain +" . "
				+ "     ?instance <" + predicate + "> ?ro1 . "
				+ "   } "
				+ "   UNION "
				+ "   { "
				+ "     ?instance a "+ this.domain +" . "
				+ "     ?lo1 <" + predicate + "> ?instance . "
				+ "   } "
				+ " } LIMIT " + limit + " OFFSET " + offset ;

		List<Instance> instanceWithPredicate = new ArrayList<>();
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			
			if (results.hasNext()) {
				while (results.hasNext()) {
					QuerySolution qs = results.nextSolution();
					Resource instance = qs.getResource("instance");
					instanceWithPredicate.add(new Instance(instance));
				}	
				instanceWithPredicate.addAll(this.getInstancesWithPredicate(predicate, ++page));
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

		predicateList.stream().parallel().forEach( predicate -> {
			
			if (predicate.isCalcTermFreq()) {
				return;
			}
			this.updatePredicateCache = true;
			
			/**
			 * Calculates the Term Frequency in documents
			 */
			double qtPredicate = 0.0;
			double qtPredicates = 0.0;
			
			List<Instance> isntanceURIList = new ArrayList<Instance>(predicate.getTFInstances().keySet());
			for (Instance instance : isntanceURIList) {
				qtPredicate = instance.countPredicateOnNeighborhoodList(predicate.getURI());
				qtPredicates = instance.getInstanceNeighborhoodList().size();
				predicate.getTFInstances().put(instance, qtPredicate/qtPredicates);
			}
			
			predicate.setCalcTermFreq(true);
		});
		
		//Updates the cache of Instances
		savePredicateCache();
	}
	
	/**
	 * Calculates the predicates stats.
	 * 
	 * @return List<Predicate> The predicates list
	 */
	public List<Predicate> calcPredicateStats() {
		
		int qtInstances = this.instanceList.size();
//		Double maxDiscrinability = 0.0;
//		Double maxPredicateFrequency = 0.0;
		
		predicateList.stream().parallel().forEach(	predicate -> {

			if (predicate.isCalcStats()) {
				return;
			} 
			this.updatePredicateCache = true;
			
	       	/**
	    	 * Calculates the discriminability
	    	 */
			double qtDistinctWithPredicate = this.countTripesWithPredicate(predicate.getURI(), true);
			double qtWithPredicate = this.countTripesWithPredicate(predicate.getURI(), false);
			double discriminability = qtDistinctWithPredicate / qtWithPredicate;
			predicate.setDiscriminability(discriminability);
//			if (discriminability > maxDiscrinability) {
//				maxDiscrinability = discriminability;
//			}
			
	    	/**
	    	 * Calculates the predicate frequency
	    	 */
			List<Instance> instanceWithPredicate = this.getInstancesWithPredicate(predicate.getURI(),0);
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
			
			System.out.println(predicate);
		});

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
		
		//Sort by discriminability
		Collections.sort(predicateList, new Comparator<Predicate>() {
			public int compare(Predicate a, Predicate b) {
				Double da = a.getIdf();
				Double db = b.getIdf();
				double diff = db.doubleValue()-da.doubleValue();
				return diff>0 ? +1 : (diff<0? -1 : 0);
			}
		});
		
		for (Predicate predicate : predicateList) {
			//System.out.println((++i) + " \t| " + predicate.toString());
			predicateSet.add(predicate.getURI());
		}
		
		return predicateList;
	}
	
	
	
	
	public void calcZoneIndex(){
		
		if (getZoneIndexCache()) {
			return;
		}
		
		for (Instance instance : instanceList) {
			List<InstanceNeighborhood> lN = instance.getInstanceNeighborhoodList();
			
			for (InstanceNeighborhood n : lN) {
				Node lastElement = n.getLastElement();
				Node lastPredicate = n.getLastPredicate();
				
				int index = predicateList.indexOf(new Predicate(lastPredicate.getNode()));
				Predicate predicate = predicateList.get(index);
				
				if (lastElement.getTypeOfNode().equals("org.apache.jena.rdf.model.impl.LiteralImpl")) {
					String[] pieces = Util.split(lastElement.getNode(), " "); 
					
					for (String piece : pieces) {
						
						Term term = new Term(piece);
						term.getInvertedIndex().add(new TermZone(instance, predicate));
						
						termList.add(term);
					}
					
				} else {
					String piece = lastElement.getNode();
					Term term = new Term(piece);
					term.getInvertedIndex().add(new TermZone(instance, predicate));
					
					termList.add(term);
				}
			}
		}
		
		this.updateZoneIndexCache = true;
		saveZoneIndexCache();
	}
	
	public void compareInstances(){
		
		int numberInstances = instanceList.size();
		double[][] simMatrix = new double[numberInstances][numberInstances];
		
		//saveSimMatrixCache(simMatrix);
//		IntStream.range(0, numberInstances).parallel().forEach( i -> {
//			for (int j = i; j < numberInstances; j++) {
//				if (i == j) {
//					simMatrix[i][j] = 1.0;
//					continue;
//				}
//				simMatrix[i][j] = 0.0;
//				simMatrix[j][i] = 0.0;
//			}
//		});		
//		double teste = 0.30;
//		predicateList = predicateList.stream()
//				.filter(x -> x.getCoverage() <= teste)
//				.collect(Collectors.toList());
		
		//predicateList.stream().parallel().forEach(predicate -> {
		for (Predicate predicate : predicateList) {
			System.out.println(predicate.getURI());
			List<Instance> instanceListAux = new ArrayList<Instance>(predicate.getTFInstances().keySet());
			
			IntStream.range(0, instanceListAux.size()).parallel().forEach( i -> {
			
			//for (int i = 0; i < instanceListAux.size(); i++) {
				
				Instance a = instanceListAux.get(i);
				int indexA = this.instanceList.indexOf(a);
				
				for (int j = i+1; j < instanceListAux.size(); j++) {
					Instance b = instanceListAux.get(j);
					int indexB = this.instanceList.indexOf(b);
					
					double tFIDFA = predicate.getTFIDF(a);
					double tFIDFB = predicate.getTFIDF(b);
					double average = (tFIDFA+tFIDFB)/2;
					double score = average*Compare.compareByPredicate(a, b, predicate);
					
					simMatrix[indexA][indexB] += score;
					simMatrix[indexB][indexA] += simMatrix[indexA][indexB];
				}
			//}
			});
		}
		//});
		
//		for (Predicate predicate : predicateList) {
//			System.out.println(predicate.getURI());
//			List<Instance> instanceListAux = new ArrayList<Instance>(predicate.getTFInstances().keySet());
//			for (int i = 0; i < instanceListAux.size(); i++) {
//				
//				Instance a = instanceListAux.get(i);
//				int indexA = this.instanceList.indexOf(a);
//				
//				for (int j = i+1; j < instanceListAux.size(); j++) {
//					Instance b = instanceListAux.get(j);
//					int indexB = this.instanceList.indexOf(b);
//					
//					double tFIDFA = predicate.getTFIDF(a);
//					double tFIDFB = predicate.getTFIDF(b);
//					double average = (tFIDFA+tFIDFB)/2;
//					double score = average*Compare.compareByPredicate(a, b, predicate);
//					
//					simMatrix[indexA][indexB] += score;
//					simMatrix[indexB][indexA] += simMatrix[indexA][indexB];
//				}
//			}
//		}
		
		
		
//		IntStream.range(0, numberInstances).parallel().forEach( i -> {
//			for (int j = i; j < numberInstances; j++) {
//				if (i == j) {
//					simMatrix[i][j] = 1.0;
//					continue;
//				}
//				System.out.println("Compare  \t"+ instanceList.get(i) + " \t" + instanceList.get(j));
//				
//				simMatrix[i][j] = Compare.compare(instanceList.get(i), instanceList.get(j), this.predicateList);
//				simMatrix[j][i] = simMatrix[i][j];
//			}
//		});
//		
//		for (int i = 0; i < numberInstances || i < numberInstances; i++) {
//			System.out.println();
//			for (int j = 0; j < numberInstances; j++) {
//				System.out.print(String.format("%.3f", Utils.round(simMatrix[i][j], 3))  + "  ");
//			}
//		}
//		
		saveSimMatrixCache(simMatrix);
		List<Teste> list = new ArrayList<>();
		for (int j = 0; j < simMatrix[1779].length; j++) {
			list.add(new Teste(instanceList.get(j), simMatrix[1779][j]));
		}
		
		System.out.println();
		Collections.sort(list, new Comparator<Teste>() {
			public int compare(Teste a, Teste b) {
				Double da = a.getScore();
				Double db = b.getScore();
				double diff = db.doubleValue()-da.doubleValue();
				return diff>0 ? +1 : (diff<0? -1 : 0);
			}
		});
		System.out.println(list);
//		
//		System.out.println();
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
	
	@SuppressWarnings({"unchecked"})
	private double[][] getSimMatrixCache() {
		double[][] simMatrix = null;
		try {
			FileInputStream fis = new FileInputStream(this.domain.replace(":", "")+"Instance.ser");
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
	
	public void saveSimMatrixCache(double[][] simMatrix){
		try {
			FileOutputStream fos = new FileOutputStream(this.domain.replace(":", "")+"SimMatrix.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(simMatrix);
			oos.close();
			fos.close();
			System.out.printf("Serialized instance list data is saved in " + this.domain.replace(":", "")+"SimMatrix.ser");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	@SuppressWarnings({"unchecked"})
	private boolean getZoneIndexCache() {
		try {
			FileInputStream fis = new FileInputStream(this.domain.replace(":", "")+"ZoneIndex.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.termList = (HashSet<Term>) ois.readObject();
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
}
