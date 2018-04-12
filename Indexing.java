import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Indexing{
	public static void main(String[] args){
    String path = "documents";
    if (args.length > 0) path = args[0];

    //Generate stop word set. stop word list found in http://xpo6.com/list-of-english-stop-words/
    HashSet<String> stopWordSet = new HashSet<String>();
    try{
      BufferedReader br = new BufferedReader(new FileReader("stopWord.txt"));
      String line = br.readLine();
      while (line != null){
        stopWordSet.add(line.trim());
        line = br.readLine();
      }
      br.close();
    }
    catch (Exception e){
      e.printStackTrace();
    }

    //Indexing
    Stemmer stemmer = new Stemmer();
    Pattern pattern = Pattern.compile("(:?^[\\d]+(:?\\.[\\d]+)?$|[\\w]{3,})");
    List<String> indexToDocument = new ArrayList<String>();
    List<Integer> documentLength = new ArrayList<Integer>();
    HashMap<String, PostingList> invertedIndex = new HashMap<String, PostingList>();
    double avgdl = 0.0;
    try{
      File f = new File(path);
      File[] documents = f.listFiles();
      int documentIndex = 0;
      for (File document: documents){
        if (document.getName().charAt(0) == '.') continue;
        indexToDocument.add(document.getName());
        int numToken = 0;
        BufferedReader br = new BufferedReader(new FileReader(document));
        String line = br.readLine();
        while (line != null){
          line = line.toLowerCase();
          String[] tokens = line.split(" ");
          //remove stop word, remove noise, and stem words
          for (String s: tokens){
            if (!stopWordSet.contains(s)){
              Matcher m = pattern.matcher(s);
              if (!m.find()) continue;
              String token = m.group(0);
              stemmer.add(token);
              stemmer.stem();
              token = stemmer.toString();
              if (invertedIndex.containsKey(token)) invertedIndex.get(token).add(documentIndex);
              else{
                PostingList list = new PostingList(token);
                list.add(documentIndex);
                invertedIndex.put(token, list);
              }
              numToken += 1;
            }
          }
          line = br.readLine();
        }
        documentLength.add(numToken);
        avgdl += (numToken - avgdl) / indexToDocument.size();
        documentIndex += 1;
      }
      /*
      for (String key: invertedIndex.keySet()){
        System.out.println(key);
        System.out.println(invertedIndex.get(key).toString());
      }
      */
    }
    catch (Exception e){
      e.printStackTrace();
    }

    File dir = new File("indexing");
    dir.mkdir();
    try{
      FileOutputStream fileOut = new FileOutputStream("indexing/indexToDocument");
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(indexToDocument);
      out.close();
      fileOut.close();
      
      fileOut = new FileOutputStream("indexing/invertedIndex");
      out = new ObjectOutputStream(fileOut);
      out.writeObject(invertedIndex);
      out.close();
      fileOut.close();
      
      fileOut = new FileOutputStream("indexing/documentLength");
      out = new ObjectOutputStream(fileOut);
      out.writeObject(documentLength);
      out.close();
      fileOut.close();

      PrintWriter writer = new PrintWriter("indexing/avgdl.txt");
      writer.print(avgdl);
      writer.close();
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }
}