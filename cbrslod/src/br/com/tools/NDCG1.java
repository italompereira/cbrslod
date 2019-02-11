package br.com.tools;

import java.util.List;

public class NDCG1 {
	public static double calculateNDCG(List<String> realData, List<String> predictionData, int k) {
		
		double idcg = calculateIDCG(k);
		double dcg = 0.0;
		
		for (int i = 0; i < k; i++) {
			String item = predictionData.get(i);
			
			int itemRelevance;
			if (!realData.subList(0, k).contains(item)) { 
				itemRelevance = 0;
			} else	/*if (realData.get(i).equals(predictionData.get(i)))*/ { 
				itemRelevance = 1;
			} /*else {
				itemRelevance = 1;
			}*/
			
			if (i == 0) {
				dcg += itemRelevance;
			} else {
				dcg += itemRelevance/(Math.log(i+1)/Math.log(2));
			}
		}
		
		return dcg/idcg;
	}
	
	public static double calculateIDCG(int n) {
		double idcg = 0;
		int itemRelevance = 1;
		
		for (int i = 0; i < n; i++){
			if (i == 0) {
				idcg += itemRelevance;
			} else {
				idcg += itemRelevance/(Math.log(i+1)/Math.log(2));
			}
		} 

		return idcg;
	}
}
