package br.com.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.wcohen.ss.SoftTFIDF;

import info.debatty.java.stringsimilarity.Cosine;

public class Compare {
	
	/**
	 * Compare - Method 1 A(Cosine)
	 *
	 * @param a Instance a
	 * @param b Instance b
	 * @param predicateList Predicate List
	 * @return double Score of similarity
	 */
	public static double compare1A(Instance a, Instance b, List<Predicate> predicateList){
		
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
					
					score += cosine(lastElementA.getNode(), lastElementB.getNode());
				}
				
			} else {
				
				for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodListAux) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					if (lastElementA.getNode().equals(lastElementB.getNode())) {
						score += 1.0;
					}
				}
			}
			
			//Apply the tf-idf weight to adjust the score
			int index = predicateList.indexOf(new Predicate(inA.getNeighborhood().get(0).toString()));
			
			double tFIDFA = predicateList.get(index).getTFIDF(a);
			double tFIDFB = predicateList.get(index).getTFIDF(b);
			
			sim += ((tFIDFA+tFIDFB)/2) * score;
			
			
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
	public static double compare1B(Instance a, Instance b, List<Predicate> predicateList){
		
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
					
					score += softTFIDF(lastElementA.getNode(), lastElementB.getNode());
				}
				
			} else {
				
				for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodListAux) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					if (lastElementA.getNode().equals(lastElementB.getNode())) {
						score += 1.0;
					}
				}
			}
			
			//Apply the tf-idf weight to adjust the score
			int index = predicateList.indexOf(new Predicate(inA.getNeighborhood().get(0).toString()));
			
			double tFIDFA = predicateList.get(index).getTFIDF(a);
			double tFIDFB = predicateList.get(index).getTFIDF(b);
			
			sim += ((tFIDFA+tFIDFB)/2) * score;
			
			
		}
		
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
	 * Intersection of two sets
	 * 
	 * @param arlFirst
	 * @param arlSecond
	 * @return
	 */
	private static List<?> intersect(List<?> arlFirst, List<?> arlSecond) {
		ArrayList<?> arlHold = new ArrayList<>(arlFirst);
		arlHold.retainAll(arlSecond);
		return arlHold;
	}

//	/**
//	 * Union of two sets
//	 * 
//	 * @param arlFirst
//	 * @param arlSecond
//	 * @return
//	 */
//	private static List<?> union(List<?> arlFirst, List<?> arlSecond) {
//		ArrayList<Object> arlHold = new ArrayList<>(arlFirst);
//		arlHold.addAll(arlSecond);
//		return arlHold;
//	}
}
