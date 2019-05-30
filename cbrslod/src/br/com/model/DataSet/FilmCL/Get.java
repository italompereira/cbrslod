package br.com.model.DataSet.FilmCL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.json.JSONArray;
import org.json.JSONObject;

import br.com.controller.Compare;
import br.com.controller.Rank;
import br.com.controller.Rank.InstanceSim;
import br.com.model.DBPediaEndpoint;
import br.com.model.EndpointInterface;
import br.com.tools.JsonReader;

public class Get   extends EndpointInterface {
	private String endPoint = DBPediaEndpoint.ENDPOINT;
	private String graph = "http://dbpedia.org";
	private String domain = "dbo:Film";	
	private int limitEndpoint = 10000;
	private static int qtItensRank = 8;
	
	private static String pathFileEndPoint = "./src/br/com/model/DataSet/FilmCL/allInstances.txt";
	
	private static String pathFileML20m = "./src/br/com/model/DataSet/FilmCL/ml-20m/movies.csv";
	private static String pathFileML20mLink = "./src/br/com/model/DataSet/FilmCL/ml-20m/links.csv";
	private static String pathFileCL = "./src/br/com/model/DataSet/FilmCL/moviesim.dataset1.csv";
	private static String pathFileInstancesCL = "./src/br/com/model/DataSet/FilmCL/intancesCL.csv";
	
	private static String pathFileInstancesNP = "./src/br/com/model/DataSet/FilmCL/instancesNotPresent.txt";
	private static String pathFileInstancesNPA = "./src/br/com/model/DataSet/FilmCL/instancesNotPresentAnalyse.txt";
	private static String pathFileInstancesP = "./src/br/com/model/DataSet/FilmCL/instancesPresent.txt";
	private static String pathFileInstancesI = "./src/br/com/model/DataSet/FilmCL/instances.txt";
	
	private static String pathFileTMDBRank = "TMDBColluciLengRank.ser";
	private static String pathFileCLRank = "ColluciLengRank.ser";
	
	public static final String APIKEY = "bcb42643bbf2042990ebb0b3c440e125";
	
	
	public static void main(String args[]){
		Get m = new Get();
		
		File fE = new File(pathFileEndPoint);
		if (!fE.exists()) {
			m.get();
		}
		
		File fCL = new File(pathFileInstancesCL);
		if (!fCL.exists()) {
			m.getListFromColluciLeng();
		}
		
		File fP = new File(pathFileInstancesP);
		if (!fP.exists()) {
			m.matchingEndpointVSInstancesCL();
		}
		
		m.buildRanks();
//		m.buildRankFromInstancesCL();
//		m.buildRankFromTMDB();
		
		
//		File f = new File(pathFileInstancesI);
//		if (!f.exists()) {
//			m.buildInstancesFile();
//		}
		
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
	
	public void getListFromColluciLeng(){

    	File file = new File(pathFileCL);
    	List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
			lines.remove(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File fileML20M = new File(pathFileML20m);
    	List<String> linesML20M = new ArrayList<String>();
		try {
			linesML20M = FileUtils.readLines(fileML20M, StandardCharsets.ISO_8859_1);
			linesML20M.remove(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File fileML20ML = new File(pathFileML20mLink);
    	List<String> linesML20ML = new ArrayList<String>();
		try {
			linesML20ML = FileUtils.readLines(fileML20ML, StandardCharsets.ISO_8859_1);
			linesML20ML.remove(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		//FileWriter fw;
		BufferedWriter fw;
		try {
			Map<Integer, String> map = new HashMap<Integer, String>();
			
			fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFileInstancesCL), StandardCharsets.ISO_8859_1));
			 //fw = new PrintWriter (new OutputStreamWriter (new FileOutputStream (pathFileInstancesCL), "Cp1252"));
			//fw = new FileWriter(pathFileInstancesCL);			
			for (String line : lines) {
				String[] lineSplited = line.split(",");
				int idMovie1 = Integer.parseInt(lineSplited[0]) , idMovie2 = Integer.parseInt(lineSplited[1]), idMovie;
				
				
				for (String lineML : linesML20M) {
					//System.out.println(lineML);
					int first = lineML.indexOf(",");
					int id = Integer.parseInt(lineML.substring(0, first));
					
					if (idMovie1 != id && idMovie2 != id) {
						continue;
					} else {
						if (idMovie1 == id) {
							idMovie = idMovie1;
						} else {
							idMovie = idMovie2;
						}
					}
					
					
					int second = lineML.indexOf(",", first + 1);
					
					String lineMLS;
					if (lineML.contains("\"")) {
						int third = lineML.lastIndexOf("\"");
						lineMLS = lineML.substring(first+2, third);
					} else {
						lineMLS = lineML.substring(first+1, second);
					}
					
					map.put(idMovie, lineMLS);
				}
			}
			
			List<Integer> keys = new ArrayList<Integer>(map.keySet());
			Collections.sort(keys);
			
			for (Integer key : keys) {
				Integer idMovie = key;
			    String movieName = map.get(key);
			    
			    int tmdbId = 0;
			    for (String line : linesML20ML) {
					String[] lineSplited = line.split(",");
					if (key == Integer.parseInt(lineSplited[0])  && lineSplited.length == 3) {
						tmdbId = Integer.parseInt(lineSplited[2]);
						break;
					}					
				}
			    
			    if (tmdbId == 0) {
			    	System.out.println("Erro: tmdbid 0: " + idMovie + "|" +  movieName);
			    	continue;
				}			    
			    
			    fw.write(idMovie + "|" +  movieName + "|" + tmdbId +"\r\n");
			}
			
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    /**
     * Match against an EndPoint and Movies from Colluci and Leng
     */
    public void matchingEndpointVSInstancesCL(){
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
    	File fileML = new File(pathFileInstancesCL);
    	List<String> linesML = new ArrayList<String>();
		try {
			linesML = FileUtils.readLines(fileML, StandardCharsets.ISO_8859_1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			BufferedWriter fwp;
			fwp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFileInstancesP), StandardCharsets.ISO_8859_1));//new FileWriter(pathFileInstancesP);
			BufferedWriter fwnp;
			fwnp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFileInstancesNP), StandardCharsets.ISO_8859_1));//new FileWriter(pathFileInstancesNP);
			BufferedWriter fwnpa;
			fwnpa = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFileInstancesNPA), StandardCharsets.ISO_8859_1));//new FileWriter(pathFileInstancesNPA);
			
			Scanner sc = new Scanner(System.in);
			
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


					fwp.write(lineMLS[0] + "|" + (lineAux2.equals("") ? lineAux1 : lineAux2)  + "|" + lineMLS[2] + "\n");
					

				} else {

					
					
					System.out.println();
					System.out.println(lineML);
					System.out.println((lineAux2.equals("") ? lineAux1 : lineAux2));
					
					System.out.print("Is it valid? 1|0:");
					int valid = sc.nextInt();
					if (valid == 1) {
						fwp.write(lineMLS[0] + "|" + (lineAux2.equals("") ? lineAux1 : lineAux2)  + "|" + lineMLS[2] + "\n");
					} else {
						fwnp.write(lineML + "\n");
						fwnpa.write(lineML + "|" + (lineAux2.equals("") ? lineAux1 : lineAux2)  + "|" + minDistance + "\n");	
					}
					
				}

				System.out.println("\n" + lineMLS[0]);
				System.out.println(lineAux1);
				System.out.println(lineAux2);
				System.out.println();

			}
			
			sc.close();
			fwp.close();
			fwnp.close();
			fwnpa.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void buildRanks(){
    	File file = new File(pathFileCL);
    	List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
			lines.remove(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	File fileP = new File(pathFileInstancesP);
		List<String> linesP = new ArrayList<String>();
		Map<String, RankCL> resultsMap = new HashMap<String, RankCL>();
		List<Rank> rankListCL = new ArrayList<>();
		
		List<Rank> rankListTMDB = new ArrayList<>();		
		List<Rank> rankListAlreadyExist = getRankFromTMDB();
		Map<String, String> resultsMapTMDB = new HashMap<String, String>();
		
		//List<Rank> rankListAlreadyExist = getRank();
		boolean changedRankList = false;
		try {
			linesP = FileUtils.readLines(fileP, StandardCharsets.ISO_8859_1);
			
			for (String line : linesP) {
				try {
					String[] lineS = line.split("\\|");
					resultsMap.put(lineS[0], new RankCL(lineS[1].substring(28)));
				} catch (Exception e) {
					System.out.println(line);
					e.printStackTrace();
				}
			}
			
			/**
			 * Builds the CollucciLengRank
			 */
			for (String line : lines) {
			
				String lineSplited[] = line.split(",");
				String idMovie1 = lineSplited[0] , idMovie2 = lineSplited[1]; 
				Integer rate = Integer.parseInt(lineSplited[2]);
								
				RankCL movie1 = resultsMap.get(idMovie1);
				RankCL movie2 = resultsMap.get(idMovie2);
				
				if (movie1 != null && movie2 != null) {

					if (movie1.getAssociatedResources().get(movie2.toString()) == null) {
						movie1.getAssociatedResources().put(movie2.toString(), (rate == 1 ? 1 : -1));
					} else {
						int rateM1 = movie1.getAssociatedResources().get(movie2.toString());
						movie1.getAssociatedResources().put(movie2.toString(), rateM1 + (rate == 1 ? 1 : -1));
					}
					
					if (movie2.getAssociatedResources().get(movie1.toString()) == null) {
						movie2.getAssociatedResources().put(movie1.toString(), (rate == 1 ? 1 : -1));
					} else {
						int rateM1 = movie2.getAssociatedResources().get(movie1.toString());
						movie2.getAssociatedResources().put(movie1.toString(), rateM1 + (rate == 1 ? 1 : -1));
					}
					
				} else {
					continue;
				}
			}
			
			for (Map.Entry<String,RankCL> entry : resultsMap.entrySet()) {
				String movie = entry.getKey();
				RankCL rankCL = entry.getValue();				
				Map<String, Integer> associatedResources = rankCL.getAssociatedResources();
				
				Rank rank = new Rank(rankCL.toString());
				
				for (Map.Entry<String, Integer> associatedResource : associatedResources.entrySet()) {
					
					if (associatedResource.getValue() >= 0) {
						rank.addInstanceSim(associatedResource.getKey(), 1);
					}
					
				}
				
				if (rank.getUnrankedInstances().size() > 0) {
					rankListCL.add(rank);
				}
			}
			
			
			
			/**
			 * Builds the TMDBRank
			 */
			for (String line : linesP) {
				try {
					String[] lineS = line.split("\\|");
					resultsMapTMDB.put(lineS[4], lineS[1]);
				} catch (Exception e) {
					System.out.println(line);
					e.printStackTrace();
				}
			}
			
			for (int i = 0; i < linesP.size(); i++) {
				System.out.print(i+1);
				String[] lineS = linesP.get(i).split("\\|");

				if (rankListAlreadyExist != null && rankListAlreadyExist.contains(new Rank(lineS[1].substring(28)))) {
					System.out.println(" " + linesP.get(i));
					rankListTMDB.add(rankListAlreadyExist.get(rankListAlreadyExist.indexOf(new Rank(lineS[1].substring(28)))));
					continue;
				}
				changedRankList = true;
				Rank rank = new Rank(lineS[1].substring(28));
				
				String tdmbId = lineS[4];
				int page = 1;
				while (true) {
					String URI = "https://api.themoviedb.org/3/movie/"+tdmbId+"/similar?api_key="+APIKEY+"&language=en-US&page="+page;
					JSONObject json = JsonReader.readJsonFromUrl(URI);
					JSONArray results = json.getJSONArray("results");
					if (results.length() == 0) {
						break;
					}
					
					for (int j = 0; j < results.length(); j++) {
						String tmdbIdSimilar = results.getJSONObject(j).get("id").toString();
						if (!resultsMapTMDB.containsKey(tmdbIdSimilar)) {
							continue;
						}
						
						rank.addInstanceSim(resultsMapTMDB.get(tmdbIdSimilar).substring(28), 1);
						
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
				rankListTMDB.add(rank);
				System.out.println("->TMDB ID: "+ tdmbId + " | " + rank.toString() + " - " + rank.getUnrankedInstances().size());
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		System.out.println("Saving ColluciLengRank.ser");
		try {
			FileOutputStream fos = new FileOutputStream(pathFileCLRank);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(rankListTMDB);
			oos.close();
			fos.close();
			System.out.printf("Serialized " + pathFileCLRank);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("");
		int k=0;
		for (Rank rank : rankListTMDB) {
			if (rank.getUnrankedInstances().size() >= 8) {
				k++;
				System.out.println(k + ": " + rank.toString() + " - " + rank.getUnrankedInstances().size());
				
			}
		}
		
		
		System.out.println("Saving TMDBRank.ser");
		if (changedRankList) {
			try {
				FileOutputStream fos = new FileOutputStream(pathFileTMDBRank);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(rankListTMDB);
				oos.close();
				fos.close();
				System.out.printf("Serialized TMDBRank.ser");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		System.out.println("");
		for (Rank rank : rankListTMDB) {
			System.out.println(rank.toString() + " - " + rank.getUnrankedInstances().size());
		}
		
		
		
		
		
		/**
		 * Builds the instances file
		 */
		int i = 0;
		
		List<Rank> rankListFromTMDBAux = new ArrayList<>();
		List<Rank> rankListFromCLAux = new ArrayList<>();
		
		
		
		try {

			BufferedWriter fwi;
			fwi = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFileInstancesI), StandardCharsets.ISO_8859_1));
			Set<String> instances = new HashSet<>();
		
			for (String line : linesP) {
				String[] lineS = line.split("\\|");
				Rank rank = new Rank(lineS[1].substring(28));
				
				int indexCl = rankListCL.indexOf(rank);
				if (indexCl == -1) {
					continue;
				}
				Rank rankCL = rankListCL.get(indexCl);
				
				int indexTMDB = rankListTMDB.indexOf(rank);
				if (indexTMDB == -1) {
					continue;
				}
				Rank rankTMDB = rankListTMDB.get(indexTMDB);
	
				
				
				
				if (rankTMDB.getUnrankedInstances().size()>=8 && rankCL.getUnrankedInstances().size()>=8) {
					System.out.println((i++) + "-" + rankTMDB.toString());
					
					try {
						instances.add(rank.toString());
						rankListFromCLAux.add(new Rank(rank.toString()));
						rankListFromTMDBAux.add(new Rank(rank.toString()));
						
						fwi.write(rank.toString() + "\n");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
			
			for (Rank rankAux : rankListFromTMDBAux) {
				Rank rank = rankListTMDB.get(rankListTMDB.indexOf(rankAux));
				for (InstanceSim instanceSim : rank.getUnrankedInstances()) {
					if (instances.contains(instanceSim.instance.toString())) {
						rankAux.addInstanceSim(instanceSim.instance.toString(), instanceSim.getSimilarity());
					}
				}
			}
			
			try {
				FileOutputStream fos = new FileOutputStream(pathFileTMDBRank);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(rankListFromTMDBAux);
				oos.close();
				fos.close();
				System.out.printf("Serialized " + pathFileTMDBRank);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			
			for (Rank rankAux : rankListFromCLAux) {
				Rank rank = rankListCL.get(rankListCL.indexOf(rankAux));
				for (InstanceSim instanceSim : rank.getUnrankedInstances()) {
					if (instances.contains(instanceSim.instance.toString())) {
						rankAux.addInstanceSim(instanceSim.instance.toString(), instanceSim.getSimilarity());
					}
				}
			}
			
			try {
				FileOutputStream fos = new FileOutputStream(pathFileCLRank);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(rankListFromCLAux);
				oos.close();
				fos.close();
				System.out.printf("Serialized " + pathFileCLRank);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			
			fwi.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void buildRankFromTMDB(){
    	File fileP = new File(pathFileInstancesP);
		List<String> linesP = new ArrayList<String>();
		Map<String, String> resultsMap = new HashMap<String, String>();
		List<Rank> rankList = new ArrayList<>();
		
		List<Rank> rankListAlreadyExist = getRankFromTMDB();
		boolean changedRankList = false;
		try {
			linesP = FileUtils.readLines(fileP, StandardCharsets.ISO_8859_1);
			
			
			
			for (String line : linesP) {
				try {
					String[] lineS = line.split("\\|");
					resultsMap.put(lineS[4], lineS[1]);
				} catch (Exception e) {
					System.out.println(line);
					e.printStackTrace();
				}
			}
			
			for (int i = 0; i < linesP.size(); i++) {
				System.out.print(i+1);
				String[] lineS = linesP.get(i).split("\\|");

				if (rankListAlreadyExist != null && rankListAlreadyExist.contains(new Rank(lineS[1].substring(28)))) {
					System.out.println(" " + linesP.get(i));
					rankList.add(rankListAlreadyExist.get(rankListAlreadyExist.indexOf(new Rank(lineS[1].substring(28)))));
					continue;
				}
				changedRankList = true;
				Rank rank = new Rank(lineS[1].substring(28));
				
				String tdmbId = lineS[4];
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
				FileOutputStream fos = new FileOutputStream(pathFileTMDBRank);
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
    
    public void buildRankFromInstancesCL(){
    	File file = new File(pathFileCL);
    	List<String> lines = new ArrayList<String>();
		try {
			lines = FileUtils.readLines(file, StandardCharsets.ISO_8859_1);
			lines.remove(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	File fileP = new File(pathFileInstancesP);
		List<String> linesP = new ArrayList<String>();
		Map<String, RankCL> resultsMap = new HashMap<String, RankCL>();
		List<Rank> rankList = new ArrayList<>();
		
		//List<Rank> rankListAlreadyExist = getRank();
		boolean changedRankList = false;
		try {
			linesP = FileUtils.readLines(fileP, StandardCharsets.ISO_8859_1);
			
			for (String line : linesP) {
				try {
					String[] lineS = line.split("\\|");
					resultsMap.put(lineS[0], new RankCL(lineS[1].substring(28)));
				} catch (Exception e) {
					System.out.println(line);
					e.printStackTrace();
				}
			}
			
			for (String line : lines) {
			
				String lineSplited[] = line.split(",");
				String idMovie1 = lineSplited[0] , idMovie2 = lineSplited[1]; 
				Integer rate = Integer.parseInt(lineSplited[2]);
								
				RankCL movie1 = resultsMap.get(idMovie1);
				RankCL movie2 = resultsMap.get(idMovie2);
				
				if (movie1 != null && movie2 != null) {

					if (movie1.getAssociatedResources().get(movie2.toString()) == null) {
						movie1.getAssociatedResources().put(movie2.toString(), (rate == 1 ? 1 : -1));
					} else {
						int rateM1 = movie1.getAssociatedResources().get(movie2.toString());
						movie1.getAssociatedResources().put(movie2.toString(), rateM1 + (rate == 1 ? 1 : -1));
					}
					
					if (movie2.getAssociatedResources().get(movie1.toString()) == null) {
						movie2.getAssociatedResources().put(movie1.toString(), (rate == 1 ? 1 : -1));
					} else {
						int rateM1 = movie2.getAssociatedResources().get(movie1.toString());
						movie2.getAssociatedResources().put(movie1.toString(), rateM1 + (rate == 1 ? 1 : -1));
					}
					
				} else {
					continue;
				}
			}
			
			
			for (Map.Entry<String,RankCL> entry : resultsMap.entrySet()) {
				String movie = entry.getKey();
				RankCL rankCL = entry.getValue();				
				Map<String, Integer> associatedResources = rankCL.getAssociatedResources();
				
				Rank rank = new Rank(rankCL.toString());
				
				for (Map.Entry<String, Integer> associatedResource : associatedResources.entrySet()) {
					
					if (associatedResource.getValue() >= 0) {
						rank.addInstanceSim(associatedResource.getKey(), 1);
					}
					
				}
				
				if (rank.getUnrankedInstances().size() > 0) {
					rankList.add(rank);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileOutputStream fos = new FileOutputStream(pathFileCLRank);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(rankList);
			oos.close();
			fos.close();
			System.out.printf("Serialized " + pathFileCLRank);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		System.out.println("");
		int k=0;
		for (Rank rank : rankList) {
			if (rank.getUnrankedInstances().size() >= 8) {
				k++;
				System.out.println(k + ": " + rank.toString() + " - " + rank.getUnrankedInstances().size());
				
			}
		}
		
		System.out.println("");
		
    }
    
	/**
	 * Gets the similarity matrix cache
	 * @return double[][]
	 */
	@SuppressWarnings({"unchecked"})
	public List<Rank> getRankFromTMDB() {
		List<Rank> rankList = null;
		try {
			FileInputStream fis = new FileInputStream(pathFileTMDBRank);
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
	
	/**
	 * Gets the similarity matrix cache
	 * @return double[][]
	 */
	@SuppressWarnings({"unchecked"})
	public List<Rank> getRankFromCL() {
		List<Rank> rankList = null;
		try {
			FileInputStream fis = new FileInputStream(pathFileCLRank);
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
	
	/**
	 * Builds the instance file after matching the data from EndPoint and MovieLens
	 */
	public void buildInstancesFile(){
		List<Rank> rankListFromTMDB = getRankFromTMDB();
		List<Rank> rankListFromCL = getRankFromCL();
		
		List<Rank> rankListFromTMDBAux = new ArrayList<>();
		List<Rank> rankListFromCLAux = new ArrayList<>();
		
		
		
		try {

			BufferedWriter fwi;
			fwi = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFileInstancesI), StandardCharsets.ISO_8859_1));
			
			//FileWriter fwi;
			//fwi = new FileWriter(pathFileInstancesI);

			Rank cLRank;
			Set<String> instances = new HashSet<>();
			
			for (Rank rank : rankListFromTMDB) {
				
				int index = rankListFromCL.indexOf(rank);
				
				if (index != -1) {
					cLRank = rankListFromCL.get(index);					
				} else {
					continue;
				}
				
				try {
					if (rank.getUnrankedInstances().size() >= qtItensRank && cLRank.getUnrankedInstances().size() >= qtItensRank) {
						instances.add(rank.toString());
						rankListFromTMDBAux.add(new Rank(rank.toString()));
						rankListFromCLAux.add(new Rank(rank.toString()));
						
						fwi.write(rank.toString() + "\n");
						System.out.println(rank.toString()); 
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			for (Rank rankAux : rankListFromTMDBAux) {
				Rank rank = rankListFromTMDB.get(rankListFromTMDB.indexOf(rankAux));
				
				for (InstanceSim instanceSim : rank.getUnrankedInstances()) {
					if (instances.contains(instanceSim.toString())) {
						rankAux.addInstanceSim(instanceSim.toString(), instanceSim.getSimilarity());
					}
				}
			}
			
//			try {
//				FileOutputStream fos = new FileOutputStream(pathFileTMDBRank);
//				ObjectOutputStream oos = new ObjectOutputStream(fos);
//				oos.writeObject(rankListFromTMDBAux);
//				oos.close();
//				fos.close();
//				System.out.printf("Serialized " + pathFileTMDBRank);
//			} catch (IOException ioe) {
//				ioe.printStackTrace();
//			}
			
			
			for (Rank rankAux : rankListFromCLAux) {
				Rank rank = rankListFromCL.get(rankListFromCL.indexOf(rankAux));
				
				for (InstanceSim instanceSim : rank.getUnrankedInstances()) {
					if (instances.contains(instanceSim.toString())) {
						rankAux.addInstanceSim(instanceSim.toString(), instanceSim.getSimilarity());
					}
				}
			}
			
//			try {
//				FileOutputStream fos = new FileOutputStream(pathFileCLRank);
//				ObjectOutputStream oos = new ObjectOutputStream(fos);
//				oos.writeObject(rankListFromCLAux);
//				oos.close();
//				fos.close();
//				System.out.printf("Serialized " + pathFileCLRank);
//			} catch (IOException ioe) {
//				ioe.printStackTrace();
//			}
			
			
			fwi.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
