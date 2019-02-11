package br.com.model.DataSet.Film;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;

import br.com.controller.Compare;
import br.com.model.DBPediaEndpoint;
import br.com.model.EndpointInterface;

public class Get  extends EndpointInterface {
	private String endPoint = DBPediaEndpoint.ENDPOINT;
	private String graph = "http://dbpedia.org";
	private String domain = "dbo:Film";	
	private int limitEndpoint = 1000;
	private static String fileName = "./src/br/com/model/DataSet/Movie/allInstances.txt";
	private static String fileNameMovieLens = /*"./src/br/com/model/DataSet/Movie/instancesNotPresent.txt";//*/"./src/br/com/model/DataSet/Movie/ml-100k/u.item";
	
	private static String fileNameNP = "./src/br/com/model/DataSet/Movie/instancesNotPresent.txt";
	private static String fileNameNPA = "./src/br/com/model/DataSet/Movie/instancesNotPresentAnalyse.txt";
	private static String fileNameP = "./src/br/com/model/DataSet/Movie/instancesPresent.txt";
	private static String fileNameI = "./src/br/com/model/DataSet/Movie/instances.txt";
	
	public static void main(String args[]){
		
		Get m = new Get();
		File f = new File(fileName);
		if (f.exists()) {
			//m.printWiki();
			//m.check();
			//return;
		}
		
		//m.get();
		m.buildInstancesFile();
	}
	
	public void buildInstancesFile(){
    	File file = new File(fileNameP);
    	List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter fwi;
			fwi = new FileWriter(fileNameI);

			for (String line : lines) {
				String[] lineS = line.split("\\|");
				try {
					URI myURI = new URI(lineS[1]);
//					fwi.write(FilenameUtils.getName(myURI.toString()) + "\n");
					fwi.write(myURI.toString().substring(28) + "\n");
					System.out.println(myURI.toString().substring(28)); 
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			fwi.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void get(){
		List<QuerySolution> qsList = this.getSetOfInstances(0);
		System.out.println(qsList.size());
		
		FileWriter fw;
		try {
			fw = new FileWriter(fileName);
			for (QuerySolution qs : qsList) {
				Resource instance = qs.getResource("i");
				Literal label = qs.getLiteral("label");
				Literal release = qs.getLiteral("release");
				fw.write(instance.toString() + "|" + label.getLexicalForm().toString() + "|" + ((release!=null)?release.asLiteral().getLexicalForm():"0000") + "\n");
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
    public List<QuerySolution> getSetOfInstances(int page) {
    	System.out.println("Page " + page);
		// Query
		String szQuery = null;
		int limit = limitEndpoint;
		int offset = page * limit;
		
		szQuery = " PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ " PREFIX : <http://dbpedia.org/resource/> "
				+ " PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ " SELECT DISTINCT * "
				+ (graph == null ? "" : "FROM <" + this.graph + ">")
				+ " WHERE { "
				+ " 	?i a "+ this.domain + " . "
			    + "     ?i rdfs:label ?label "
			    + "     OPTIONAL { ?i dbo:releaseDate ?release } . "
			    + "     FILTER(!isLiteral(?label) || lang(?label) = \"\" || langMatches(lang(?label), \"EN\"))"
				+ " } LIMIT " + limit + " OFFSET " + offset ;

		List<QuerySolution> querySolutionList = new ArrayList<>();
		try {
			ResultSet results = this.queryEndpoint(szQuery, endPoint);
			if (results.hasNext()) {
				while (results.hasNext()) {
					querySolutionList.add(results.nextSolution());
				}
				//this.close();
	    		querySolutionList.addAll(this.getSetOfInstances(++page));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(this.getQuery());
		}
		
		return querySolutionList;
	}
    
    public void check(){
    	File file = new File(fileName);
    	List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	File fileML = new File(fileNameMovieLens);
    	List<String> linesML = new ArrayList<String>();
		try {
			linesML = FileUtils.readLines(fileML, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter fwp;
			fwp = new FileWriter(fileNameP);
			FileWriter fwnp;
			fwnp = new FileWriter(fileNameNP);
			FileWriter fwnpa;
			fwnpa = new FileWriter(fileNameNPA);
			

			for (String lineML : linesML) {
				String[] lineMLS = lineML.split("\\|");
				String movieNameML = lineMLS[1].substring(0, lineMLS[1].length()-6).trim();
				String yearML = lineMLS[1].substring(lineMLS[1].length()-5, lineMLS[1].length()-1);
				if (movieNameML.indexOf("(") != -1 && movieNameML.indexOf(")") != -1) {
					movieNameML = movieNameML.substring(0, movieNameML.indexOf("(")).trim();
				}
				
				if (movieNameML.length() > 5 && (movieNameML.substring(movieNameML.length()-5,movieNameML.length()).equals(", the") || movieNameML.substring(movieNameML.length()-5,movieNameML.length()).equals(", The"))) {
					movieNameML = "The " + movieNameML.substring(0,movieNameML.length()-5);
				}
				
				if (movieNameML.length() > 4 && (movieNameML.substring(movieNameML.length()-4,movieNameML.length()).equals(", la") || movieNameML.substring(movieNameML.length()-4,movieNameML.length()).equals(", La"))) {
					movieNameML = "La " + movieNameML.substring(0,movieNameML.length()-4);
				}
				
				if (movieNameML.length() > 4 && (movieNameML.substring(movieNameML.length()-4,movieNameML.length()).equals(", an") || movieNameML.substring(movieNameML.length()-4,movieNameML.length()).equals(", An"))) {
					movieNameML = "An " + movieNameML.substring(0,movieNameML.length()-4);
				}
				
				if (movieNameML.length() > 3 && (movieNameML.substring(movieNameML.length()-3,movieNameML.length()).equals(", a") || movieNameML.substring(movieNameML.length()-3,movieNameML.length()).equals(", A"))) {
					movieNameML = "A " + movieNameML.substring(0,movieNameML.length()-3);
				}


				System.out.print(movieNameML);
				System.out.print("   ");
				System.out.println(yearML);

				double minDistance = Double.MAX_VALUE*(-1);
				String lineAux1 = "", lineAux2 = "";

				for (String line : lines) {
					String[] lineS = line.split("\\|");

					String movieName = null;
					String year = null;
					if (lineS[1].indexOf("(") != -1 && lineS[1].indexOf(")") != -1 && (lineS[1].indexOf(")") - lineS[1].indexOf("(") > 4)) {
						movieName = lineS[1].substring(0, lineS[1].indexOf("("));
						year = lineS[1].substring(lineS[1].indexOf("("), lineS[1].indexOf(")")+1).substring(1,5);
						if (year.equals("film")) {
							year = lineS[2].substring(0, 4);
						}

					} else {
						movieName = lineS[1];
						year = lineS[2].substring(0, 4);
					}
					movieName = movieName.trim();

					if (Compare.levenshtein(movieName,movieNameML) >= minDistance) {
						System.out.println(movieName + " " + year + " " + lineS[0] + " " + Compare.levenshtein(movieName,movieNameML));
						minDistance = Compare.levenshtein(movieName,movieNameML);

						if (year.equals("0000")) {
							lineAux1 = line;
						}

						if (minDistance == 0.0 && year.equals(yearML)) {
							lineAux2 = line;
							break;
						}
					}
				}

				if (minDistance == 0.0) {


					fwp.write(lineMLS[0] + "|" + (lineAux2.equals("") ? lineAux1 : lineAux2)  + "\n");
					

				} else {

					fwnp.write(lineML + "\n");
					fwnpa.write(lineML + "|" + (lineAux2.equals("") ? lineAux1 : lineAux2)  + "|" + minDistance + "\n");
					
				}


				System.out.println("\n" + lineMLS[0]);
				System.out.println(lineAux1);
				System.out.println(lineAux2);
				System.out.println();

			}
			
			fwp.close();
			fwnp.close();
			fwnpa.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
    public void printWiki(){
    	
    	File file = new File(fileName);
    	List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	for (String line : lines) {
			String[] lineS = line.split("\\|");
			
			String movieName = null;
			String year = null;
			if (lineS[1].indexOf("(") != -1 && lineS[1].indexOf(")") != -1 && (lineS[1].indexOf(")") - lineS[1].indexOf("(") > 4)) {
				movieName = lineS[1].substring(0, lineS[1].indexOf("("));
				year = lineS[1].substring(lineS[1].indexOf("("), lineS[1].indexOf(")")+1).substring(1,5);
				if (year.equals("film")) {
					year = lineS[2].substring(0, 4);
				}
				
			} else {
				movieName = lineS[1];
				year = lineS[2].substring(0, 4);
			}
			movieName = movieName.trim();
			System.out.println(movieName + " " + year + " " + lineS[0]);
		}
    	System.out.println();
    }
}
