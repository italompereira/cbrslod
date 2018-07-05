package br.com.model.DataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DataSetDownloader {
	
	public static final String TO = "C:/datasets/";
	
	public static final Map<String, String> dbPediaDatasets = new HashMap<String, String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			put("http://downloads.dbpedia.org/2016-10/core/", "dbpedia\\core\\");
		}
	};
	
	static {
		File folder = new File(TO);
		folder.mkdir();
	}
	
	public static void getDataSetsFromDbPedia(String from, String to){
		Document doc;
		
		File datasetfolder = new File(DataSetDownloader.TO + "dbpedia");
		datasetfolder.mkdir();
		
		try {
			System.out.println("Starting download from "+from);
			doc = Jsoup.connect(from).get();
			Elements links = doc.select("body a");
			links.stream().parallel().forEach(file -> {
				if(file.attr("href").contains("json") 
					|| file.attr("href").contains("ttl")
					|| file.attr("href").contains("bz2")
					){
				
					String savedFileName = to+file.text();
					
					File folder = new File(to);
					folder.mkdir();
					
					File test = new File(savedFileName);
					if (!test.exists()) {
						try {
							System.out.println("Download file: "+file.attr("href"));
				            byte[] bytes = Jsoup.connect(file.attr("abs:href"))
				    		   .header("Accept-Encoding", "gzip, deflate")
				    		   .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
				    		   .referrer("http://swat.cse.lehigh.edu/resources/data/swetodblp/")
				    		   .ignoreContentType(true)
				    		   .maxBodySize(0)
				    		   .timeout(14400000)
				    		   .execute()
				    		   .bodyAsBytes();
				            
			                FileOutputStream fos = new FileOutputStream(savedFileName);
			                fos.write(bytes);
			                fos.flush();
			                fos.close();
			                System.out.println("Downloaded "+file.attr("href"));
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
							
						}
					}
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Classe de extração de datasets.
	 * @param args
	 */
	public static void main(String args[]){
		//Gets datasets from endpoint
		for (Map.Entry<String, String> entry : dbPediaDatasets.entrySet()) {
			getDataSetsFromDbPedia(entry.getKey(), DataSetDownloader.TO + entry.getValue());
		}
	}
}
