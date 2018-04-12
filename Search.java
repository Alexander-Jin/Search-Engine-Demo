import java.io.*;
import java.util.*;
import java.util.regex.*;

//Search uses BM-25 algorithm
public class Search{
  public static class Point{
    double score;
    int documentIndex;
    public Point(double score, int documentIndex){
      this.score = score;
      this.documentIndex = documentIndex;
    }
  }
  
  @SuppressWarnings("unchecked")
	public static void main(String[] args){
    HashMap<String, PostingList> invertedIndex = null;
    List<String> indexToDocument = null;
    List<Integer> documentLength = null;
    HashSet<String> stopWordSet = new HashSet<String>();
    double avgdl = 0.0;
    try{
      FileInputStream fileIn = new FileInputStream("indexing/invertedIndex");
      ObjectInputStream in = new ObjectInputStream(fileIn);
      invertedIndex = (HashMap<String,PostingList>) in.readObject();
      in.close();
      fileIn.close();

      fileIn = new FileInputStream("indexing/indexToDocument");
      in = new ObjectInputStream(fileIn);
      indexToDocument = (List<String>) in.readObject();
      in.close();
      fileIn.close();

      fileIn = new FileInputStream("indexing/documentLength");
      in = new ObjectInputStream(fileIn);
      documentLength = (List<Integer>) in.readObject();
      in.close();
      fileIn.close();
      
      BufferedReader br = new BufferedReader(new FileReader("stopWord.txt"));
      String line = br.readLine();
      while (line != null){
        stopWordSet.add(line.trim());
        line = br.readLine();
      }
      br.close();

      br = new BufferedReader(new FileReader("indexing/avgdl.txt"));
      avgdl = Double.parseDouble(br.readLine());
      br.close();
    }
    catch (Exception e){
      e.printStackTrace();
    }
    Stemmer stemmer = new Stemmer();
    Pattern pattern = Pattern.compile("(:?^[\\d]+(:?\\.[\\d]+)?$|[\\w]{3,})");

    String query = "music";
    if (args.length > 0) query = args[0];
    System.out.println("query: " + query);
    System.out.println("-----");

    String[] queryTokens = query.split(" ");
    List<String> tokens = new ArrayList<String>();
    for (String s: queryTokens){
      if (!stopWordSet.contains(s)){
        Matcher m = pattern.matcher(s);
        if (!m.find()) continue;
        String token = m.group(0);
        stemmer.add(token);
        stemmer.stem();
        token = stemmer.toString();
        tokens.add(token);
      }
    }

    //simplified version of BM-25 algorithm
    HashMap<Integer, Double> scoreMap = new HashMap<Integer, Double>();
    int N = indexToDocument.size();
    double k1 = 2.0;
    double b = 0.75;
    for (String token: tokens){
      if (!invertedIndex.containsKey(token)) continue;
      int n = invertedIndex.get(token).documentFrequency;
      double w = Math.log((N - n + 0.5) / (n + 0.5));
      HashMap<Integer, Integer> tfMap = invertedIndex.get(token).termFrequencyMap;
      for (int documentIndex: tfMap.keySet()){
        int f = tfMap.get(documentIndex);
        int dl = documentLength.get(documentIndex);
        double K = k1 * (1 - b + b * dl/avgdl);
        double R = f * (k1 + 1) / (f + K);
        scoreMap.put(documentIndex, scoreMap.getOrDefault(documentIndex, 0.0) + R * w);
      }
    }

    PriorityQueue<Point> q = new PriorityQueue<Point>(new Comparator<Point>(){
      public int compare(Point p1, Point p2){
        if (p2.score - p1.score < 0) return -1;
        else if (p2.score - p1.score > 0) return 1;
        return 0;
      }
    });

    for (Integer documentIndex: scoreMap.keySet()){
      q.offer(new Point(scoreMap.get(documentIndex), documentIndex));
    }

    System.out.println("Search Result by document name:");
    int numTop = 5;
    while (!q.isEmpty() && numTop > 0){
      System.out.println(indexToDocument.get(q.poll().documentIndex));
    }
	}
}