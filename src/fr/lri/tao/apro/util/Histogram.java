package fr.lri.tao.apro.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.*;

/** Counts instances of T */
public class Histogram<T> {

  Map<T, Integer> map = new HashMap<T, Integer>();
  
  public Histogram() {    
  }
  
  public Histogram(Collection<T> collection) {
    this.add(collection);
  }
  
  
  public void add(Collection<T> collection) {
    for (T ranking: collection) {
      this.add(ranking);
    }
  }
  
  public Integer get(T key) {
    return map.get(key);
  }
  
  public void add(T key) {
    int i = count(key);
    map.put(key, i+1);    
  }
  
  public int count(T key) {
    Integer i = map.get(key);
    if (i == null) i = 0;
    return i;
  }
  
  public T getMostFrequent() {
    int max = 0;
    T key = null;
    for (T r: map.keySet()) {
      int c = map.get(r);
      if (c > max) {
        max = c;
        key = r;
      }
    }
    return key;
  }
  
  public void output(OutputStream os) throws IOException {
    // FileOutputStream fos = new FileOutputStream("hashmap.ser");
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(map);
    oos.close();
  }
  
  public void input(InputStream is) throws IOException {
    ObjectInputStream ois = new ObjectInputStream(is);
    try { map = (HashMap) ois.readObject(); } 
    catch (ClassNotFoundException ex) { }
    ois.close();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(T t: map.keySet()) {
      sb.append(t).append(": ").append(map.get(t)).append("\n");
    }
    return sb.toString();
  }

  public int size() {
    return map.size();
  }
  
}
