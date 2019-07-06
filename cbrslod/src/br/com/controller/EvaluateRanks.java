package br.com.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.math.plot.Plot2DPanel;
import org.math.plot.plotObjects.BaseLabel;

import br.com.model.DBPediaEndpoint;
import br.com.model.Instance;
import br.com.model.Predicate;
import br.com.tools.NDCG1;
import br.com.tools.Round;

public class EvaluateRanks {
	
	static double[] p1TotalMax, p2TotalMax;
	static int countMax = 0;
	static String toMax;
	
	public static void evaluateFilm(String to, String params, List<String> instanceList, List<Predicate> predicateList){
		//evaluateMuseum(to, params, instanceList, predicateList);
		evaluateFilmCLByNDCG(to, params, instanceList, predicateList);		
		//evaluateFilmCLByPrecisionRecall(to, params, instanceList);
	}
	
	public static void evaluateFilmCLByNDCG(String to, String params, List<String> instanceList, List<Predicate> predicateList){
		
		FileWriter fw, fwLog, fwPredicateList;
		try {
			File folder = new File(to);
			folder.mkdir();
			
			fwLog = new FileWriter("./" + to + "/resultsLog.txt", true);
			fw = new FileWriter("./" + to + "/results.txt", true);
			
			System.out.println("**********************************************************************************************************"+params+"***************************************************************************************************");
			fwLog.write("\n\n******************************************************************************************************"+params+"*******************************************************************************************************\n");
			fwPredicateList = new FileWriter("./" + to + "/resultsPredicates.txt", true);
			
			fwPredicateList.write("\n\n******************************************************************************************************"+params+"*******************************************************************************************************\n");
			
			for (Predicate predicate : predicateList) {
				fwPredicateList.write(predicate.toString()+"\n");
			}
			fwPredicateList.write("\n");
			
			Color dark = Color.BLUE;
			Color red = Color.RED;
			
			int instanceListSize = instanceList.size(), validSize=0;
			
	
			//Get the rank lists
			System.out.println("\nComparing Ranks lists ...");
			List<Rank> rank1AList = DBPediaEndpoint.getRank("Rank.ser");
			List<Rank> goldenRankList = DBPediaEndpoint.getRank("TMDBColluciLengRank.ser");
			List<Rank> cLRankList = DBPediaEndpoint.getRank("ColluciLengRank.ser");
	
			String format = "%s%-75s";
			System.out.printf(format,"","Rank 1A");
			System.out.printf(format,"","          ");
			System.out.printf(format,"","goldenRank");
			System.out.printf(format,"","CL Rank 1A");
			System.out.println("\n");
			
			int begin = 3;
			int end = 8;
			int size = end-begin+1;
				
			double[] x = new double[size];
			
			double[] p1Total = new double[size];
			double[][] p1TotalAux = new double[size][instanceListSize];
			
			double[] p2Total = new double[size];
			double[][] p2TotalAux = new double[size][instanceListSize];
			
			//Sets the labels for the X-axis
			int aux = 0;
			for (int j = begin; j <= end; j++) {
				x[aux++] = j;
			}
			
			for (int k = 0; k < instanceList.size(); k++) {
				Instance instance = new Instance(instanceList.get(k));
			
				Rank rank1A;
				Rank goldenRank;
				Rank cLRank;
				try {
					rank1A = rank1AList.get(rank1AList.indexOf(new Rank(instance.toString())));
					goldenRank = goldenRankList.get(goldenRankList.indexOf(new Rank(instance.toString())));
					cLRank = cLRankList.get(cLRankList.indexOf(new Rank(instance.toString())));
				} catch (Exception e) {
					System.out.println(instance.toString().replace('–', '?'));
					e.printStackTrace();
					continue;
				}
				
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("\n\n-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
				
				System.out.printf(format,"",rank1A);
				fwLog.write(String.format(format,"",rank1A));
				
				System.out.printf(format,"",goldenRank);
				fwLog.write(String.format(format,"",goldenRank));
				
				System.out.printf(format,"",cLRank);
				fwLog.write(String.format(format,"",cLRank));
				
				System.out.println();	
				fwLog.write("\n");
				
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
				
				int sizeRank =  goldenRank.getRankedInstances().size() > cLRank.getRankedInstances().size() ? cLRank.getRankedInstances().size() : goldenRank.getRankedInstances().size();
				if (sizeRank<3) {
					System.out.println();
					continue;
				}
				validSize++;
				
				for (int i = 0; i < sizeRank; i++) {
					System.out.printf(format,"",rank1A.getRankedInstances().get(i));
					fwLog.write(String.format(format,"",rank1A.getRankedInstances().get(i)));
					
					System.out.printf(format,"",goldenRank.getRankedInstances().get(i));
					fwLog.write(String.format(format,"",goldenRank.getRankedInstances().get(i)));
					
					System.out.printf(format,"",cLRank.getRankedInstances().get(i));
					fwLog.write(String.format(format,"",cLRank.getRankedInstances().get(i)));
					
					System.out.println();
					fwLog.write("\n");
					
				}
				System.out.println();
				
				
				
				double[] p1 = new double[size];
				double[] p2 = new double[size];
				int i = 0;
	
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
				for (int j = begin; j <= sizeRank; j++) {
					
					double rank1ANDCG = NDCG1.calculateNDCG(cLRank.getRankedInstancesLS(), rank1A.getRankedInstancesLS(),j);
					System.out.printf(format,"",rank1ANDCG);
					fwLog.write(String.format(format,"",rank1ANDCG));
								
					double goldenRankNDCG = NDCG1.calculateNDCG(cLRank.getRankedInstancesLS(), goldenRank.getRankedInstancesLS(),j);
					System.out.printf(format,"",goldenRankNDCG);
					fwLog.write(String.format(format,"",goldenRankNDCG));
					
					System.out.println();
					fwLog.write("\n");
					
					if (j > end) {
						continue;
					}
					
					p1[i] = Round.round(rank1ANDCG,2);
					p1Total[i] += rank1ANDCG;
					p1TotalAux[i][k] = rank1ANDCG;
					
					p2[i] = Round.round(goldenRankNDCG,2);
					p2Total[i] += goldenRankNDCG;
					p2TotalAux[i][k] = goldenRankNDCG;
					
					i++;
				}	
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				
				System.out.println();
				fwLog.write("\n");
				
				System.out.println(instance.toString().replace("_", " "));
				fwLog.write(instance.toString().replace("_", " "));
				
				System.out.println("Proposed");
				fwLog.write("\nProposed");
				
				System.out.println(Arrays.toString(p1));
				fwLog.write(Arrays.toString(p1));
				
				System.out.println("TMDB");
				fwLog.write("\nTMDB");
				
				System.out.println(Arrays.toString(p2));
				fwLog.write(Arrays.toString(p2));
				
				System.out.println("\n");
				fwLog.write("\n");
				
				//Graph
				Object lines[][] = new Object[2][4];
				lines[0][0] = dark;
				lines[0][1] = x;
				lines[0][2] = p1;
				lines[0][3] = "Proposed";
				
				lines[1][0] = red;
				lines[1][1] = x;
				lines[1][2] = p2;
				lines[1][3] = "TMDB";
				
				//printGraph(instance.toString().replace("_", " "), to, lines, false);
				
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n\n");
			}
			
			//GRÁFICO DE MÉDIA
			for (int i= 0; i < size; i++) {
				p1Total[i] = Round.round(p1Total[i]/validSize,2);
				p2Total[i] = Round.round(p2Total[i]/validSize,2);
			}
			
			System.out.println("Média");
			System.out.println("Proposed");
			System.out.println(Arrays.toString(p1Total));
			System.out.println("TMDB");
			System.out.println(Arrays.toString(p2Total));
			
			
			if (p1TotalMax == null) {
				p1TotalMax = new double[size];
			}
			
			if (p2TotalMax == null) {
				p2TotalMax = new double[size];
			}
			
			System.out.println();
			int count = 0;
			for (int i = 0; i < p1Total.length; i++) {
				if (p1Total[i] > p1TotalMax[i]) {
					count++;
				}
			}
			
			if (count > countMax) {
				p1TotalMax = p1Total;
				toMax = to;
			}
			System.out.println(toMax + " Proposed " + Arrays.toString(p1TotalMax));
			
			count = 0;
			for (int i = 0; i < p2Total.length; i++) {
				if (p2Total[i] > p2TotalMax[i]) {
					count++;
				}
			}
			
			if (count > countMax) {
				p2TotalMax = p2Total;
				toMax = to;
			}
			System.out.println(toMax + " TMDB " + Arrays.toString(p2TotalMax));
			
			
	
			
			//Graph
			Object lines[][] = new Object[2][4];
			lines[0][0] = dark;
			lines[0][1] = x;
			lines[0][2] = p1Total;
			lines[0][3] = "Proposed";
			
			lines[1][0] = red;
			lines[1][1] = x;
			lines[1][2] = p2Total;
			lines[1][3] = "TMDB";
			
			printGraph("Média "+params, to, lines, true);
			
			fwLog.close();
			fwPredicateList.close();
			fw.write(params + " Proposed |" + Arrays.toString(p1Total) + " == ");
			fw.write(params + " TMDB |" + Arrays.toString(p2Total) + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void evaluateFilmCLByPrecisionRecall(String to, String params, List<String> instanceList){
		//Get the rank lists
		System.out.println("\nComparing Ranks lists ...");
		List<Rank> rank1AList = DBPediaEndpoint.getRank("Rank.ser");
		List<Rank> goldenRankList = DBPediaEndpoint.getRank("TMDBColluciLengRank.ser");
		List<Rank> cLRankList = DBPediaEndpoint.getRank("ColluciLengRank.ser");
		
		double precisionTotalRank = 0;
		double precisionTotalTMDB = 0;
		int validSize=0;
		
		String format = "%s%-75s";
		System.out.printf(format,"","Rank 1A");
		System.out.printf(format,"","goldenRank");
		System.out.printf(format,"","CL Rank 1A");
		System.out.println("\n");
		
		for (int k = 0; k < instanceList.size(); k++) {
			Instance instance = new Instance(instanceList.get(k));
		
			Rank rank1A;
			Rank goldenRank;
			Rank cLRank;
			try {
				rank1A = rank1AList.get(rank1AList.indexOf(new Rank(instance.toString())));
				goldenRank = goldenRankList.get(goldenRankList.indexOf(new Rank(instance.toString())));
				cLRank = cLRankList.get(cLRankList.indexOf(new Rank(instance.toString())));
			} catch (Exception e) {
				System.out.println(instance.toString());
				e.printStackTrace();
				continue;
			}
			
			int sizeRank =  goldenRank.getRankedInstances().size() > cLRank.getRankedInstances().size() ? cLRank.getRankedInstances().size() : goldenRank.getRankedInstances().size();
			if (sizeRank<3) {
				System.out.println();
				continue;
			}
			validSize++;
			
			List<String> cLRankedList = cLRank.getRankedInstancesLS().subList(0, sizeRank);
			List<String> goldenRankedList = goldenRank.getRankedInstancesLS().subList(0, sizeRank);
			List<String> rankedList = rank1A.getRankedInstancesLS().subList(0, sizeRank);
			
			List<?> truePositiveTMDB = Compare.intersect(goldenRankedList, cLRankedList);
			List<?> falsePositiveTMDB = Compare.diff(cLRankedList,goldenRankedList);
			List<?> falseNegativeTMDB = Compare.diff(goldenRankedList,cLRankedList);
			
			List<?> truePositiveRank = Compare.intersect(rankedList, cLRankedList);
			List<?> falsePositiveRank = Compare.diff(cLRankedList,rankedList);
			List<?> falseNegativeRank = Compare.diff(rankedList,cLRankedList);
			
			double precisionTMDB = (double)truePositiveTMDB.size()/(double)(truePositiveTMDB.size()+falsePositiveTMDB.size());
			double recallTMDB = (double)truePositiveTMDB.size()/(double)(truePositiveTMDB.size()+falseNegativeTMDB.size()); 
			
			double precisionRank = (double)truePositiveRank.size()/(double)(truePositiveRank.size()+falsePositiveRank.size());
			double recallRank = (double)truePositiveRank.size()/(double)(truePositiveRank.size()+falseNegativeRank.size()); 
			
			precisionTotalTMDB += precisionTMDB;
			precisionTotalRank += precisionRank;
			
			System.out.println(instance);
			System.out.println("=======================================");
			for (int i = 0; i < sizeRank; i++) {
				System.out.printf(format,"",rank1A.getRankedInstances().get(i));
				System.out.printf(format,"",goldenRank.getRankedInstances().get(i));
				System.out.printf(format,"",cLRank.getRankedInstances().get(i));
				System.out.println();
			}
			System.out.println("=======================================");
			System.out.println();
			
			
			System.out.println(" Precision Rank " + precisionRank + " Recall " + recallRank);
			System.out.println(" Precision TMDB " + precisionTMDB + " Recall " + recallTMDB);
			System.out.println();
		}
		System.out.println(to + " - " + params);
		System.out.println("Média Precision Rank " + precisionTotalRank/validSize);
		System.out.println("Média Precision TMDB " + precisionTotalTMDB/validSize);
		
		FileWriter fw;
		try {
			File folder = new File(to);
			folder.mkdir();
			
			fw = new FileWriter("./" + to + "/resultsPrecision.txt", true);
			fw.write(params + " Proposed |" + (precisionTotalRank/validSize) + " == ");
			fw.write(" TMDB |" + (precisionTotalTMDB/validSize) + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	
	public static void evaluateFilmByPrecisionRecall(String to, String params, List<String> instanceList){
		//Get the rank lists
		System.out.println("\nComparing Ranks lists ...");
		List<Rank> rank1AList = DBPediaEndpoint.getRank("Rank.ser");
		List<Rank> goldenRankList = DBPediaEndpoint.getRank("TMDBRank.ser");
		
		double precisionTotal = 0;
		for (int k = 0; k < instanceList.size(); k++) {
			Instance instance = new Instance(instanceList.get(k));
		
			Rank rank1A;
			Rank goldenRank;
			try {
				rank1A = rank1AList.get(rank1AList.indexOf(new Rank(instance.toString())));
				goldenRank = goldenRankList.get(goldenRankList.indexOf(new Rank(instance.toString())));
			} catch (Exception e) {
				System.out.println(instance.toString());
				e.printStackTrace();
				continue;
			}
			
			List<String> goldenRankedList = goldenRank.getRankedInstancesLS();
			List<String> rankedList = rank1A.getRankedInstancesLS().subList(0, goldenRankedList.size());
			
			List<?> truePositive = Compare.intersect(goldenRankedList, rankedList);
			List<?> falsePositive = Compare.diff(rankedList,goldenRankedList);
			List<?> falseNegative = Compare.diff(goldenRankedList,rankedList);
			
			double precision = (double)truePositive.size()/(double)(truePositive.size()+falsePositive.size());
			double recall = (double)truePositive.size()/(double)(truePositive.size()+falseNegative.size()); 
			
			precisionTotal += precision;
			
			System.out.println(instance + " Precision " + precision + " Recall " + recall);
		}
		System.out.println(precisionTotal/instanceList.size());
			
	}
	
	public static void evaluateFilmByNDCG(String to, String params, List<String> instanceList){
		Color dark = Color.BLUE;
		
		int instanceListSize = instanceList.size();

		//Get the rank lists
		System.out.println("\nComparing Ranks lists ...");
		List<Rank> rank1AList = DBPediaEndpoint.getRank("Rank.ser");
		List<Rank> goldenRankList = DBPediaEndpoint.getRank("TMDBRank.ser");

		String format = "%s%-30s";
		System.out.printf(format,"","Rank 1A");
		System.out.printf(format,"","goldenRank");
		System.out.println("\n");
		
		int begin = 3;
		int end = 8;
		int size = end-begin+1;
			
		double[] x = new double[size];
		
		double[] p1Total = new double[size];
		double[][] p1TotalAux = new double[size][instanceListSize];
		
		//Sets the labels for the X-axis
		int aux = 0;
		for (int j = begin; j <= end; j++) {
			x[aux++] = j;
		}
		
		for (int k = 0; k < instanceList.size(); k++) {
			Instance instance = new Instance(instanceList.get(k));
		
			Rank rank1A;
			Rank goldenRank;
			try {
				rank1A = rank1AList.get(rank1AList.indexOf(new Rank(instance.toString())));
				goldenRank = goldenRankList.get(goldenRankList.indexOf(new Rank(instance.toString())));
			} catch (Exception e) {
				System.out.println(instance.toString());
				e.printStackTrace();
				continue;
			}
			
			System.out.printf(format,"",rank1A);
			System.out.printf(format,"",goldenRank);
			System.out.println();			
			for (int i = 0; i < goldenRank.getRankedInstances().size(); i++) {
				System.out.printf(format,"",rank1A.getRankedInstances().get(i));
				System.out.printf(format,"",goldenRank.getRankedInstances().get(i));
				System.out.println();
			}
			System.out.println();
			
			
			double[] p1 = new double[size];
			int i = 0;
			for (int j = begin; j <= goldenRank.getRankedInstances().size(); j++) {
				
				double rank1ANDCG = NDCG1.calculateNDCG(goldenRank.getRankedInstancesLS(), rank1A.getRankedInstancesLS(),j);
				System.out.printf(format,"",rank1ANDCG);
				System.out.println();
				
				if (j > end) {
					continue;
				}
				
				p1[i] = Round.round(rank1ANDCG,2);
				p1Total[i] += rank1ANDCG;
				p1TotalAux[i][k] = rank1ANDCG;
				
				i++;
			}	
			
			System.out.println();
			
			System.out.println(instance.toString().replace("_", " "));
			System.out.println(Arrays.toString(p1));
			
			System.out.println("\n");

			//Graph
			Object lines[][] = new Object[1][3];
			lines[0][0] = dark;
			lines[0][1] = x;
			lines[0][2] = p1;
			
			//printGraph(instance.toString().replace("_", " "), to, lines, false);
			
//			// Create the PlotPanel
//			Plot2DPanel plot = new Plot2DPanel("SOUTH");
//			BaseLabel title = new BaseLabel(instance.toString().replace("_", " "), Color.BLACK, 0.5, 1.1);
//			title.setFont(new Font("Courier", Font.BOLD, 20));
//			plot.addPlotable(title);
//			plot.setAxisLabel(0, "@k");
//			plot.getAxis(0).setLabelPosition(0.5, -0.15);
//			plot.setAxisLabel(1, "nDCG");
//			plot.getAxis(1).setLabelPosition(-0.15, 0.5);
//			
//			// add a line plot to the PlotPanel
//			plot.addLinePlot("Proposed", dark, x, p1);
//			
//			// put the PlotPanel in a JFrame, as a JPanel
//			JFrame frame = new JFrame(instance.toString().replace("_", " ") + " " + to);
//			frame.setContentPane(plot);
//			frame.setSize(600, 600);
//			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			frame.setVisible(true);
//						
//			saveIntoFile(plot, to, instance.toString().replace("_", " "));
//			frame.dispose();
		}
		
		//GRÁFICO DE MÉDIA
		for (int i= 0; i < size; i++) {
			p1Total[i] = Round.round(p1Total[i]/instanceListSize,2);
		}
		
		System.out.println("Média");
		System.out.println(Arrays.toString(p1Total));
		
		
		if (p1TotalMax == null) {
			p1TotalMax = new double[size];
		}
		
		int count = 0;
		for (int i = 0; i < p1Total.length; i++) {
			if (p1Total[i] > p1TotalMax[i]) {
				count++;
			}
		}
		
		if (count > countMax) {
			p1TotalMax = p1Total;
			toMax = to;
		}
		System.out.println(toMax + " " + Arrays.toString(p1TotalMax));
		
		
		FileWriter fw;
		try {
			File folder = new File(to);
			folder.mkdir();
			
			fw = new FileWriter("./" + to + "/results.txt", true);
			fw.write(params + "|" + Arrays.toString(p1Total) + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Graph
		Object lines[][] = new Object[1][3];
		lines[0][0] = dark;
		lines[0][1] = x;
		lines[0][2] = p1Total;
		
		printGraph("Média "+params, to, lines, true);
		
//		//Print graph
//		Plot2DPanel plot = new Plot2DPanel("SOUTH");
//		BaseLabel title = new BaseLabel("Média", Color.BLACK, 0.5, 1.1);
//        title.setFont(new Font("Courier", Font.BOLD, 20));
//        plot.addPlotable(title);        
//        plot.setAxisLabel(0, "@k");
//		plot.getAxis(0).setLabelPosition(0.5, -0.15);		
//		plot.setAxisLabel(1, "nDCG");
//		plot.getAxis(1).setLabelPosition(-0.15, 0.5);		
//		
//		// add a line plot to the PlotPanel
//		plot.addLinePlot("Proposto", dark, x, p1Total);
//		
//		// put the PlotPanel in a JFrame, as a JPanel
//		JFrame frame = new JFrame("Média " + to);
//		frame.setContentPane(plot);
//		frame.setSize(600, 600);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setVisible(true);
//		saveIntoFile(plot, to, "Média");
//		
//		frame.dispose();
	}
	
	public static void evaluateMuseum(String to, String params, List<String> instanceList, List<Predicate> predicateList){
		
		FileWriter fw, fwLog, fwPredicateList;
		try {
			File folder = new File(to);
			folder.mkdir();
			
			fw = new FileWriter("./" + to + "/results.txt", true);
			fwLog = new FileWriter("./" + to + "/resultsLog.txt", true);
			fwPredicateList = new FileWriter("./" + to + "/resultsPredicates.txt", true);
			
			
			System.out.println("**********************************************************************************************************"+params+"***************************************************************************************************");
			fwLog.write("\n\n******************************************************************************************************"+params+"*******************************************************************************************************\n");
			fwPredicateList.write("\n\n******************************************************************************************************"+params+"*******************************************************************************************************\n");
			
			for (Predicate predicate : predicateList) {
				fwPredicateList.write(predicate.toString()+"\n");
			}
			fwPredicateList.write("\n");
			
			Color dark = Color.BLUE;
			Color gray = Color.RED;
			Color light = Color.GREEN;
			
			int museumListSize = instanceList.size();
	
			//Get the rank lists
			System.out.println("\nComparing Ranks lists ...");
			List<Rank> rank1AList = DBPediaEndpoint.getRank("Rank.ser");
			List<Rank> selectorRank095List = DBPediaEndpoint.getRank("SelectorRank095.ser");
			List<Rank> selectorRank098List = DBPediaEndpoint.getRank("SelectorRank098.ser");
			List<Rank> smartHistoryRankList = DBPediaEndpoint.getRank("SmartHistoryRank.ser");
			
			String format = "%s%-50s";
			System.out.printf(format,"","Rank 1A");
			System.out.printf(format,"","selectorRank095");
			System.out.printf(format,"","selectorRank098");
			System.out.printf(format,"","smartHistoryRank");
			System.out.println("\n");
			
			int begin = 3;
			int end = 8;
			int size = end-begin+1;
				
			double[] x = new double[size];
			
			double[] p1Total = new double[size];
			double[][] p1TotalAux = new double[size][museumListSize];
			
			double[] s1Total = new double[size];
			double[][] s1TotalAux = new double[size][museumListSize];
			
			double[] s2Total = new double[size];
			double[][] s2TotalAux = new double[size][museumListSize];
			
			//Sets the labels for the X-axis
			int aux = 0;
			for (int j = begin; j <= end; j++) {
				x[aux++] = j;
			}
			
			for (int k = 0; k < instanceList.size(); k++) {
				String museum = instanceList.get(k);
			
				Rank rank1A = rank1AList.get(rank1AList.indexOf(new Rank(museum.toString())));
				Rank selectorRank095 = selectorRank095List.get(selectorRank095List.indexOf(new Rank(museum.toString())));
				Rank selectorRank098 = selectorRank098List.get(selectorRank098List.indexOf(new Rank(museum.toString())));
				Rank smartHistoryRank = smartHistoryRankList.get(smartHistoryRankList.indexOf(new Rank(museum.toString())));
				
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("\n\n-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
				
				System.out.printf(format,"",rank1A);
				fwLog.write(String.format(format,"",rank1A));
				
				System.out.printf(format,"",selectorRank095);
				fwLog.write(String.format(format,"",selectorRank095));
				
				System.out.printf(format,"",selectorRank098);
				fwLog.write(String.format(format,"",selectorRank098));
				
				System.out.printf(format,"",smartHistoryRank);
				fwLog.write(String.format(format,"",smartHistoryRank));
				
				System.out.println();
				fwLog.write("\n");
				
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("\n\n-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
				
				for (int i = 0; i < 15; i++) {
					System.out.printf(format,"",rank1A.getRankedInstances().get(i));
					fwLog.write(String.format(format,"",rank1A.getRankedInstances().get(i)));
					
					System.out.printf(format,"",selectorRank095.getRankedInstances().get(i));
					fwLog.write(String.format(format,"",selectorRank095.getRankedInstances().get(i)));
					
					System.out.printf(format,"",selectorRank098.getRankedInstances().get(i));
					fwLog.write(String.format(format,"",selectorRank098.getRankedInstances().get(i)));
					
					System.out.printf(format,"",smartHistoryRank.getRankedInstances().get(i));
					fwLog.write(String.format(format,"",smartHistoryRank.getRankedInstances().get(i)));
					
					System.out.println();
					fwLog.write("\n");
				}
				System.out.println();
				
				double[] p1 = new double[size];
				double[] s1 = new double[size];
				double[] s2 = new double[size];
				int i = 0;
				
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("\n\n-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
				for (int j = begin; j <= end; j++) {
					
					double rank1ANDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), rank1A.getRankedInstancesLS(),j);
					p1[i] = Round.round(rank1ANDCG,2);
					p1Total[i] += rank1ANDCG;
					p1TotalAux[i][k] = rank1ANDCG;
					
					double selectorRank095NDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), selectorRank095.getRankedInstancesLS(),j);
					s1[i] = Round.round(selectorRank095NDCG,2);
					s1Total[i] += selectorRank095NDCG;
					s1TotalAux[i][k] = selectorRank095NDCG;
					
					double selectorRank098NDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), selectorRank098.getRankedInstancesLS(),j);
					s2[i] = Round.round(selectorRank098NDCG,2);
					s2Total[i] += selectorRank098NDCG;
					s2TotalAux[i][k] = selectorRank098NDCG;
					
					i++;
					System.out.printf(format,"",rank1ANDCG);
					fwLog.write(String.format(format,"",rank1ANDCG));
					
					System.out.printf(format,"",selectorRank095NDCG);
					fwLog.write(String.format(format,"",selectorRank095NDCG));
					
					System.out.printf(format,"",selectorRank098NDCG);
					fwLog.write(String.format(format,"",selectorRank098NDCG));
					
					System.out.println();
					fwLog.write("\n");
				}	
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("\n\n-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
				
				System.out.println();
				fwLog.write("\n");
				
				System.out.println(museum.toString().replace("_", " "));
				fwLog.write(museum.toString().replace("_", " "));
				
				System.out.println("Proposed");
				fwLog.write("\nProposed");
				
				System.out.println(Arrays.toString(p1));
				fwLog.write(Arrays.toString(p1));
				
				System.out.println("SELEcTor 095");
				fwLog.write("\nSELEcTor 095");
				
				System.out.println(Arrays.toString(s1));
				fwLog.write(Arrays.toString(s1));
			
				System.out.println("SELEcTor 098");
				fwLog.write("\nSELEcTor 098");
				
				System.out.println(Arrays.toString(s2));
				fwLog.write(Arrays.toString(s2));
				
				System.out.println("\n");
				fwLog.write("\n");
				
				
				
				
				
				
				Object lines[][] = new Object[3][4];
				lines[0][0] = dark;
				lines[0][1] = x;
				lines[0][2] = p1;
				lines[0][3] = "Proposed";
				
				lines[1][0] = gray;
				lines[1][1] = x;
				lines[1][2] = s1;
				lines[1][3] = "selectorRank095";
				
				lines[2][0] = light;
				lines[2][1] = x;
				lines[2][2] = s2;
				lines[2][3] = "selectorRank098";
				
				System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
				fwLog.write("\n\n-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
				//printGraph(museum.toString().replace("_", " ") + " " + params, to, lines, false);
				
				
	//			// Create the PlotPanel
	//			Plot2DPanel plot = new Plot2DPanel("SOUTH");
	//			BaseLabel title = new BaseLabel(museum.toString().replace("_", " "), Color.BLACK, 0.5, 1.1);
	//            title.setFont(new Font("Courier", Font.BOLD, 20));
	//            plot.addPlotable(title);
	//			plot.setAxisLabel(0, "@k");
	//			plot.getAxis(0).setLabelPosition(0.5, -0.15);
	//			plot.setAxisLabel(1, "nDCG");
	//			plot.getAxis(1).setLabelPosition(-0.15, 0.5);
	//			
	//			// add a line plot to the PlotPanel
	//			plot.addLinePlot("Proposed", dark, x, p1);
	//			plot.addLinePlot("SELEcTor 0.95", gray, x, s1);
	//			plot.addLinePlot("SELEcTor 0.98", light, x, s2);
	//			
	//			// put the PlotPanel in a JFrame, as a JPanel
	//			JFrame frame = new JFrame(museum.toString().replace("_", " ") + " " + to);
	//			frame.setContentPane(plot);
	//			frame.setSize(600, 600);
	//			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//			frame.setVisible(true);
	//						
	//			saveIntoFile(plot, to, museum.toString().replace("_", " "));
	//			frame.dispose();
			}
			
			//GRÁFICO DE MÉDIA
			for (int i= 0; i < size; i++) {
				p1Total[i] = Round.round(p1Total[i]/museumListSize,2);
				s1Total[i] = Round.round(s1Total[i]/museumListSize,2);
				s2Total[i] = Round.round(s2Total[i]/museumListSize,2);
			}
			
			System.out.println("Média");
			System.out.println(Arrays.toString(p1Total));
			System.out.println(Arrays.toString(s1Total));
			System.out.println(Arrays.toString(s2Total));
			
			if (p1TotalMax == null) {
				p1TotalMax = new double[size];
			}
			
			int count = 0;
			for (int i = 0; i < p1Total.length; i++) {
				if (p1Total[i] > p1TotalMax[i]) {
					count++;
				}
			}
			
			if (count > countMax) {
				p1TotalMax = p1Total;
				toMax = to;
			}
			System.out.println(toMax + " " + Arrays.toString(p1TotalMax));
			
			Object lines[][] = new Object[3][4];
			lines[0][0] = dark;
			lines[0][1] = x;
			lines[0][2] = p1Total;
			lines[0][3] = "Proposed";
			
			lines[1][0] = gray;
			lines[1][1] = x;
			lines[1][2] = s1Total;
			lines[1][3] = "selectorRank095";
			
			lines[2][0] = light;
			lines[2][1] = x;
			lines[2][2] = s2Total;
			lines[2][3] = "selectorRank098";
			
			printGraph("Média " + params, to, lines, true);
			

			fwLog.close();
			fwPredicateList.close();
			fw.write(params + "|" + Arrays.toString(p1Total) + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		//Graph
//		Plot2DPanel plot = new Plot2DPanel("SOUTH");
//		BaseLabel title = new BaseLabel("Média", Color.BLACK, 0.5, 1.1);
//        title.setFont(new Font("Courier", Font.BOLD, 20));
//        plot.addPlotable(title);
//        
//        plot.setAxisLabel(0, "@k");
//		plot.getAxis(0).setLabelPosition(0.5, -0.15);
//		
//		plot.setAxisLabel(1, "nDCG");
//		plot.getAxis(1).setLabelPosition(-0.15, 0.5);
//		
//		// add a line plot to the PlotPanel
//		plot.addLinePlot("Proposto", dark, x, p1Total);
//		plot.addLinePlot("SELEcTor 0.95", gray, x, s1Total);
//		plot.addLinePlot("SELEcTor 0.98", light, x, s2Total);
//		
//		// put the PlotPanel in a JFrame, as a JPanel
//		JFrame frame = new JFrame("Média " + to);
//		frame.setContentPane(plot);
//		frame.setSize(600, 600);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setVisible(true);
//		saveIntoFile(plot, to, "Média");
//		
//		frame.dispose();
	}
	
//	public static void evaluate(String to, List<Instance> instanceList){
//		
//		Color dark = Color.BLUE;
//		Color gray = Color.RED;
//		Color light = Color.GREEN;
//		
//		int museumListSize = instanceList.size();
//
//		//Get the rank lists
//		System.out.println("\nComparing Ranks lists ...");
//		List<Rank> rank1AList = DBPediaEndpoint.getRank("Rank.ser");
//		//List<Rank> rank1BList = DBPediaEndpoint.getRank("Rank1B.ser");
//		List<Rank> selectorRank095List = DBPediaEndpoint.getRank("SelectorRank095.ser");
//		List<Rank> selectorRank098List = DBPediaEndpoint.getRank("SelectorRank098.ser");
//		List<Rank> smartHistoryRankList = DBPediaEndpoint.getRank("SmartHistoryRank.ser");
//		
//		String format = "%s%-30s";
//		System.out.printf(format,"","Rank 1A");
//		//System.out.printf(format,"","Rank 1B");
//		System.out.printf(format,"","selectorRank095");
//		System.out.printf(format,"","selectorRank098");
//		System.out.printf(format,"","smartHistoryRank");
//		System.out.println("\n");
//		
//		int begin = 3;
//		int end = 8;
//		int size = end-begin+1;
//			
//		double[] x = new double[size];
//		
//		double[] p1Total = new double[size];
//		double[][] p1TotalAux = new double[size][museumListSize];
//		
////		double[] p2Total = new double[size];
////		double[][] p2TotalAux = new double[size][museumsList.size()];
//		
//		double[] s1Total = new double[size];
//		double[][] s1TotalAux = new double[size][museumListSize];
//		
//		double[] s2Total = new double[size];
//		double[][] s2TotalAux = new double[size][museumListSize];
//		
//		//Sets the labels for the X-axis
//		int aux = 0;
//		for (int j = begin; j <= end; j++) {
//			x[aux++] = j;
//		}
//		
//		for (int k = 0; k < instanceList.size(); k++) {
//			Instance museum = instanceList.get(k);
//		
//			Rank rank1A = rank1AList.get(rank1AList.indexOf(new Rank(museum.toString())));
//			//Rank rank1B = rank1BList.get(rank1BList.indexOf(new Rank(museum.toString())));
//			Rank selectorRank095 = selectorRank095List.get(selectorRank095List.indexOf(new Rank(museum.toString())));
//			Rank selectorRank098 = selectorRank098List.get(selectorRank098List.indexOf(new Rank(museum.toString())));
//			Rank smartHistoryRank = smartHistoryRankList.get(smartHistoryRankList.indexOf(new Rank(museum.toString())));
//			
//			System.out.printf(format,"",rank1A);
//			//System.out.printf(format,"",rank1B);
//			System.out.printf(format,"",selectorRank095);
//			System.out.printf(format,"",selectorRank098);
//			System.out.printf(format,"",smartHistoryRank);
//			System.out.println();			
//			for (int i = 0; i < 15; i++) {
//				System.out.printf(format,"",rank1A.getRankedInstances().get(i));
//				//System.out.printf(format,"",rank1B.getRankedInstances().get(i));
//				System.out.printf(format,"",selectorRank095.getRankedInstances().get(i));
//				System.out.printf(format,"",selectorRank098.getRankedInstances().get(i));
//				System.out.printf(format,"",smartHistoryRank.getRankedInstances().get(i));
//				System.out.println();
//			}
//			System.out.println();
//			
//			// Create the PlotPanel
//			Plot2DPanel plot = new Plot2DPanel("SOUTH");
//			BaseLabel title = new BaseLabel(museum.toString().replace("_", " "), Color.BLACK, 0.5, 1.1);
//            title.setFont(new Font("Courier", Font.BOLD, 20));
//            plot.addPlotable(title);
//			
//			double[] p1 = new double[size];
////			double[] p2 = new double[size];
//			double[] s1 = new double[size];
//			double[] s2 = new double[size];
//			int i = 0;
//			for (int j = begin; j <= end; j++) {
//				
//				double rank1ANDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), rank1A.getRankedInstancesLS(),j);
//				p1[i] = Round.round(rank1ANDCG,2);
//				p1Total[i] += rank1ANDCG;
//				p1TotalAux[i][k] = rank1ANDCG;
//				
////				double rank1BNDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), rank1B.getRankedInstancesLS(),j);
////				p2[i] = Round.round(rank1BNDCG,2);
////				p2Total[i] += rank1BNDCG;
////				p2TotalAux[i][k] = rank1BNDCG;
//				
//				double selectorRank095NDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), selectorRank095.getRankedInstancesLS(),j);
//				s1[i] = Round.round(selectorRank095NDCG,2);
//				s1Total[i] += selectorRank095NDCG;
//				s1TotalAux[i][k] = selectorRank095NDCG;
//				
//				double selectorRank098NDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), selectorRank098.getRankedInstancesLS(),j);
//				s2[i] = Round.round(selectorRank098NDCG,2);
//				s2Total[i] += selectorRank098NDCG;
//				s2TotalAux[i][k] = selectorRank098NDCG;
//				
//				i++;
//				System.out.printf(format,"",rank1ANDCG);
//				//System.out.printf(format,"",rank1BNDCG);
//				System.out.printf(format,"",selectorRank095NDCG);
//				System.out.printf(format,"",selectorRank098NDCG);
//				System.out.println();
//			}	
//			
//			System.out.println();
//			
//			System.out.println(museum.toString().replace("_", " "));
//			System.out.println(Arrays.toString(p1));
//			//System.out.println(Arrays.toString(p2));
//			System.out.println(Arrays.toString(s1));
//			System.out.println(Arrays.toString(s2));
//			
//			System.out.println("\n");
//			
//			plot.setAxisLabel(0, "@k");
//			plot.getAxis(0).setLabelPosition(0.5, -0.15);
//			plot.setAxisLabel(1, "nDCG");
//			plot.getAxis(1).setLabelPosition(-0.15, 0.5);
//			
//			// add a line plot to the PlotPanel
//			plot.addLinePlot("Proposed", dark, x, p1);
//			//plot.addLinePlot("Proposto 1B", x, p2);
//			plot.addLinePlot("SELEcTor 0.95", gray, x, s1);
//			plot.addLinePlot("SELEcTor 0.98", light, x, s2);
//			
//			// put the PlotPanel in a JFrame, as a JPanel
//			JFrame frame = new JFrame(museum.toString().replace("_", " ") + " " + to);
//			frame.setContentPane(plot);
//			frame.setSize(600, 600);
//			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			frame.setVisible(true);
//						
//			saveIntoFile(plot, to, museum.toString().replace("_", " "));
//			frame.dispose();
//		}
//		
//		
//		//GRÁFICO DE MÉDIA
//		Plot2DPanel plot = new Plot2DPanel("SOUTH");
//		BaseLabel title = new BaseLabel("Média", Color.BLACK, 0.5, 1.1);
//        title.setFont(new Font("Courier", Font.BOLD, 20));
//        plot.addPlotable(title);
//        
//        plot.setAxisLabel(0, "@k");
//		plot.getAxis(0).setLabelPosition(0.5, -0.15);
//		
//		plot.setAxisLabel(1, "nDCG");
//		plot.getAxis(1).setLabelPosition(-0.15, 0.5);
//		
//		for (int i= 0; i < size; i++) {
//			p1Total[i] = Round.round(p1Total[i]/museumListSize,2);
////			p2Total[i] = Round.round(p2Total[i]/museumListSize,2);
//			s1Total[i] = Round.round(s1Total[i]/museumListSize,2);
//			s2Total[i] = Round.round(s2Total[i]/museumListSize,2);
//		}
//		
//		System.out.println("Média");
//		System.out.println(Arrays.toString(p1Total));
////		System.out.println(Arrays.toString(p2Total));
//		System.out.println(Arrays.toString(s1Total));
//		System.out.println(Arrays.toString(s2Total));
//		
//		
//		if (p1TotalMax == null) {
//			p1TotalMax = new double[size];
//		}
//		
//		int count = 0;
//		for (int i = 0; i < p1Total.length; i++) {
//			if (p1Total[i] > p1TotalMax[i]) {
//				count++;
//			}
//		}
//		
//		if (count > countMax) {
//			p1TotalMax = p1Total;
//			toMax = to;
//		}
//		System.out.println(toMax + " " + Arrays.toString(p1TotalMax));
//		
//		
//		// add a line plot to the PlotPanel
//		plot.addLinePlot("Proposto", dark, x, p1Total);
//		//plot.addLinePlot("Proposto 1B", x, p2Total);
//		plot.addLinePlot("SELEcTor 0.95", gray, x, s1Total);
//		plot.addLinePlot("SELEcTor 0.98", light, x, s2Total);
//		
//		// put the PlotPanel in a JFrame, as a JPanel
//		JFrame frame = new JFrame("Média " + to);
//		frame.setContentPane(plot);
//		frame.setSize(600, 600);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setVisible(true);
//		saveIntoFile(plot, to, "Média");
//		
//		frame.dispose();
//		//GRÁFICO DE MÉDIA
////		Plot2DPanel plotDP = new Plot2DPanel("SOUTH");
////		BaseLabel titleDP = new BaseLabel("Desvio Padrão", Color.BLACK, 0.5, 1.1);
////        titleDP.setFont(new Font("Courier", Font.BOLD, 20));
////        plotDP.addPlotable(titleDP);
////        
////        plotDP.setAxisLabel(0, "nDCG@k");
////		plotDP.getAxis(0).setLabelPosition(0.5, -0.15);
////		
////		plotDP.setAxisLabel(1, "Similarity");
////		plotDP.getAxis(1).setLabelPosition(-0.15, 0.5);
////		
////		
////		for (int i= 0; i < size; i++) {
////			p1Total[i] = Round.round(Round.getDesvioPadrao(p1TotalAux[i]),2);
////			p2Total[i] = Round.round(Round.getDesvioPadrao(p2TotalAux[i]),2);
////			s1Total[i] = Round.round(Round.getDesvioPadrao(s1TotalAux[i]),2);
////			s2Total[i] = Round.round(Round.getDesvioPadrao(s2TotalAux[i]),2);
////		}
////		
////		System.out.println("Desvio Padrão");
////		System.out.println(Arrays.toString(p1Total));
////		System.out.println(Arrays.toString(p2Total));
////		System.out.println(Arrays.toString(s1Total));
////		System.out.println(Arrays.toString(s2Total));
////		
////		// add a line plot to the PlotPanel
////		plotDP.addLinePlot("Proposto", dark, x, p1Total);
////		plot.addLinePlot("Proposto 1B", x, p2Total);
////		plotDP.addLinePlot("SELEcTor 0.95", gray, x, s1Total);
////		plotDP.addLinePlot("SELEcTor 0.98", light, x, s2Total);
////		
////		// put the PlotPanel in a JFrame, as a JPanel
////		JFrame frameDP = new JFrame("a plot panel");
////		frameDP.setContentPane(plotDP);
////		frameDP.setSize(600, 600);
////		frameDP.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////		frameDP.setVisible(true);
//	}
	
	public static void printGraph(String label, String to, Object lines[][], boolean saveFile){
		// Create the PlotPanel
		Plot2DPanel plot = new Plot2DPanel("SOUTH");
		BaseLabel title = new BaseLabel(label, Color.BLACK, 0.5, 1.1);
		title.setFont(new Font("Courier", Font.BOLD, 20));
		plot.addPlotable(title);
		plot.setAxisLabel(0, "@k");
		plot.getAxis(0).setLabelPosition(0.5, -0.15);
		plot.setAxisLabel(1, "nDCG");
		plot.getAxis(1).setLabelPosition(-0.15, 0.5);
		
		// add line plot to the PlotPanel
		for (Object[] line : lines) {
			plot.addLinePlot((String)line[3], (Color)line[0], (double[])line[1], (double[])line[2]);
		}
		//plot.addLinePlot("Proposed", dark, x, p1);
		
		// put the PlotPanel in a JFrame, as a JPanel
		JFrame frame = new JFrame(label + " " + to);
		frame.setContentPane(plot);
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
			
		if (saveFile) {
			saveIntoFile(plot, to, label);
		}
		frame.dispose();
	}
	
	public static void saveIntoFile(Plot2DPanel plot, String to, String name){
		try
        {
			File folder = new File(to);
			folder.mkdir();
			Thread.sleep(150);
			
			Component c1 = plot.getComponent(1);
			Component c2 = plot.getComponent(2);

            BufferedImage image = new BufferedImage(c2.getWidth(), c2.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = image.createGraphics();
            c2.print(graphics2D);
            c1.print(graphics2D);
            
            ImageIO.write(image,"jpeg", new File(to + "/" + name +".jpeg"));
        }
        catch(Exception exception)
        {
            //code
        }
	}
}
