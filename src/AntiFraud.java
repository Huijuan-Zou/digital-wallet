package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * @author Huijuan Zou
 * The class AntiFraud is built to analyze whether a transaction is safe or not based on previous
 * transactions. According to different degree of connection, 
 */
public class AntiFraud {
  private HashMap<String, HashSet<String>> adjacentLists;

  public AntiFraud() {
    adjacentLists = new HashMap<String, HashSet<String>>();
  }

  /**
   * @param input input file path.
   * @return buffered reader of the input file.
   */
  private BufferedReader getReader(String input) {
    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(input));
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("File Address not correct.");
    }
    return reader;
  }

  /**
   * @param input input file path of source data (completed transactions) to help 
   *        in classifying ongoing transaction as trusted or unverified.
   */
  public void parseInput(String input) {
    BufferedReader reader = getReader(input);
    String line;
    try {
      //remove header line
      line = reader.readLine();
      while ((line = reader.readLine()) != null) {
        String[] cols = line.split(",");
        if (cols.length <  1) {
          continue;
        }
        String time[] = cols[0].split(" ");
        //check if the line input is regular input but not comments.
        if (time.length < 1 || !time[0].matches("\\d{4}-\\d{2}-\\d{2}")) {
          continue;
        }
        String key1 = cols[1].trim();
        String key2 = cols[2].trim();

        if (!adjacentLists.containsKey(key1)) {
          HashSet<String> adjList = new HashSet<String>();
          adjList.add(key2);
          adjacentLists.put(key1, adjList);
        } else {
          HashSet<String> adjList = adjacentLists.get(key1);
          if (!adjList.contains(key2)) {
            adjList.add(key2);
          }
          adjacentLists.put(key1, adjList);
        }
        if (!adjacentLists.containsKey(key2)) {
          HashSet<String> adjList = new HashSet<String>();
          adjList.add(key1);
          adjacentLists.put(key2, adjList);
        } else {
          HashSet<String> adjList = adjacentLists.get(key2);
          if (!adjList.contains(key1)) {
            adjList.add(key1);
          }
          adjacentLists.put(key2, adjList);
        } 
      } 
    } catch (IOException e) {
      throw new IllegalArgumentException("Error in reading line.");
    }
    try {
      reader.close();
    } catch (IOException e) {
      throw new IllegalArgumentException("Error in closing buffer reader.");
    }
  }

  /**
   * @param root the first id of the two ids related in an transaction to be classified.
   * @param queue queue to help in performing BFS search.
   * @param edges edges to help in performing BFS search.
   * @param degree max degree allowed to be classified as trusted.
   * @param target the second id of the two ids related in an transaction to be classified.
   * @return boolean value indicating whether the transaction is trusted.
   */
  public boolean BFS(String root, LinkedList<String> queue,HashMap<Tuple, Integer> edges, int degree, String target) {
    Tuple rootTp = new Tuple(root,root);
    edges.put(rootTp, 0);
    queue.addLast(root);

    while(!queue.isEmpty()) {
      String curNode = queue.removeFirst();
      int oldDist = edges.get(new Tuple(root, curNode));
      for  (String node : adjacentLists.get(curNode)) {
        Tuple nTuple = new Tuple(root, node);
        if (!edges.containsKey(nTuple)) {
          edges.put(nTuple, oldDist + 1);
          if (oldDist + 1 > degree) {
            queue.clear();
            edges.clear();
            return false;
          } 
          if (node.equals(target)) {
            queue.clear();
            edges.clear();
            return true;
          }
          queue.addLast(node);
        }
      } 
    }
    queue.clear();
    edges.clear();
    return false;
  }

  /**
   * Initialize input and output file. Loop through all the client ids,
   * write to file the result to indicate whether a transaction is trusted 
   * by calling BFS.
   * @param degree maximum degree allowed to classify a safe transaction.
   * @param fileName output file name or file path.
   * @param input input file path
   */
  public void getShortestPath(int degree,String fileName, String input) {
    LinkedList<String> queue = new LinkedList<String>();
    HashMap<Tuple, Integer> edges = new HashMap<Tuple, Integer>();
    
    File file = new File(fileName);
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new IllegalArgumentException("Error in creating file.");
      }
    }
    FileWriter fw = null;
    try {
      fw = new FileWriter(file.getAbsoluteFile());
    } catch (IOException e) {
      throw new IllegalArgumentException("Error in creating file writer.");
    }
    BufferedWriter  bw = new BufferedWriter(fw);
    BufferedReader reader = getReader(input);
    String line;
    try {
      line = reader.readLine();
      while ((line = reader.readLine()) != null) {
        String[] cols = line.split(",");
        if (cols.length <  1) {
          continue;
        }
        String time[] = cols[0].split(" ");
        if (time.length < 1 || !time[0].matches("\\d{4}-\\d{2}-\\d{2}")) {
          continue;
        }
        String key1 = cols[1].trim();
        String key2 = cols[2].trim();
        if (adjacentLists.containsKey(key1)) {
          //call BFS on first client of each client-couple to be identified
          boolean res = BFS(key1, queue, edges, degree, key2);
          if (res) {
            bw.write("trusted");
            bw.newLine();
          } else {
            bw.write("unverified");
            bw.newLine();
          }   
        } else {
          bw.write("unverified");
          bw.newLine();
        }
      } 
    } catch (IOException e) {
      try {
        bw.close();
      } catch (IOException e1) {
        throw new IllegalArgumentException("Unable to close buffer writer.");
      }
      throw new IllegalArgumentException("Error in reading line.");
    }
    try {
      bw.close();
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to close buffer writer.");
    }
    try {
      reader.close();
    } catch (IOException e) {
      throw new IllegalArgumentException("Error in closing buffer reader.");
    }
  }

  /**
   * output boolean value indicating whether a transaction is safe assuming only first degree 
   * connection is safe.
   * @param input path of input file to be analyzed
   */
    public void getFeature1(String input, String output) {
    getShortestPath(1, output, input);
  }
  
  /**
   * output boolean value indicating whether a transaction is safe assuming only first two 
   * degree connections are safe.
   * @param input path of input file to be analyzed
   */
  public void getFeature2(String input,String output) {
    getShortestPath(2, output, input);
  }
  
  /**
   * output boolean value indicating whether a transaction is safe assuming only first four 
   * degree connections are safe.
   * @param input path of input file to be analyzed
   */
  public void getFeature3(String input,String output) {
    getShortestPath(4, output, input);
  }
  
  /**
   * possible connection of two clients.
   * id is unique for each client.
   * @id1 id of client one 
   * @id2 id of client two
   */
  protected class Tuple{
    private String id1;
    private String id2;
    public Tuple(String id1, String id2) {
      this.id1 = id1;
      this.id2 = id2;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((id1 == null) ? 0 : id1.hashCode());
      result = prime * result + ((id2 == null) ? 0 : id2.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof Tuple)) {
        return false;
      }
      Tuple user = (Tuple) o;
      if (!user.id1.equals(this.id1)) return false;
      if (!user.id2.equals(this.id2)) return false; 
      return true;
    }
    private AntiFraud getOuterType() {
      return AntiFraud.this;
    }
  }
}
