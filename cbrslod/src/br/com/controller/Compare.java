package br.com.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.wcohen.ss.SoftTFIDF;

public class Compare {
	
	public static double compareByPredicate(Instance a, Instance b, Predicate p){
		List<InstanceNeighborhood> lNA = getNeighborhoodListWithPredicate(a,p);
		List<InstanceNeighborhood> lNB = getNeighborhoodListWithPredicate(b, p);
		
		//score instanceNeighborhood
		double score = 0.0;
		
		for (InstanceNeighborhood nA : lNA) {
				
			Node lastElementA = nA.getLastElement();
			if (lastElementA.getTypeOfNode().equals("org.apache.jena.rdf.model.impl.LiteralImpl")) {
				
				for (InstanceNeighborhood instanceNeighborhood : lNB) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					
					if (!shareAToken(lastElementA.getNode(), lastElementB.getNode())) {
						continue;
					}
					
					score += sim(lastElementA.getNode(), lastElementB.getNode());
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
	
	private static boolean shareAToken(String sA, String sB/*, double k, double delta*/){
//		String sbA = kDisc(nA, k);
//		String sbB = kDisc(nB, k);
		
		String[] aA = sA.split(" ");
		String[] aB = sB.split(" ");
		
		List<String> lA = Arrays.asList(aA);
		List<String> lB = Arrays.asList(aB);
		
		if (intersect(lA, lB).size() > 0) {
//			Cosine c = new Cosine();
//			double sim = c.similarity(sbA.toString(), sbB.toString());
//			if (sim > delta) {
				return true;
//			}			
		}
		
		return false;
	}
	public static List<InstanceNeighborhood> getNeighborhoodListWithPredicate(Instance i, Predicate p){
		List<InstanceNeighborhood> instanceNeighborhoodList = new ArrayList<>();
		
		for (InstanceNeighborhood nI : i.getInstanceNeighborhoodList()) {
			int aux = 0;
			boolean valid = true;
			while (aux < nI.getNeighborhood().size()) {
				if (!nI.getNeighborhood().get(aux).getNode().equals(p.getURI())) {
					valid = false;
					break;
				}
				aux = aux+2;
			}
				
			if (valid) {
				instanceNeighborhoodList.add(nI);	
			}
		}
		
		return instanceNeighborhoodList;
	}
	
	public static double compares(Instance a, Instance b){
		double sim = 0.0;
		List<Node> listResourceA = new ArrayList<>();
		
		List<Node> listResourceB = new ArrayList<>();
		
		for (InstanceNeighborhood inA : a.getInstanceNeighborhoodList()) {
			Node lastA = inA.getLastElement();
//			if (lastA instanceof Literal) {
//				listLiteralA.add(lastA);
//			} else {
				listResourceA.add(lastA);
//			}
		}
		
		for (InstanceNeighborhood inB : b.getInstanceNeighborhoodList()) {
			Node lastB = inB.getLastElement();
//			if (lastB instanceof Literal) {
//				listLiteralB.add(lastB);
//			} else {
				listResourceB.add(lastB);
//			}
		}
		
		//Compare all Resources with Jaccard
		sim = jaccard(listResourceA, listResourceB);
		
		//
		
		
		return sim;
	}

	/**
	 * Compares two instances and returns their similarities
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double compare(Instance a, Instance b, List<Predicate> predicatesList){
		
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
			if (lastElementA.getTypeOfNode().equals("Literal")) {
				
				for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodListAux) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					score += sim(lastElementA.toString(), lastElementB.toString());
				}
				
			} else {
				
				for (InstanceNeighborhood instanceNeighborhood : instanceNeighborhoodListAux) {
					Node lastElementB = instanceNeighborhood.getLastElement();
					if (lastElementA.equals(lastElementB)) {
						score += 1.0;
					}
				}
			}
			
			//Apply the tf-idf weight to adjust the score
			int index = predicatesList.indexOf(new Predicate(inA.getNeighborhood().get(0).toString()));
			
			double tFIDFA = predicatesList.get(index).getTFIDF(a);
			double tFIDFB = predicatesList.get(index).getTFIDF(b);
			
			sim += ((tFIDFA+tFIDFB)/2) * score;
			
			
		}
		
		return sim;
	}
	
	public static List<InstanceNeighborhood> getComparableInstanceNeighborhoodList(InstanceNeighborhood inA, Instance b){
		List<InstanceNeighborhood> instanceNeighborhoodList = new ArrayList<>();
		
		for (InstanceNeighborhood inB : b.getInstanceNeighborhoodList()) {
			if (inA.getNeighborhood().size() == inB.getNeighborhood().size()) {
				
				int i = 0;
				boolean valid = true;
				while (i < inA.getNeighborhood().size()) {
					if (!inA.getNeighborhood().get(i).equals(inB.getNeighborhood().get(i))) {
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
	 * Similarity based on Jaro Winkler TFIDF
	 * 
	 * @param s1
	 * @param s2
	 * @return Double
	 */
	private static Double sim(String s1, String s2) {
		
		SoftTFIDF sf = new SoftTFIDF();
		return sf.score(s1, s2);
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

	/**
	 * Union of two sets
	 * 
	 * @param arlFirst
	 * @param arlSecond
	 * @return
	 */
	private static List<?> union(List<?> arlFirst, List<?> arlSecond) {
		ArrayList<Object> arlHold = new ArrayList<>(arlFirst);
		arlHold.addAll(arlSecond);
		return arlHold;
	}
	
	/**
	 * Similarity based on Jaro Winkler TFIDF
	 * 
	 * @param s1
	 * @param s2
	 * @return Double
	 */
	private static Double jaccard(List<?> arlFirst, List<?> arlSecond) {
		
		if (arlFirst.isEmpty() && arlSecond.isEmpty()) {
			return 1.0;
		} else {
			return (double) (intersect(arlFirst, arlSecond).size())/union(arlFirst, arlSecond).size();
		}
	}
}
