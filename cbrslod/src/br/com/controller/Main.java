package br.com.controller;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import br.com.model.DBPediaEndpoint;
import br.com.tools.NDCG;
import br.com.tools.NDCG1;

import org.apache.commons.lang3.ArrayUtils;
import org.math.plot.*;
import org.math.plot.plotObjects.BaseLabel;

public class Main {

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		
//		System.out.println("Building endpoint...");
//		DBPediaEndpoint dbpedia = new DBPediaEndpoint("dbo:Museum");
//		
//		System.out.println("Calculating predicates stats...");
//		dbpedia.calcPredicateStats();
//		
//		System.out.println("Gets the instance neighborhood...");
//		dbpedia.getInstanceNeighborhood();
//		
//		System.out.println("Calculating predicate term frequency...");
//		dbpedia.calcPredicateFrequencyOnInstance();
//		
//		System.out.println("Calculating Zone Index...");
//		dbpedia.calcZoneIndex();
//		
//		System.out.println("Comparing...");System.exit(0);
//		dbpedia.compare1();
		
		
		
		
		
		 

		 

		
		
		
		
		
		
		List<Instance> museumsList = DBPediaEndpoint.getSetOfMuseumsFiltered();
		
		System.out.println("Comparing Ranks lists ...");
		List<Rank> rankList = DBPediaEndpoint.getRank("Rank.ser");
		List<Rank> selectorRank095List = DBPediaEndpoint.getRank("SelectorRank095.ser");
		List<Rank> selectorRank098List = DBPediaEndpoint.getRank("SelectorRank098.ser");
		List<Rank> smartHistoryRankList = DBPediaEndpoint.getRank("SmartHistoryRank.ser");
		
		String format = "%s%-30s";
		
		System.out.printf(format," ","rank");
		System.out.printf(format," ","selectorRank095");
		System.out.printf(format," ","selectorRank098");
		System.out.printf(format," ","smartHistoryRank");
		System.out.println("\n");
		
		for (Instance museum : museumsList) {
			Rank rank = rankList.get(rankList.indexOf(new Rank(museum.toString())));
			Rank selectorRank095 = selectorRank095List.get(selectorRank095List.indexOf(new Rank(museum.toString())));
			Rank selectorRank098 = selectorRank098List.get(selectorRank098List.indexOf(new Rank(museum.toString())));
			Rank smartHistoryRank = smartHistoryRankList.get(smartHistoryRankList.indexOf(new Rank(museum.toString())));
			
			System.out.printf(format,"_",rank);
			System.out.printf(format,"_",selectorRank095);
			System.out.printf(format,"_",selectorRank098);
			System.out.printf(format,"_",smartHistoryRank);
			System.out.println();			
			for (int i = 0; i < 15; i++) {
				System.out.printf(format,"|",rank.getRankedInstances().get(i));
				System.out.printf(format,"|",selectorRank095.getRankedInstances().get(i));
				System.out.printf(format,"|",selectorRank098.getRankedInstances().get(i));
				System.out.printf(format,"|",smartHistoryRank.getRankedInstances().get(i));
				System.out.println();
			}
			System.out.println();
			
			// create your PlotPanel (you can use it as a JPanel)
			Plot2DPanel plot = new Plot2DPanel("SOUTH");
			BaseLabel title = new BaseLabel(museum.toString(), Color.RED, 0.5, 1.1);
            title.setFont(new Font("Courier", Font.BOLD, 20));
            plot.addPlotable(title);
            
            
			
			double[] x = {3.0,4.0,5.0,6.0,7.0,8.0};
			double[] y1 = new double[6];
			double[] y2 = new double[6];
			double[] y3 = new double[6];
			int i = 0;
			for (int j = 3; j <= 8; j++) {
				double rankNDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), rank.getRankedInstancesLS(),j);
				y1[i] = rankNDCG;
				
				double selectorRank095NDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), selectorRank095.getRankedInstancesLS(),j);
				y2[i] = selectorRank095NDCG;
				
				double selectorRank098NDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), selectorRank098.getRankedInstancesLS(),j);
				y3[i++] = selectorRank098NDCG;
				
				System.out.printf(format," ",rankNDCG);
				System.out.printf(format," ",selectorRank095NDCG);
				System.out.printf(format," ",selectorRank098NDCG);
				System.out.println();
			}
			
			System.out.println("\n");
			
			
			
			// add a line plot to the PlotPanel
			plot.addLinePlot("Proposto", x, y1);
			plot.addLinePlot("SELEcTor 0.95", x, y2);
			plot.addLinePlot("SELEcTor 0.98", x, y3);
			 
			  // put the PlotPanel in a JFrame, as a JPanel
			JFrame frame = new JFrame("a plot panel");
			frame.setContentPane(plot);
			frame.setSize(500, 600);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			
			if(true) continue;
			
//			System.out.printf(format,"",rank);
//			System.out.printf(format,"",selectorRank095);
//			System.out.printf(format,"",selectorRank098);
//			System.out.printf(format,"",smartHistoryRank);
//			System.out.println("\n");
//			
//			for (int j = 0; j < 15; j++) {
//				System.out.printf(format,"",rank.getRankedInstances().get(j));
//				System.out.printf(format,"",selectorRank095.getRankedInstances().get(j));
//				System.out.printf(format,"",selectorRank098.getRankedInstances().get(j));
//				System.out.printf(format,"",smartHistoryRank.getRankedInstances().get(j));
//				System.out.println();
//			}
//			System.out.printf(format,"","--------");
//			System.out.printf(format,"","--------");
//			System.out.printf(format,"","--------");
//			System.out.printf(format,"","--------");
//			System.out.println("\n");
		}
	}
}
