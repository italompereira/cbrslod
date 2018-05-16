
package br.com.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class StopWords {

    public HashMap<String, Byte> map;
    
    public StopWords(String file) throws FileNotFoundException, IOException{
        map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String row = br.readLine();
            while (row != null){
                String[] p = row.split("[|]");
                String word = p[0].trim();
                //System.out.println(word);
                map.put(word, null);
                row = br.readLine();
            }
        }
    }
    
    public String removeStopWords(String str){
        StringTokenizer st = new StringTokenizer(str, " ");
        StringBuilder sb = new StringBuilder();
        int t = st.countTokens();
        int i = 0;
        while (i < t){
            i++;
            String aux = st.nextToken();
            if (! map.containsKey(aux)){
                sb.append(aux);
                break;
            }
        }
        for (; i<t; i++){
            String aux = st.nextToken();
            if (! map.containsKey(aux)){
                sb.append(" ").append(aux);
            }
        }
        return sb.toString();
    }
}