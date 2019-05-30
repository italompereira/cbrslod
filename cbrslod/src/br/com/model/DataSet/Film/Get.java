package br.com.model.DataSet.Film;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.json.JSONArray;
import org.json.JSONObject;

import br.com.controller.Compare;
import br.com.controller.Rank;
import br.com.model.DBPediaEndpoint;
import br.com.model.EndpointInterface;
import br.com.tools.JsonReader;

public class Get  extends EndpointInterface {
	private String endPoint = DBPediaEndpoint.ENDPOINT;
	private String graph = "http://dbpedia.org";
	private String domain = "dbo:Film";	
	private int limitEndpoint = 10000;
	private static int qtItensRank = 8;
	private static String pathFileEndPoint = "./src/br/com/model/DataSet/Film/allInstances.txt";
	private static String pathFileML100k = /*"./src/br/com/model/DataSet/Film/instancesNotPresent.txt";//*/"./src/br/com/model/DataSet/Film/ml-100k/u.item";
	private static String pathFileML20m = "./src/br/com/model/DataSet/Film/ml-20m/movies.csv";
	private static String pathFileML20mLink = "./src/br/com/model/DataSet/Film/ml-20m/links.csv";
	private static String pathFileML100kXML20m = "./src/br/com/model/DataSet/Film/ML100kXML20m.txt";
	
	private static String pathFileInstancesNP = "./src/br/com/model/DataSet/Film/instancesNotPresent.txt";
	private static String pathFileInstancesNPA = "./src/br/com/model/DataSet/Film/instancesNotPresentAnalyse.txt";
	private static String pathFileInstancesP = "./src/br/com/model/DataSet/Film/instancesPresent.txt";
	private static String pathFileInstancesI = "./src/br/com/model/DataSet/Film/instances.txt";
	
	private static String pathFileRank = "TMDBRank.ser";
	
	public static final String APIKEY = "bcb42643bbf2042990ebb0b3c440e125";
	
	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void main(String args[]){
		Get m = new Get();
		
		File fE = new File(pathFileEndPoint);
		if (!fE.exists()) {
			m.get();
		}
		
		File fP = new File(pathFileInstancesP);
		if (!fP.exists()) {
			//m.printWiki();
			m.matchingEndpointVSML100k();
			//m.matchingEndpointVSML20m();
		}
		
		File fVs = new File(pathFileML100kXML20m);
		if (!fVs.exists()) {
			m.matchingML100kVsML20m();
		}
		
		//File fos = new File(pathFileRank);
		//if (!fos.exists()) {
			m.buildRankFromTMDB();
		//}
		
		File f = new File(pathFileInstancesI);
		if (!f.exists()) {
			m.buildInstancesFile();
		}
		
		m.printRank();
	}
	
	/**
	 * Gets all instances of a domain from an EndPoint
	 */
	public void get(){
		List<QuerySolution> qsList = this.getSetOfInstances(0);
		System.out.println(qsList.size());
		
		FileWriter fw;
		try {
			fw = new FileWriter(pathFileEndPoint);
			for (QuerySolution qs : qsList) {
				Resource instance = qs.getResource("i");
				Literal label = qs.getLiteral("label");
				Literal release = qs.getLiteral("release");
				fw.write(instance.toString() + "|" + label.getLexicalForm().toString() + "|" + ((release!=null)?release.asLiteral().getLexicalForm():"0000") + "\n");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	/**
	 * Execute the query on EndPoint
	 * 
	 * @param page
	 * @return
	 */
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
    
    /**
     * Match against an EndPoint and MovieLens
     */
    public void matchingEndpointVSML100k(){
    	/**
    	 * Load file that represents the EndPoint
    	 */
    	File file = new File(pathFileEndPoint);
    	List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/**
    	 * Load file that represents the MovieLens
    	 */		
    	File fileML = new File(pathFileML100k);
    	List<String> linesML = new ArrayList<String>();
		try {
			linesML = FileUtils.readLines(fileML, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter fwp;
			fwp = new FileWriter(pathFileInstancesP);
			FileWriter fwnp;
			fwnp = new FileWriter(pathFileInstancesNP);
			FileWriter fwnpa;
			fwnpa = new FileWriter(pathFileInstancesNPA);
			

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

						if (minDistance == 0.0) {
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
			e.printStackTrace();
		}
    }
    
    /**
     * Match against an EndPoint and MovieLens
     */
    public void matchingEndpointVSML20m(){
    	File file = new File(pathFileEndPoint);
    	List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	File fileML = new File(pathFileML20m);
    	List<String> linesML = new ArrayList<String>();
		try {
			linesML = FileUtils.readLines(fileML, StandardCharsets.ISO_8859_1);
			linesML.remove(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter fwp;
			fwp = new FileWriter(pathFileInstancesP);
			FileWriter fwnp;
			fwnp = new FileWriter(pathFileInstancesNP);
			FileWriter fwnpa;
			fwnpa = new FileWriter(pathFileInstancesNPA);
			

			for (String lineML : linesML) {
				
				int first = lineML.indexOf(",");
				int second = lineML.indexOf(",", first + 1);
				
				String lineMLS;
				if (lineML.contains("\"")) {
					int third = lineML.lastIndexOf("\"");
					lineMLS = lineML.substring(first+2, third);
				} else {
					lineMLS = lineML.substring(first+1, second);
				}
				
				String movieNameML;
				String yearML = "";
				if (lineMLS.contains("(")) {
					movieNameML = lineMLS.substring(0, lineMLS.indexOf("(")).trim();
					yearML = lineMLS.substring(lineMLS.length()-5, lineMLS.length()-1);
				} else {
					movieNameML = lineMLS.trim();
					yearML = "0000";
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

						if (year.equals("0000") && minDistance == 0.0) {
							lineAux1 = line.substring(0, line.length()-4)+yearML;
						} else if (year.equals("0000")) {
							lineAux1 = line;
						}

						if (minDistance == 0.0 && year.equals(yearML)) {
							lineAux2 = line;
							break;
						}
					}
				}

				if (minDistance == 0.0) {
					fwp.write(lineML.substring(0,lineML.indexOf(",")) + "|" + (lineAux2.equals("") ? lineAux1 : lineAux2)  + "\n");
				} else {
					fwnp.write(lineML + "\n");
					fwnpa.write(lineML + "|" + (lineAux2.equals("") ? lineAux1 : lineAux2)  + "|" + minDistance + "\n");
				}

				System.out.println("\n" + lineML.substring(0,lineML.indexOf(",")));
				System.out.println(lineAux1);
				System.out.println(lineAux2);
				System.out.println();
			}
			
			fwp.close();
			fwnp.close();
			fwnpa.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	/**
	 * Builds the instance file after matching the data from EndPoint and MovieLens
	 */
	public void buildInstancesFile(){
//    	File file = new File(pathFileInstancesP);
//    	List<String> lines = new ArrayList<String>();
//		try {
//			lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		File fileP = new File(pathFileML100kXML20m);
//		List<String> linesP = new ArrayList<String>();
//		Set<String> resultsSet = new HashSet<String>();
//		try {
//			linesP = FileUtils.readLines(fileP, StandardCharsets.UTF_8);
//			
//			for (String line : linesP) {
//				String[] lineS = line.split(",");
//				resultsSet.add(lineS[0]);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			FileWriter fwi;
//			fwi = new FileWriter(pathFileInstancesI);
//
//			for (String line : lines) {
//				String[] lineS = line.split("\\|");
//				
//				if (!resultsSet.contains(lineS[0])) {
//					continue;
//				}
//				
//				try {
//					URI myURI = new URI(lineS[1]);
////					fwi.write(FilenameUtils.getName(myURI.toString()) + "\n");
//					fwi.write(myURI.toString().substring(28) + "\n");
//					System.out.println(myURI.toString().substring(28)); 
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			
//			fwi.close();
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
		List<Rank> rankList = getRank();
		
		
		try {
			FileWriter fwi;
			fwi = new FileWriter(pathFileInstancesI);

			for (Rank rank : rankList) {
				
				try {
					if (rank.getUnrankedInstances().size() >= qtItensRank) {
						//URI myURI = new URI(rank.toString());
						fwi.write(rank.toString() + "\n");
						System.out.println(rank.toString()); 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			fwi.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	
	/**
	 * Prints all data obtained from an EndPoint
	 */
    public void printWiki(){
    	
    	File file = new File(pathFileEndPoint);
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
    
    /**
     * Matching against ml100k and ml20m to get the TMDB ID
     */
    public void matchingML100kVsML20m    (){
		File fileML100k = new File(pathFileML100k);
		List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(fileML100k, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File fileML20m = new File(pathFileML20m);
		List<String> linesML = new ArrayList<String>();
		try {
			linesML = FileUtils.readLines(fileML20m, StandardCharsets.UTF_8);
			linesML.remove(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File fileP = new File(pathFileInstancesP);
		List<String> linesP = new ArrayList<String>();
		Map<String, String> resultsSet = new HashMap<String, String>();
		try {
			linesP = FileUtils.readLines(fileP, StandardCharsets.ISO_8859_1);
			
			for (String line : linesP) {
				String[] lineS = line.split("\\|");
				resultsSet.put(lineS[0], lineS[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File fileML20mLink = new File(pathFileML20mLink);
		List<String> linesMLL = new ArrayList<String>();
		Map<String, String> resultsMap = new HashMap<String, String>();
		try {
			linesMLL = FileUtils.readLines(fileML20mLink, StandardCharsets.UTF_8);
			linesMLL.remove(0);
			
			for (String line : linesMLL) {
				String[] lineS = line.split(",");
				
				if (lineS.length < 3) {
					
					String imdbID = lineS[1];
					String URI = "http://api.themoviedb.org/3/find/tt"+imdbID+"?api_key="+APIKEY+"&external_source=imdb_id";
					JSONObject json = JsonReader.readJsonFromUrl(URI);
					
					JSONArray j = json.getJSONArray("movie_results");
					if (j.length() > 0) {
						resultsMap.put(lineS[0], j.getJSONObject(0).get("id").toString());
					} else {
						resultsMap.put(lineS[0], "NOT FOUND");
						System.out.println("NOT FOUND: IMDB ID " + imdbID);
					}
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				} else {
					resultsMap.put(lineS[0], lineS[2]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int count = 0;
		try {
			Writer fw = null;
						
			fw = new OutputStreamWriter(new FileOutputStream(pathFileML100kXML20m), StandardCharsets.ISO_8859_1);
			    
			//FileWriter fw;
			//fw = new FileWriter(pathFileML100kXML20m);
			
			for (String line : lines) {
				String[] lineS = line.split("\\|");
			
				if (!resultsSet.containsKey(lineS[0])) {
					continue;
				}
				
				String movieName;
				if (lineS[1].contains("(")) {
					movieName = lineS[1].substring(0, lineS[1].indexOf("("));
				} else {
					movieName = lineS[1];
				}
				movieName = movieName.trim();


				boolean found = false;
				for (String lineML : linesML) {
					//System.out.println(lineML);
					int first = lineML.indexOf(",");
					int second = lineML.indexOf(",", first + 1);
					
					String lineMLS;
					if (lineML.contains("\"")) {
						int third = lineML.lastIndexOf("\"");
						lineMLS = lineML.substring(first+2, third);
					} else {
						lineMLS = lineML.substring(first+1, second);
					}
					
					String movieNameML;
					if (lineMLS.contains("(")) {
						movieNameML = lineMLS.substring(0, lineMLS.indexOf("("));
					} else {
						movieNameML = lineMLS;
					}
					
					if (movieName.toLowerCase().equals(movieNameML.toLowerCase().trim()) || lineMLS.contains(movieName)) {
						count++;
						
						String ml100kId = lineS[0];
						String ml20mId = lineML.substring(0, lineML.indexOf(","));
						String tmdbId = resultsMap.get(ml20mId);
						
						System.out.println(ml100kId +","+ ml20mId +","+tmdbId+","+resultsSet.get(lineS[0]));
						fw.write(ml100kId +"|"+ ml20mId +"|"+tmdbId+"|"+resultsSet.get(lineS[0])+"\n");
						
						found = true;
						break;
					}
				}
				
				if (!found) {
					System.out.println(line);
				}
			}
			System.out.println(count);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void buildRankFromTMDB(){
    	File fileP = new File(pathFileML100kXML20m);
		List<String> linesP = new ArrayList<String>();
		Map<String, String> resultsMap = new HashMap<String, String>();
		List<Rank> rankList = new ArrayList<>();
		
		List<Rank> rankListAlreadyExist = getRank();
		boolean changedRankList = false;
		try {
			linesP = FileUtils.readLines(fileP, StandardCharsets.ISO_8859_1);
			
			
			
			for (String line : linesP) {
				try {
					String[] lineS = line.split("\\|");
					resultsMap.put(lineS[2], lineS[3]);
				} catch (Exception e) {
					System.out.println(line);
					e.printStackTrace();
				}
			}
			
			for (int i = 0; i < linesP.size(); i++) {
				System.out.print(i+1);
				String[] lineS = linesP.get(i).split("\\|");

				if (rankListAlreadyExist != null && rankListAlreadyExist.contains(new Rank(lineS[3].substring(28)))) {
					System.out.println(" " + linesP.get(i));
					rankList.add(rankListAlreadyExist.get(rankListAlreadyExist.indexOf(new Rank(lineS[3].substring(28)))));
					continue;
				}
				changedRankList = true;
				Rank rank = new Rank(lineS[3].substring(28));
				
				String tdmbId = lineS[2];
				int page = 1;
				while (true) {
					String URI = "https://api.themoviedb.org/3/movie/"+tdmbId+"/recommendations?api_key="+APIKEY+"&language=en-US&page="+page;
					JSONObject json = JsonReader.readJsonFromUrl(URI);
					JSONArray results = json.getJSONArray("results");
					if (results.length() == 0) {
						break;
					}
					
					for (int j = 0; j < results.length(); j++) {
						String tmdbIdSimilar = results.getJSONObject(j).get("id").toString();
						if (!resultsMap.containsKey(tmdbIdSimilar)) {
							continue;
						}
						
						rank.addInstanceSim(resultsMap.get(tmdbIdSimilar).substring(28), 1);
						
						if (rank.getUnrankedInstances().size() >= 20) {
							//break;
						}
						
					}
					page++;
					
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if (rank.getUnrankedInstances().size() >= 20) {
						break;
					}
				}
				rankList.add(rank);
				System.out.println("->TMDB ID: "+ tdmbId + " | " + rank.toString() + " - " + rank.getUnrankedInstances().size());
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (changedRankList) {
			try {
				FileOutputStream fos = new FileOutputStream(pathFileRank);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(rankList);
				oos.close();
				fos.close();
				System.out.printf("Serialized TMDBRank.ser");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		
		
		System.out.println("");
		for (Rank rank : rankList) {
			System.out.println(rank.toString() + " - " + rank.getUnrankedInstances().size());
		}
		
		System.out.println("");
		
    }
    
    public void printRank(){
    	List<Rank> rankList = getRank();
		int i = 0;
    	for (Rank rank : rankList) {
			if (rank.getUnrankedInstances().size() >= qtItensRank) {
				System.out.println(rank.toString() + " - " + rank.getUnrankedInstances().size());
				i++;
			}
		}
    	System.out.println(rankList.size());
    	System.out.println(i);
    }
    
	/**
	 * Gets the similarity matrix cache
	 * @return double[][]
	 */
	@SuppressWarnings({"unchecked"})
	public List<Rank> getRank() {
		List<Rank> rankList = null;
		try {
			FileInputStream fis = new FileInputStream(pathFileRank);
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
}
