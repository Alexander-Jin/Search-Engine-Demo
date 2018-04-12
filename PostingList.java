import java.util.*;

public class PostingList implements java.io.Serializable{
  String word;
  int documentFrequency;
  HashMap<Integer, Integer> termFrequencyMap;

  public PostingList(String word){
    this.word = word;
    this.documentFrequency = 0;
    this.termFrequencyMap = new HashMap<Integer, Integer>();
  }

  public void add(int documentIndex){
    termFrequencyMap.put(documentIndex, termFrequencyMap.getOrDefault(documentIndex, 0) + 1);
  }

  public String toString(){
    return termFrequencyMap.toString();
  }
}