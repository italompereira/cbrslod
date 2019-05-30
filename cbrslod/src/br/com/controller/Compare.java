package br.com.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wcohen.ss.JaroWinklerTFIDF;
import com.wcohen.ss.Levenstein;
import com.wcohen.ss.SoftTFIDF;

import br.com.model.Instance;
import br.com.model.InstanceNeighborhood;
import br.com.model.Node;
import br.com.model.Predicate;
import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.Levenshtein;
import info.debatty.java.stringsimilarity.WeightedLevenshtein;

public class Compare {
	
	/**
	 * Compare - Method 1 A(Cosine)
	 *
	 * @param a Instance a
	 * @param b Instance b
	 * @param predicateList Predicate List
	 * @return double Score of similarity
	 */
	public static double compareCosine(Instance a, Instance b, List<Predicate> predicateList, boolean jaccard){
		
		//score between two instances
		double sim = 0.0;
		
		for (InstanceNeighborhood inA : a.getInstanceNeighborhoodList()) {
			//score instanceNeighborhood
			double score = 0.0;
			
			//Get comparable neighborhoods
			List<InstanceNeighborhood> instanceNeighborhoodListAux = getComparableInstanceNeighborhoodList(inA, b);
			if (instanceNeighborhoodListAux.size() == 0) {
				continue;
			}
			
			Node lastElementA = inA.getLastElement();
			//System.out.println("");
			if (lastElementA.getTypeOfNode().equals("LiteralImpl")) {
				
				for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodListAux) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					
					if (!shareAToken(lastElementA.getNode(), lastElementB.getNode())) {
						continue;
					}
					
//					String lastElementAS = lastElementA.toString().replaceFirst(".*/([^/?]+).*", "$1");
//					String lastElementBS = lastElementB.toString().replaceFirst(".*/([^/?]+).*", "$1");
					
					score += cosine(lastElementA.toString(), lastElementB.toString());
				}
				
			} else {
				
				for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodListAux) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					
					String lastElementAS = lastElementA.toString().replaceFirst(".*/([^/?]+).*", "$1").replace("Category:", "");
					String lastElementBS = lastElementB.toString().replaceFirst(".*/([^/?]+).*", "$1").replace("Category:", "");
					
//					double maxSize = lastElementAS.length()>lastElementBS.length()?lastElementAS.length():lastElementBS.length();
//					score += (1.0 + Compare.levenshtein(lastElementAS,lastElementBS)/maxSize);
					
					//if (lastElementA.getNode().equals(lastElementB.getNode())) {
					if (Compare.levenshtein(lastElementBS,lastElementAS) >= -3) {
						score += 1.0;
					}
				}
			}
			
			//Apply the tf-idf weight to adjust the score
			int index = predicateList.indexOf(new Predicate(inA.getLastPredicate().toString(), inA.getNeighborhood().size()/2));
			
			double tFIDFA = predicateList.get(index).getTFIDF(a);
			double tFIDFB = predicateList.get(index).getTFIDF(b);
			
			sim += ((tFIDFA+tFIDFB)/2) * score;
//			sim += score;
			
			
		}
		
		if (jaccard) {
			sim = sim/(a.getInstanceNeighborhoodList().size() + b.getInstanceNeighborhoodList().size() - sim);
			//System.out.println("Score [" + sim + "] - Jaccard [" + jaccard + "]");
		}
		
		return sim;
	}
	
	/**
	 * Compare - Method 1 B(SoftTFIDF)
	 *
	 * @param a Instance a
	 * @param b Instance b
	 * @param predicateList Predicate List
	 * @return double Score of similarity
	 */
	public static double compareSoftTFIDF(Instance a, Instance b, List<Predicate> predicateList, boolean jaccard){
		
		//score between two instances
		double sim = 0.0;
		
		for (InstanceNeighborhood inA : a.getInstanceNeighborhoodList()) {
			//score instanceNeighborhood
			double score = 0.0;
			
			//Get comparable neighborhoods
			List<InstanceNeighborhood> instanceNeighborhoodListAux = getComparableInstanceNeighborhoodList(inA, b);
			if (instanceNeighborhoodListAux.size() == 0) {
				continue;
			}
			
			Node lastElementA = inA.getLastElement();
			if (lastElementA.getTypeOfNode().equals("LiteralImpl")) {
				
				for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodListAux) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					
					if (!shareAToken(lastElementA.getNode(), lastElementB.getNode())) {
						continue;
					}
					
//					String lastElementAS = lastElementA.toString().replaceFirst(".*/([^/?]+).*", "$1");
//					String lastElementBS = lastElementB.toString().replaceFirst(".*/([^/?]+).*", "$1");
					
					score += softTFIDF(lastElementA.toString(), lastElementB.toString());
				}
				
			} else {
				
				for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodListAux) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					
					String lastElementAS = lastElementA.toString().replaceFirst(".*/([^/?]+).*", "$1").replace("Category:", "");
					String lastElementBS = lastElementB.toString().replaceFirst(".*/([^/?]+).*", "$1").replace("Category:", "");
					
//					double maxSize = lastElementAS.length()>lastElementBS.length()?lastElementAS.length():lastElementBS.length();
//					score += (1.0 + Compare.levenshtein(lastElementAS,lastElementBS)/maxSize);
					
					//if (lastElementA.getNode().equals(lastElementB.getNode())) {
					if (Compare.levenshtein(lastElementAS,lastElementBS) >= -3) {
						score += 1.0;
					}
				}
			}
			
			if (score == 0.0) {
				continue;
			}
			
			//Apply the tf-idf weight to adjust the score
			int index = predicateList.indexOf(new Predicate(inA.getLastPredicate().toString(), inA.getNeighborhood().size()/2));
			
			double tFIDFA = predicateList.get(index).getTFIDF(a);
			double tFIDFB = predicateList.get(index).getTFIDF(b);
			
			sim += ((tFIDFA+tFIDFB)/2) * score;
//			sim += score;
		}
		
		if (jaccard) {
			sim = sim/(a.getInstanceNeighborhoodList().size() + b.getInstanceNeighborhoodList().size() - sim);
			//System.out.println("Score [" + sim + "] - Jaccard [" + jaccard + "]");
		}
		
		return sim;
	}
	
	/**
	 * Compare - Method 1 B(SoftTFIDF)
	 *
	 * @param a Instance a
	 * @param b Instance b
	 * @param predicateList Predicate List
	 * @return double Score of similarity
	 */
	public static double compareByTextCosine(Instance a, Instance b){
		double sim = 0.0;
		
		StringBuilder nAS = new StringBuilder();
		StringBuilder nBS = new StringBuilder();
		
		for (InstanceNeighborhood inA : a.getInstanceNeighborhoodList()) {
			Node lastElementA = inA.getLastElement();
			if (lastElementA.getTypeOfNode().equals("LiteralImpl")) {
				nAS.append(inA.getLastElement()+ " ");	
			} else {
				nAS.append(lastElementA.toString().replaceFirst(".*/([^/?]+).*", "$1").replace("Category:", "")/*.replace("_", " ")*/+ " ");
				//nAS.append(lastElementA.toString().replaceFirst(".*/([^/?]+).*", "$1") + " ");
			}
			
			//nAS.append(inA.getLastElement()+ " ");
			
//			for (Node nA : inA.getNeighborhood()) {
//				nAS.append(nA.toString() + " ");
//			}
		}
		
		for (InstanceNeighborhood inB : b.getInstanceNeighborhoodList()) {
			
			Node lastElementB = inB.getLastElement();
			if (lastElementB.getTypeOfNode().equals("LiteralImpl")) {
				nBS.append(inB.getLastElement()+ " ");	
			} else {
				nBS.append(lastElementB.toString().replaceFirst(".*/([^/?]+).*", "$1").replace("Category:", "")/*.replace("_", " ")*/+ " ");
				//nBS.append(lastElementB.toString().replaceFirst(".*/([^/?]+).*", "$1") + " ");
			}
			
			//nBS.append(inB.getLastElement()+ " ");
			
//			for (Node nB : inB.getNeighborhood()) {
//				nBS.append(nB.toString() + " ");
//			}
		}
		
		sim = cosine(nAS.toString(), nBS.toString());
//		System.out.println(nAS.toString());
//		System.out.println();
//		System.out.println(nBS.toString());
//		System.out.println(nAS.toString());
//		System.out.println(nBS.toString());
		
		return sim;
	}
	
	public static double compareByTextSoftTFIDF(Instance a, Instance b){
		double sim = 0.0;
		
		StringBuilder nAS = new StringBuilder();
		StringBuilder nBS = new StringBuilder();
		
		for (InstanceNeighborhood inA : a.getInstanceNeighborhoodList()) {
			
			Node lastElementA = inA.getLastElement();
			if (lastElementA.getTypeOfNode().equals("LiteralImpl")) {
				nAS.append(inA.getLastElement()+ " ");	
			} else {
				nAS.append(lastElementA.toString().replaceFirst(".*/([^/?]+).*", "$1").replace("Category:", "") + " ");
				//nAS.append(lastElementA.toString().replaceFirst(".*/([^/?]+).*", "$1") + " ");
			}
			
			//nAS.append(inA.getLastElement()+ " ");
			
//			for (Node nA : inA.getNeighborhood()) {
//				nAS.append(nA.toString() + " ");
//			}
		}
		
		for (InstanceNeighborhood inB : b.getInstanceNeighborhoodList()) {
			
			Node lastElementB = inB.getLastElement();
			if (lastElementB.getTypeOfNode().equals("LiteralImpl")) {
				nBS.append(inB.getLastElement()+ " ");	
			} else {
				nBS.append(lastElementB.toString().replaceFirst(".*/([^/?]+).*", "$1").replace("Category:", "") + " ");
				//nBS.append(lastElementB.toString().replaceFirst(".*/([^/?]+).*", "$1") + " ");
			}
			
			//nBS.append(inB.getLastElement()+ " ");
			
//			for (Node nB : inB.getNeighborhood()) {
//				nBS.append(nB.toString() + " ");
//			}
		}
		
		sim = softTFIDF(nAS.toString(), nBS.toString());
		
		return sim;
	}
	
	/**
	 * Compare - Method 2 A(Cosine)
	 *
	 * @param a Instance a
	 * @param b Instance b
	 * @param p Predicate
	 * @return double Score of similarity
	 */
	public static double compare2A(Instance a, Instance b, Predicate p) {
		List<InstanceNeighborhood> lNA = getNeighborhoodListWithPredicate(a, p);
		List<InstanceNeighborhood> lNB = getNeighborhoodListWithPredicate(b, p);

		// score instanceNeighborhood
		double score = 0.0;

		for (InstanceNeighborhood nA : lNA) {

			Node lastElementA = nA.getLastElement();
			if (lastElementA.getTypeOfNode().equals("LiteralImpl")) {

				for (InstanceNeighborhood instanceNeighborhood : lNB) {
					Node lastElementB = instanceNeighborhood.getLastElement();

					if (!shareAToken(lastElementA.getNode(), lastElementB.getNode())) {
						continue;
					}

					score += cosine(lastElementA.getNode(), lastElementB.getNode());
				}

			} else {

				for (InstanceNeighborhood instanceNeighborhood : lNB) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					if (lastElementA.getNode().equals(lastElementB.getNode())) {
						score += 1.0;
					}
				}
			}
		}

		return score;
	}
	
	/**
	 * Compare - Method 2 B(SoftTFIDF)
	 *
	 * @param a Instance a
	 * @param b Instance b
	 * @param p Predicate
	 * @return double Score of similarity
	 */
	public static double compare2B(Instance a, Instance b, Predicate p) {
		List<InstanceNeighborhood> lNA = getNeighborhoodListWithPredicate(a, p);
		List<InstanceNeighborhood> lNB = getNeighborhoodListWithPredicate(b, p);

		// score instanceNeighborhood
		double score = 0.0;

		for (InstanceNeighborhood nA : lNA) {

			Node lastElementA = nA.getLastElement();
			if (lastElementA.getTypeOfNode().equals("LiteralImpl")) {

				for (InstanceNeighborhood instanceNeighborhood : lNB) {
					Node lastElementB = instanceNeighborhood.getLastElement();

					if (!shareAToken(lastElementA.getNode(), lastElementB.getNode())) {
						continue;
					}

					score += softTFIDF(lastElementA.getNode(), lastElementB.getNode());
				}

			} else {

				for (InstanceNeighborhood instanceNeighborhood : lNB) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					if (lastElementA.getNode().equals(lastElementB.getNode())) {
						score += 1.0;
					}
				}
			}
		}

		return score;
	}

	/**
	 * Get the neighborhoods of i with the predicate p
	 * @param i Instance
	 * @param p predicate
	 * @return List<InstanceNeighborhood> List of neighborhoods
	 */
	public static List<InstanceNeighborhood> getNeighborhoodListWithPredicate(Instance i, Predicate p) {
		List<InstanceNeighborhood> instanceNeighborhoodList = new ArrayList<>();

		for (InstanceNeighborhood nI : i.getInstanceNeighborhoodList()) {
			int aux = 0;
			boolean valid = true;
			while (aux < nI.getNeighborhood().size()) {
				if (!nI.getNeighborhood().get(aux).getNode().equals(p.getURI())) {
					valid = false;
					break;
				}
				aux = aux + 2;
			}

			if (valid) {
				instanceNeighborhoodList.add(nI);
			}
		}

		return instanceNeighborhoodList;
	}
	
	/**
	 * Gets the list of neighborhoods in b that are comparable to the neighborhood inA.
	 * @param inA Neighborhood in A
	 * @param b Instance b
	 * @return List<InstanceNeighborhood> List of neighborhoods
	 */
	public static List<InstanceNeighborhood> getComparableInstanceNeighborhoodList(InstanceNeighborhood inA, Instance b){
		List<InstanceNeighborhood> instanceNeighborhoodList = new ArrayList<>();
		
		for (InstanceNeighborhood inB : b.getInstanceNeighborhoodList()) {
			if (inA.getNeighborhood().size() == inB.getNeighborhood().size()) {
				
				int i = 0;
				boolean valid = true;
				while (i < inA.getNeighborhood().size()) {
					if (!inA.getNeighborhood().get(i).getNode().equals(inB.getNeighborhood().get(i).getNode())) {
						valid = false;
						break;
					}
					i = i+2;
				}
				
				if (valid) {
					instanceNeighborhoodList.add(inB);	
				}
			}
		}
		
		return instanceNeighborhoodList;
	}
	
	/**
	 * Checks if two string share at least one token
	 * @param sA String a
	 * @param sB String b
	 * @return boolean
	 */
	private static boolean shareAToken(String sA, String sB/*, double k, double delta*/){
		String[] aA = sA.split(" ");
		String[] aB = sB.split(" ");
		
		List<String> lA = Arrays.asList(aA);
		List<String> lB = Arrays.asList(aB);
		
		if (intersect(lA, lB).size() > 0) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Similarity based on Jaro Winkler TFIDF
	 * 
	 * @param s1 String 1
	 * @param s2 String 2
	 * @return Double score
	 */
	private static Double softTFIDF(String s1, String s2) {
		try {
			SoftTFIDF sf = new SoftTFIDF();
			return sf.score(s1, s2);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return 0.0;
	}
	
	/**
	 * Similarity based on Cosine
	 * 
	 * @param s1 String 1
	 * @param s2 String 2
	 * @return Double score
	 */
	private static Double cosine(String s1, String s2){
		Cosine cosine = new Cosine();
		return cosine.similarity(s1, s2);
	}
	
	/**
	 * Similarity based on Cosine
	 * 
	 * @param s1 String 1
	 * @param s2 String 2
	 * @return Double score
	 */
	private static Double jaro(String s1, String s2){
		JaroWinklerTFIDF jaro = new JaroWinklerTFIDF();
		return jaro.score(s1, s2);
	}
	
	/**
	 * Distance based on Levenshtein
	 * 
	 * @param s1 String 1
	 * @param s2 String 2
	 * @return Double score
	 */
	public static Double levenshtein(String s1, String s2){
		Levenstein l = new Levenstein();
		return l.score(s1, s2);
	}
	
	/**
	 * Intersection of two sets
	 * 
	 * @param arlFirst
	 * @param arlSecond
	 * @return
	 */
	public static List<?> intersect(List<?> arlFirst, List<?> arlSecond) {
		ArrayList<?> arlHold = new ArrayList<>(arlFirst);
		arlHold.retainAll(arlSecond);
		return arlHold;
	}

	/**
	 * Union of two sets
	 * 
	 * @param arlFirst
	 * @param arlSecond
	 * @return
	 */
	public static List<?> union(List<?> arlFirst, List<?> arlSecond) {
		ArrayList<Object> arlHold = new ArrayList<>(arlFirst);
		arlHold.addAll(arlSecond);
		return arlHold;
	}
	
	/**
	 * Difference of two sets
	 * 
	 * @param arlFirst
	 * @param arlSecond
	 * @return
	 */
	public static List<?> diff(List<?> arlFirst, List<?> arlSecond) {
		ArrayList<?> arlHold = new ArrayList<>(arlFirst);
		arlHold.removeAll(arlSecond);
		return arlHold;
	}
}
