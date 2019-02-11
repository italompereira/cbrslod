package br.com.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.math.plot.Plot2DPanel;
import org.math.plot.plotObjects.BaseLabel;

import br.com.model.DBPediaEndpoint;
import br.com.model.Instance;
import br.com.tools.NDCG1;
import br.com.tools.Round;

public class EvaluateRanks {
	
	static double[] p1TotalMax;
	static int countMax = 0;
	static String toMax;
	
	public static void evaluate(String to){
		
		Color dark = Color.BLUE;
		Color gray = Color.RED;
		Color light = Color.GREEN;
		
		List<Instance> museumsList = DBPediaEndpoint.getSetOfMuseumsFiltered();
		int museumListSize = museumsList.size();

		//Get the rank lists
		System.out.println("\nComparing Ranks lists ...");
		List<Rank> rank1AList = DBPediaEndpoint.getRank("Rank.ser");
		//List<Rank> rank1BList = DBPediaEndpoint.getRank("Rank1B.ser");
		List<Rank> selectorRank095List = DBPediaEndpoint.getRank("SelectorRank095.ser");
		List<Rank> selectorRank098List = DBPediaEndpoint.getRank("SelectorRank098.ser");
		List<Rank> smartHistoryRankList = DBPediaEndpoint.getRank("SmartHistoryRank.ser");
		
		String format = "%s%-30s";
		System.out.printf(format,"","Rank 1A");
		//System.out.printf(format,"","Rank 1B");
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
		
//		double[] p2Total = new double[size];
//		double[][] p2TotalAux = new double[size][museumsList.size()];
		
		double[] s1Total = new double[size];
		double[][] s1TotalAux = new double[size][museumListSize];
		
		double[] s2Total = new double[size];
		double[][] s2TotalAux = new double[size][museumListSize];
		
		//Sets the labels for the X-axis
		int aux = 0;
		for (int j = begin; j <= end; j++) {
			x[aux++] = j;
		}
		
		for (int k = 0; k < museumsList.size(); k++) {
			Instance museum = museumsList.get(k);
		
			Rank rank1A = rank1AList.get(rank1AList.indexOf(new Rank(museum.toString())));
			//Rank rank1B = rank1BList.get(rank1BList.indexOf(new Rank(museum.toString())));
			Rank selectorRank095 = selectorRank095List.get(selectorRank095List.indexOf(new Rank(museum.toString())));
			Rank selectorRank098 = selectorRank098List.get(selectorRank098List.indexOf(new Rank(museum.toString())));
			Rank smartHistoryRank = smartHistoryRankList.get(smartHistoryRankList.indexOf(new Rank(museum.toString())));
			
			System.out.printf(format,"",rank1A);
			//System.out.printf(format,"",rank1B);
			System.out.printf(format,"",selectorRank095);
			System.out.printf(format,"",selectorRank098);
			System.out.printf(format,"",smartHistoryRank);
			System.out.println();			
			for (int i = 0; i < 15; i++) {
				System.out.printf(format,"",rank1A.getRankedInstances().get(i));
				//System.out.printf(format,"",rank1B.getRankedInstances().get(i));
				System.out.printf(format,"",selectorRank095.getRankedInstances().get(i));
				System.out.printf(format,"",selectorRank098.getRankedInstances().get(i));
				System.out.printf(format,"",smartHistoryRank.getRankedInstances().get(i));
				System.out.println();
			}
			System.out.println();
			
			// Create the PlotPanel
			Plot2DPanel plot = new Plot2DPanel("SOUTH");
			BaseLabel title = new BaseLabel(museum.toString().replace("_", " "), Color.BLACK, 0.5, 1.1);
            title.setFont(new Font("Courier", Font.BOLD, 20));
            plot.addPlotable(title);
			
			double[] p1 = new double[size];
//			double[] p2 = new double[size];
			double[] s1 = new double[size];
			double[] s2 = new double[size];
			int i = 0;
			for (int j = begin; j <= end; j++) {
				
				double rank1ANDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), rank1A.getRankedInstancesLS(),j);
				p1[i] = Round.round(rank1ANDCG,2);
				p1Total[i] += rank1ANDCG;
				p1TotalAux[i][k] = rank1ANDCG;
				
//				double rank1BNDCG = NDCG1.calculateNDCG(smartHistoryRank.getRankedInstancesLS(), rank1B.getRankedInstancesLS(),j);
//				p2[i] = Round.round(rank1BNDCG,2);
//				p2Total[i] += rank1BNDCG;
//				p2TotalAux[i][k] = rank1BNDCG;
				
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
				//System.out.printf(format,"",rank1BNDCG);
				System.out.printf(format,"",selectorRank095NDCG);
				System.out.printf(format,"",selectorRank098NDCG);
				System.out.println();
			}	
			
			System.out.println();
			
			System.out.println(museum.toString().replace("_", " "));
			System.out.println(Arrays.toString(p1));
			//System.out.println(Arrays.toString(p2));
			System.out.println(Arrays.toString(s1));
			System.out.println(Arrays.toString(s2));
			
			System.out.println("\n");
			
			plot.setAxisLabel(0, "@k");
			plot.getAxis(0).setLabelPosition(0.5, -0.15);
			plot.setAxisLabel(1, "nDCG");
			plot.getAxis(1).setLabelPosition(-0.15, 0.5);
			
			// add a line plot to the PlotPanel
			plot.addLinePlot("Proposed", dark, x, p1);
			//plot.addLinePlot("Proposto 1B", x, p2);
			plot.addLinePlot("SELEcTor 0.95", gray, x, s1);
			plot.addLinePlot("SELEcTor 0.98", light, x, s2);
			
			// put the PlotPanel in a JFrame, as a JPanel
			JFrame frame = new JFrame(museum.toString().replace("_", " ") + " " + to);
			frame.setContentPane(plot);
			frame.setSize(600, 600);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
						
			saveIntoFile(plot, to, museum.toString().replace("_", " "));
			frame.dispose();
		}
		
		
		//GRÁFICO DE MÉDIA
		Plot2DPanel plot = new Plot2DPanel("SOUTH");
		BaseLabel title = new BaseLabel("Média", Color.BLACK, 0.5, 1.1);
        title.setFont(new Font("Courier", Font.BOLD, 20));
        plot.addPlotable(title);
        
        plot.setAxisLabel(0, "@k");
		plot.getAxis(0).setLabelPosition(0.5, -0.15);
		
		plot.setAxisLabel(1, "nDCG");
		plot.getAxis(1).setLabelPosition(-0.15, 0.5);
		
		for (int i= 0; i < size; i++) {
			p1Total[i] = Round.round(p1Total[i]/museumListSize,2);
//			p2Total[i] = Round.round(p2Total[i]/museumListSize,2);
			s1Total[i] = Round.round(s1Total[i]/museumListSize,2);
			s2Total[i] = Round.round(s2Total[i]/museumListSize,2);
		}
		
		System.out.println("Média");
		System.out.println(Arrays.toString(p1Total));
//		System.out.println(Arrays.toString(p2Total));
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
		
		
		// add a line plot to the PlotPanel
		plot.addLinePlot("Proposto", dark, x, p1Total);
		//plot.addLinePlot("Proposto 1B", x, p2Total);
		plot.addLinePlot("SELEcTor 0.95", gray, x, s1Total);
		plot.addLinePlot("SELEcTor 0.98", light, x, s2Total);
		
		// put the PlotPanel in a JFrame, as a JPanel
		JFrame frame = new JFrame("Média " + to);
		frame.setContentPane(plot);
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		saveIntoFile(plot, to, "Média");
		
		frame.dispose();
		//GRÁFICO DE MÉDIA
//		Plot2DPanel plotDP = new Plot2DPanel("SOUTH");
//		BaseLabel titleDP = new BaseLabel("Desvio Padrão", Color.BLACK, 0.5, 1.1);
//        titleDP.setFont(new Font("Courier", Font.BOLD, 20));
//        plotDP.addPlotable(titleDP);
//        
//        plotDP.setAxisLabel(0, "nDCG@k");
//		plotDP.getAxis(0).setLabelPosition(0.5, -0.15);
//		
//		plotDP.setAxisLabel(1, "Similarity");
//		plotDP.getAxis(1).setLabelPosition(-0.15, 0.5);
//		
//		
//		for (int i= 0; i < size; i++) {
//			p1Total[i] = Round.round(Round.getDesvioPadrao(p1TotalAux[i]),2);
//			p2Total[i] = Round.round(Round.getDesvioPadrao(p2TotalAux[i]),2);
//			s1Total[i] = Round.round(Round.getDesvioPadrao(s1TotalAux[i]),2);
//			s2Total[i] = Round.round(Round.getDesvioPadrao(s2TotalAux[i]),2);
//		}
//		
//		System.out.println("Desvio Padrão");
//		System.out.println(Arrays.toString(p1Total));
//		System.out.println(Arrays.toString(p2Total));
//		System.out.println(Arrays.toString(s1Total));
//		System.out.println(Arrays.toString(s2Total));
//		
//		// add a line plot to the PlotPanel
//		plotDP.addLinePlot("Proposto", dark, x, p1Total);
//		plot.addLinePlot("Proposto 1B", x, p2Total);
//		plotDP.addLinePlot("SELEcTor 0.95", gray, x, s1Total);
//		plotDP.addLinePlot("SELEcTor 0.98", light, x, s2Total);
//		
//		// put the PlotPanel in a JFrame, as a JPanel
//		JFrame frameDP = new JFrame("a plot panel");
//		frameDP.setContentPane(plotDP);
//		frameDP.setSize(600, 600);
//		frameDP.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frameDP.setVisible(true);
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
            
            ImageIO.write(image,"jpeg", new File(to + "/" + name+".jpeg"));
        }
        catch(Exception exception)
        {
            //code
        }
	}
}
