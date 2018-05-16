
package br.com.tools;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;

public class Util {
    
    private final static HashMap<String, Byte> STOPWORDS = new HashMap<>(900);
    private final static HashMap<String, Byte> STOPWORDS_T = new HashMap<>(900);
    private final static HashMap<String, Byte> STOPWORDS_V = new HashMap<>(900);
    
    static {
        STOPWORDS.put("a", null);
        STOPWORDS.put("about", null);
        STOPWORDS.put("above", null);
        STOPWORDS.put("after", null);
        STOPWORDS.put("again", null);
        STOPWORDS.put("against", null);
        STOPWORDS.put("all", null);
        STOPWORDS.put("am", null);
        STOPWORDS.put("an", null);
        STOPWORDS.put("and", null);
        STOPWORDS.put("any", null);
        STOPWORDS.put("are", null);
        STOPWORDS.put("as", null);
        STOPWORDS.put("at", null);
        STOPWORDS.put("be", null);
        STOPWORDS.put("because", null);
        STOPWORDS.put("been", null);
        STOPWORDS.put("before", null);
        STOPWORDS.put("being", null);
        STOPWORDS.put("below", null);
        STOPWORDS.put("between", null);
        STOPWORDS.put("both", null);
        STOPWORDS.put("but", null);
        STOPWORDS.put("by", null);
        STOPWORDS.put("cannot", null);
        STOPWORDS.put("could", null);
        STOPWORDS.put("did", null);
        STOPWORDS.put("do", null);
        STOPWORDS.put("does", null);
        STOPWORDS.put("doing", null);
        STOPWORDS.put("down", null);
        STOPWORDS.put("during", null);
        STOPWORDS.put("each", null);
        STOPWORDS.put("few", null);
        STOPWORDS.put("for", null);
        STOPWORDS.put("from", null);
        STOPWORDS.put("further", null);
        STOPWORDS.put("had", null);
        STOPWORDS.put("has", null);
        STOPWORDS.put("have", null);
        STOPWORDS.put("having", null);
        STOPWORDS.put("he", null);
        STOPWORDS.put("her", null);
        STOPWORDS.put("here", null);
        STOPWORDS.put("hers", null);
        STOPWORDS.put("herself", null);
        STOPWORDS.put("him", null);
        STOPWORDS.put("himself", null);
        STOPWORDS.put("his", null);
        STOPWORDS.put("how", null);
        STOPWORDS.put("i", null);
        STOPWORDS.put("if", null);
        STOPWORDS.put("in", null);
        STOPWORDS.put("into", null);
        STOPWORDS.put("is", null);
        STOPWORDS.put("it", null);
        STOPWORDS.put("its", null);
        STOPWORDS.put("itself", null);
        STOPWORDS.put("me", null);
        STOPWORDS.put("more", null);
        STOPWORDS.put("most", null);
        STOPWORDS.put("my", null);
        STOPWORDS.put("myself", null);
        STOPWORDS.put("no", null);
        STOPWORDS.put("nor", null);
        STOPWORDS.put("not", null);
        STOPWORDS.put("of", null);
        STOPWORDS.put("off", null);
        STOPWORDS.put("on", null);
        STOPWORDS.put("once", null);
        STOPWORDS.put("one", null);
        STOPWORDS.put("only", null);
        STOPWORDS.put("or", null);
        STOPWORDS.put("other", null);
        STOPWORDS.put("ought", null);
        STOPWORDS.put("our", null);
        STOPWORDS.put("ours ", null);
        STOPWORDS.put("ourselves", null);
        STOPWORDS.put("out", null);
        STOPWORDS.put("over", null);
        STOPWORDS.put("own", null);
        STOPWORDS.put("same", null);
        STOPWORDS.put("she", null);
        STOPWORDS.put("should", null);
        STOPWORDS.put("shouldn't", null);
        STOPWORDS.put("so", null);
        STOPWORDS.put("some", null);
        STOPWORDS.put("such", null);
        STOPWORDS.put("than", null);
        STOPWORDS.put("that", null);
        STOPWORDS.put("the", null);
        STOPWORDS.put("their", null);
        STOPWORDS.put("theirs", null);
        STOPWORDS.put("them", null);
        STOPWORDS.put("themselves", null);
        STOPWORDS.put("then", null);
        STOPWORDS.put("there", null);
        STOPWORDS.put("these", null);
        STOPWORDS.put("they", null);
        STOPWORDS.put("this", null);
        STOPWORDS.put("those", null);
        STOPWORDS.put("through", null);
        STOPWORDS.put("to", null);
        STOPWORDS.put("too", null);
        STOPWORDS.put("under", null);
        STOPWORDS.put("until", null);
        STOPWORDS.put("up", null);
        STOPWORDS.put("very", null);
        STOPWORDS.put("was", null);
        STOPWORDS.put("wasn't", null);
        STOPWORDS.put("we", null);
        STOPWORDS.put("were", null);
        STOPWORDS.put("weren't", null);
        STOPWORDS.put("what", null);
        STOPWORDS.put("when", null);
        STOPWORDS.put("where", null);
        STOPWORDS.put("which", null);
        STOPWORDS.put("while", null);
        STOPWORDS.put("who", null);
        STOPWORDS.put("whom", null);
        STOPWORDS.put("why", null);
        STOPWORDS.put("with", null);
        STOPWORDS.put("won't", null);
        STOPWORDS.put("would", null);
        STOPWORDS.put("wouldn't", null);
        STOPWORDS.put("you", null);
        STOPWORDS.put("your", null);
        STOPWORDS.put("yours", null);
        STOPWORDS.put("yourself", null);
        STOPWORDS.put("yourselves", null);
        
//        STOPWORDS.put("a", null);
//        STOPWORDS.put("agora", null);
//        STOPWORDS.put("ainda", null);
//        STOPWORDS.put("alguem", null);
//        STOPWORDS.put("algum", null);
//        STOPWORDS.put("alguma", null);
//        STOPWORDS.put("algumas", null);
//        STOPWORDS.put("alguns", null);
//        STOPWORDS.put("ampla", null);
//        STOPWORDS.put("amplas", null);
//        STOPWORDS.put("amplo", null);
//        STOPWORDS.put("amplos", null);
//        STOPWORDS.put("ante", null);
//        STOPWORDS.put("antes", null);
//        STOPWORDS.put("ao", null);
//        STOPWORDS.put("aos", null);
//        STOPWORDS.put("apos", null);
//        STOPWORDS.put("aquela", null);
//        STOPWORDS.put("aquelas", null);
//        STOPWORDS.put("aquele", null);
//        STOPWORDS.put("aqueles", null);
//        STOPWORDS.put("aquilo", null);
//        STOPWORDS.put("as", null);
//        STOPWORDS.put("ate", null);
//        STOPWORDS.put("atraves", null);
//        STOPWORDS.put("cada", null);
//        STOPWORDS.put("coisa", null);
//        STOPWORDS.put("coisas", null);
//        STOPWORDS.put("com", null);
//        STOPWORDS.put("como", null);
//        STOPWORDS.put("contra", null);
//        STOPWORDS.put("contudo", null);
//        STOPWORDS.put("da", null);
//        STOPWORDS.put("daquele", null);
//        STOPWORDS.put("daqueles", null);
//        STOPWORDS.put("das", null);
//        STOPWORDS.put("de", null);
//        STOPWORDS.put("dela", null);
//        STOPWORDS.put("delas", null);
//        STOPWORDS.put("dele", null);
//        STOPWORDS.put("deles", null);
//        STOPWORDS.put("depois", null);
//        STOPWORDS.put("dessa", null);
//        STOPWORDS.put("dessas", null);
//        STOPWORDS.put("desse", null);
//        STOPWORDS.put("desses", null);
//        STOPWORDS.put("desta", null);
//        STOPWORDS.put("destas", null);
//        STOPWORDS.put("deste", null);
//        STOPWORDS.put("deste", null);
//        STOPWORDS.put("destes", null);
//        STOPWORDS.put("deve", null);
//        STOPWORDS.put("devem", null);
//        STOPWORDS.put("devendo", null);
//        STOPWORDS.put("dever", null);
//        STOPWORDS.put("devera", null);
//        STOPWORDS.put("deverao", null);
//        STOPWORDS.put("deveria", null);
//        STOPWORDS.put("deveriam", null);
//        STOPWORDS.put("devia", null);
//        STOPWORDS.put("deviam", null);
//        STOPWORDS.put("disse", null);
//        STOPWORDS.put("disso", null);
//        STOPWORDS.put("disto", null);
//        STOPWORDS.put("dito", null);
//        STOPWORDS.put("diz", null);
//        STOPWORDS.put("dizem", null);
//        STOPWORDS.put("do", null);
//        STOPWORDS.put("dos", null);
//        STOPWORDS.put("e", null);
//        STOPWORDS.put("ela", null);
//        STOPWORDS.put("elas", null);
//        STOPWORDS.put("ele", null);
//        STOPWORDS.put("eles", null);
//        STOPWORDS.put("em", null);
//        STOPWORDS.put("enquanto", null);
//        STOPWORDS.put("entre", null);
//        STOPWORDS.put("era", null);
//        STOPWORDS.put("essa", null);
//        STOPWORDS.put("essas", null);
//        STOPWORDS.put("esse", null);
//        STOPWORDS.put("esses", null);
//        STOPWORDS.put("esta", null);
//        STOPWORDS.put("esta", null);
//        STOPWORDS.put("estamos", null);
//        STOPWORDS.put("estao", null);
//        STOPWORDS.put("estas", null);
//        STOPWORDS.put("estava", null);
//        STOPWORDS.put("estavam", null);
//        STOPWORDS.put("estavamos", null);
//        STOPWORDS.put("este", null);
//        STOPWORDS.put("estes", null);
//        STOPWORDS.put("estou", null);
//        STOPWORDS.put("eu", null);
//        STOPWORDS.put("fazendo", null);
//        STOPWORDS.put("fazer", null);
//        STOPWORDS.put("feita", null);
//        STOPWORDS.put("feitas", null);
//        STOPWORDS.put("feito", null);
//        STOPWORDS.put("feitos", null);
//        STOPWORDS.put("foi", null);
//        STOPWORDS.put("for", null);
//        STOPWORDS.put("foram", null);
//        STOPWORDS.put("fosse", null);
//        STOPWORDS.put("fossem", null);
//        STOPWORDS.put("grande", null);
//        STOPWORDS.put("grandes", null);
//        STOPWORDS.put("ha", null);
//        STOPWORDS.put("isso", null);
//        STOPWORDS.put("isto", null);
//        STOPWORDS.put("ja", null);
//        STOPWORDS.put("la", null);
//        STOPWORDS.put("lhe", null);
//        STOPWORDS.put("lhes", null);
//        STOPWORDS.put("lo", null);
//        STOPWORDS.put("mas", null);
//        STOPWORDS.put("me", null);
//        STOPWORDS.put("mesma", null);
//        STOPWORDS.put("mesmas", null);
//        STOPWORDS.put("mesmo", null);
//        STOPWORDS.put("mesmos", null);
//        STOPWORDS.put("meu", null);
//        STOPWORDS.put("meus", null);
//        STOPWORDS.put("minha", null);
//        STOPWORDS.put("minhas", null);
//        STOPWORDS.put("muita", null);
//        STOPWORDS.put("muitas", null);
//        STOPWORDS.put("muito", null);
//        STOPWORDS.put("muitos", null);
//        STOPWORDS.put("na", null);
//        STOPWORDS.put("nao", null);
//        STOPWORDS.put("nas", null);
//        STOPWORDS.put("nem", null);
//        STOPWORDS.put("nenhum", null);
//        STOPWORDS.put("nessa", null);
//        STOPWORDS.put("nessas", null);
//        STOPWORDS.put("nesta", null);
//        STOPWORDS.put("nestas", null);
//        STOPWORDS.put("ninguem", null);
//        STOPWORDS.put("no", null);
//        STOPWORDS.put("nos", null);
//        STOPWORDS.put("nossa", null);
//        STOPWORDS.put("nossas", null);
//        STOPWORDS.put("nosso", null);
//        STOPWORDS.put("nossos", null);
//        STOPWORDS.put("num", null);
//        STOPWORDS.put("numa", null);
//        STOPWORDS.put("nunca", null);
//        STOPWORDS.put("o", null);
//        STOPWORDS.put("os", null);
//        STOPWORDS.put("ou", null);
//        STOPWORDS.put("outra", null);
//        STOPWORDS.put("outras", null);
//        STOPWORDS.put("outro", null);
//        STOPWORDS.put("outros", null);
//        STOPWORDS.put("para", null);
//        STOPWORDS.put("pela", null);
//        STOPWORDS.put("pelas", null);
//        STOPWORDS.put("pelo", null);
//        STOPWORDS.put("pelos", null);
//        STOPWORDS.put("pequena", null);
//        STOPWORDS.put("pequenas", null);
//        STOPWORDS.put("pequeno", null);
//        STOPWORDS.put("pequenos", null);
//        STOPWORDS.put("per", null);
//        STOPWORDS.put("perante", null);
//        STOPWORDS.put("pode", null);
//        STOPWORDS.put("podendo", null);
//        STOPWORDS.put("poder", null);
//        STOPWORDS.put("poderia", null);
//        STOPWORDS.put("poderiam", null);
//        STOPWORDS.put("podia", null);
//        STOPWORDS.put("podiam", null);
//        STOPWORDS.put("pois", null);
//        STOPWORDS.put("por", null);
//        STOPWORDS.put("porem", null);
//        STOPWORDS.put("porque", null);
//        STOPWORDS.put("posso", null);
//        STOPWORDS.put("pouca", null);
//        STOPWORDS.put("poucas", null);
//        STOPWORDS.put("pouco", null);
//        STOPWORDS.put("poucos", null);
//        STOPWORDS.put("primeiro", null);
//        STOPWORDS.put("primeiros", null);
//        STOPWORDS.put("propria", null);
//        STOPWORDS.put("proprias", null);
//        STOPWORDS.put("proprio", null);
//        STOPWORDS.put("proprios", null);
//        STOPWORDS.put("quais", null);
//        STOPWORDS.put("qual", null);
//        STOPWORDS.put("quando", null);
//        STOPWORDS.put("quanto", null);
//        STOPWORDS.put("quantos", null);
//        STOPWORDS.put("que", null);
//        STOPWORDS.put("quem", null);
//        STOPWORDS.put("sao", null);
//        STOPWORDS.put("se", null);
//        STOPWORDS.put("seja", null);
//        STOPWORDS.put("sejam", null);
//        STOPWORDS.put("sem", null);
//        STOPWORDS.put("sempre", null);
//        STOPWORDS.put("sendo", null);
//        STOPWORDS.put("sera", null);
//        STOPWORDS.put("serao", null);
//        STOPWORDS.put("seu", null);
//        STOPWORDS.put("seus", null);
//        STOPWORDS.put("si", null);
//        STOPWORDS.put("sido", null);
//        STOPWORDS.put("so", null);
//        STOPWORDS.put("sob", null);
//        STOPWORDS.put("sobre", null);
//        STOPWORDS.put("sua", null);
//        STOPWORDS.put("suas", null);
//        STOPWORDS.put("talvez", null);
//        STOPWORDS.put("tambem", null);
//        STOPWORDS.put("tampouco", null);
//        STOPWORDS.put("te", null);
//        STOPWORDS.put("tem", null);
//        STOPWORDS.put("tendo", null);
//        STOPWORDS.put("tenha", null);
//        STOPWORDS.put("ter", null);
//        STOPWORDS.put("teu", null);
//        STOPWORDS.put("teus", null);
//        STOPWORDS.put("ti", null);
//        STOPWORDS.put("tido", null);
//        STOPWORDS.put("tinha", null);
//        STOPWORDS.put("tinham", null);
//        STOPWORDS.put("toda", null);
//        STOPWORDS.put("todas", null);
//        STOPWORDS.put("todavia", null);
//        STOPWORDS.put("todo", null);
//        STOPWORDS.put("todos", null);
//        STOPWORDS.put("tu", null);
//        STOPWORDS.put("tua", null);
//        STOPWORDS.put("tuas", null);
//        STOPWORDS.put("tudo", null);
//        STOPWORDS.put("ultima", null);
//        STOPWORDS.put("ultimas", null);
//        STOPWORDS.put("ultimo", null);
//        STOPWORDS.put("ultimos", null);
//        STOPWORDS.put("um", null);
//        STOPWORDS.put("uma", null);
//        STOPWORDS.put("umas", null);
//        STOPWORDS.put("uns", null);
//        STOPWORDS.put("vendo", null);
//        STOPWORDS.put("ver", null);
//        STOPWORDS.put("vez", null);
//        STOPWORDS.put("vindo", null);
//        STOPWORDS.put("vir", null);
//        STOPWORDS.put("vos", null);
//        STOPWORDS.put("vos", null);
//        
//        STOPWORDS.put("simposio", null);
//        STOPWORDS.put("brasileiro", null);
//        STOPWORDS.put("congresso", null);
//        STOPWORDS.put("workshop", null);
//        STOPWORDS.put("i", null);
//        STOPWORDS.put("ii", null);
//        STOPWORDS.put("iii", null);
//        STOPWORDS.put("iv", null);
//        STOPWORDS.put("v", null);
//        STOPWORDS.put("vi", null);
//        STOPWORDS.put("vii", null);
//        STOPWORDS.put("viii", null);
//        STOPWORDS.put("ix", null);
//        STOPWORDS.put("x", null);
//        STOPWORDS.put("xi", null);
//        STOPWORDS.put("xii", null);
//        STOPWORDS.put("xiii", null);
//        STOPWORDS.put("xiv", null);
//        STOPWORDS.put("xv", null);
//        STOPWORDS.put("xvi", null);
//        STOPWORDS.put("xvii", null);
//        STOPWORDS.put("xviii", null);
//        STOPWORDS.put("xix", null);
//        STOPWORDS.put("xx", null);
//        STOPWORDS.put("xxi", null);
//        STOPWORDS.put("xxii", null);
//        STOPWORDS.put("xxiii", null);
//        STOPWORDS.put("xxiv", null);
//        STOPWORDS.put("xxv", null);
//        STOPWORDS.put("xxvi", null);
//        STOPWORDS.put("xxvii", null);
//        STOPWORDS.put("xxviii", null);
//        STOPWORDS.put("xxivx", null);
//        
//        //STOPWORDS.put("computadores", null);        
//        //STOPWORDS.put("computacao", null);
//        STOPWORDS.put("volume", null);
//        STOPWORDS.put("1", null);
//        STOPWORDS.put("2", null);
//        STOPWORDS.put("3", null);
//        STOPWORDS.put("4", null);
//        STOPWORDS.put("5", null);
//        STOPWORDS.put("6", null);
//        STOPWORDS.put("7", null);
//        STOPWORDS.put("8", null);
//        STOPWORDS.put("9", null);
//        STOPWORDS.put("11", null);
//        STOPWORDS.put("12", null);
//        STOPWORDS.put("13", null);
//        STOPWORDS.put("14", null);
//        STOPWORDS.put("15", null);
//        STOPWORDS.put("16", null);
//        STOPWORDS.put("17", null);
//        STOPWORDS.put("18", null);
//        STOPWORDS.put("19", null);
//        STOPWORDS.put("20", null);
//        STOPWORDS.put("21", null);
//        STOPWORDS.put("22", null);
//        STOPWORDS.put("23", null);
//        STOPWORDS.put("24", null);
//        STOPWORDS.put("25", null);
//        STOPWORDS.put("26", null);
//        STOPWORDS.put("27", null);
        
        //STOPWORDS.put("computer", null);
        //STOPWORDS.put("ieee", null);
        //STOPWORDS.put("system", null);
        //STOPWORDS.put("proceed", null);
        //STOPWORDS.put("symposium", null);
        //STOPWORDS.put("journal", null);
        //STOPWORDS.put("design", null);
        
        
        /*
        STOPWORDS.clear();
        try {
            try (BufferedReader b = new BufferedReader(new FileReader("stoplist.txt"))) {
                String s;
                while ((s=b.readLine())!=null){
                    STOPWORDS.put(s.split("[ |]")[0].trim(), null);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        
        STOPWORDS_T.put("algorithm", null);
        STOPWORDS_T.put("model", null);
        STOPWORDS_T.put("network", null);
        STOPWORDS_T.put("imag", null);
        STOPWORDS_T.put("comput", null);
        STOPWORDS_T.put("detect", null);
        STOPWORDS_T.put("of", null);
        STOPWORDS_T.put("some", null);
        STOPWORDS_T.put("any", null);
        STOPWORDS_T.put("by", null);
        STOPWORDS_T.put("in", null);
        STOPWORDS_T.put("for", null);
        STOPWORDS_T.put("and", null);
        STOPWORDS_T.put("to", null);
        STOPWORDS_T.put("or", null);
        STOPWORDS_T.put("the", null);
        STOPWORDS_T.put("a", null);
        STOPWORDS_T.put("an", null);
        STOPWORDS_T.put("with", null);
        STOPWORDS_T.put("over", null);
        STOPWORDS_T.put("under", null);
        STOPWORDS_T.put("on", null);
        STOPWORDS_T.put("about", null);
        STOPWORDS_T.put("into", null);

        STOPWORDS_V.put("comput", null);
        STOPWORDS_V.put("intern", null);
        STOPWORDS_V.put("ieee", null);
        STOPWORDS_V.put("system", null);
        STOPWORDS_V.put("proceed", null);
        STOPWORDS_V.put("symposium", null);
        STOPWORDS_V.put("journal", null);
        STOPWORDS_V.put("design", null);
        STOPWORDS_V.put("autom", null);
        STOPWORDS_V.put("aid", null);
    }
    
    public static String removePunctuation(String str){
        str = Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        
        StringTokenizer st = new StringTokenizer(str, " \r\n\t#|_;.,?!-()[]}{:`'\"*&$%><=~^/");
	    int t = st.countTokens();
        if (t > 0){
            str = st.nextToken();
            for (int i=1; i<t; i++){
                String p = st.nextToken();
                if (p.length() > 0){
                    str += " "+ p;
                }
            }
        }
        
        return str;
    }
    
    public static String removeDoubleSpace(String str){
        return str.replaceAll("\\s\\s+", " ");
    }
    
    public static String[] split(String str, String token){
        StringTokenizer st = new StringTokenizer(str, token);
        int t = st.countTokens();
        String[] pieces = new String[t];
        for (int i=0; i<t; i++){
            pieces[i] = st.nextToken();
        }
        return pieces;
    }
    
    public static String implode(String separator, Iterator<?> it){
        StringBuilder sb = new StringBuilder();
        if (it.hasNext()){
            sb.append(it.next());
            while (it.hasNext()){
                sb.append(separator).append(it.next());
            }
        }

        return sb.toString();
    }
    
    public static String removeStopWords(String str){
        StringTokenizer st = new StringTokenizer(str, " ");
        StringBuilder sb = new StringBuilder();
        int t = st.countTokens();
        int i = 0;
        while (i < t){
            i++;
            String aux = st.nextToken();
            if (! STOPWORDS.containsKey(aux)){
                sb.append(aux);
                break;
            }
        }
        for (; i<t; i++){
            String aux = st.nextToken();
            if (! STOPWORDS.containsKey(aux)){
                sb.append(" ").append(aux);
            }
        }
        return sb.toString();
    }
    
    public static String removeStopWordsV(String str){
        StringTokenizer st = new StringTokenizer(str, " ");
        StringBuilder sb = new StringBuilder();
        int t = st.countTokens();
        int i = 0;
        while (i < t){
            i++;
            String aux = st.nextToken();
            if (! STOPWORDS_V.containsKey(aux)){
                sb.append(aux);
                break;
            }
        }
        for (; i<t; i++){
            String aux = st.nextToken();
            if (! STOPWORDS_V.containsKey(aux)){
                sb.append(" ").append(aux);
            }
        }
        return sb.toString();
    }
    
    public static String removeStopWordsT(String str){
        StringTokenizer st = new StringTokenizer(str, " ");
        StringBuilder sb = new StringBuilder();
        int t = st.countTokens();
        int i = 0;
        while (i < t){
            i++;
            String aux = st.nextToken();
            if (! STOPWORDS_T.containsKey(aux)){
                sb.append(aux);
                break;
            }
        }
        for (; i<t; i++){
            String aux = st.nextToken();
            if (! STOPWORDS_T.containsKey(aux)){
                sb.append(" ").append(aux);
            }
        }
        return sb.toString();
    }
    
    public static void shuffle(Object[] vet){
        Random random = new Random();
        int t = vet.length;
        for (int i=0; i<t; i++){
            int p1 = random.nextInt(t);
            int p2 = random.nextInt(t);
            Object aux = vet[p1];
            vet[p1] = vet[p2];
            vet[p2] = aux;
        }
    }
    
    public static void shuffle(Object[] vet, Random random){
        int t = vet.length;
        for (int i=0; i<t; i++){
            int p1 = i;
            int p2 = random.nextInt(t - i) + i;
            Object aux = vet[p1];
            vet[p1] = vet[p2];
            vet[p2] = aux;
        }
    }
    
    public static String stemming(String str) {
		Stemmer stemmer = new Stemmer();
		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(str, " ");
        if (st.hasMoreTokens()) {
            String t = st.nextToken();
            stemmer.add(t.toCharArray(), t.length());
            stemmer.stem();
            sb.append(stemmer.toString());
            while (st.hasMoreTokens()) {
                t = st.nextToken();
                stemmer.add(t.toCharArray(), t.length());
                stemmer.stem();
                sb.append(" ").append(stemmer.toString());
            }
        }
		return sb.toString();
	}
    
    public static double round(double v, int precision){
        double f = Math.pow(10, precision);
        return Math.round(v*f) / f;
    }
}
