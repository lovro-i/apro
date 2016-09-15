package fr.lri.tao.apro.data;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import fr.lri.tao.apro.util.Logger;
import fr.lri.tao.apro.util.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** Provides data from Delimiter Separated Values (DSV) file. Delimiter can be space, tab, comma or semicolon
 *  Similarities and preferences are in separate files
 */
public class DSVProvider implements DataProvider {

  private int n;
  private DoubleMatrix2D s;
  private String delimiters = " \t,;";
  
  /** Load data from two files:
   * @param preferences File containing preferences. Each row contains preference value for the corresponding data instance. Number of rows must correspond to number of instances
   * @param similarities File containing similarities. Each row contains three delimiter separated values: [instance_1_id] [instance_2_id] [similarity]. Instance IDs are 1-based. For 0 based, see other constructor
   * @throws IOException 
   */
  public DSVProvider(File preferences, File similarities) throws IOException {
    this.load(similarities, preferences, 1);
  }
  
  /** Load data from two files:
   * @param preferences File containing preferences. Each row contains preference value for the corresponding data instance. Number of rows must correspond to number of instances
   * @param similarities File containing similarities. Each row contains three delimiter separated values: [instance_1_id] [instance_2_id] [similarity]
   * @param base Specify if instance IDs are 0-base or 1-based
   * @throws IOException 
   */
  public DSVProvider(File preferences, File similarities, int base) throws IOException {
    this.load(similarities, preferences, base);
  }
  
  /** Load data from two files:
   * @param preferences File containing preferences. Each row contains preference value for the corresponding data instance. Number of rows must correspond to number of instances
   * @param similarities File containing similarities. Each row contains three delimiter separated values: [instance_1_id] [instance_2_id] [similarity]
   * @param base Specify if instance IDs are 0-base or 1-based
   * @param delimiters String of delimiter characters
   * @throws IOException 
   */
  public DSVProvider(File preferences, File similarities, int base, String delimiters) throws IOException {
    this.delimiters = delimiters;
    this.load(similarities, preferences, base);
  }
  
  private static List<String> getTokens(String line, String delims) {
    List<String> tokens = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(line, delims);
    while (tokenizer.hasMoreTokens()) { tokens.add(tokenizer.nextToken()); }
    return tokens;
  }
  
  private List<Double> loadPreferences(File preferences) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(preferences));
    List<Double> prefs = new ArrayList<Double>();
    int i = 0;
    String line;
    while ((line = reader.readLine()) != null) {
      prefs.add(Double.parseDouble(line));
    }
    reader.close();
    return prefs;
  }

         
  private void load(File similarities, File preferences, int base) throws IOException {
    // Load preferences
    List<Double> prefs = loadPreferences(preferences);
    this.n = prefs.size();
    
    // Similarity matrix    
    s = new SparseDoubleMatrix2D(n, n);
    
    int c = 0;
    for (Double p: prefs) {
      s.set(c, c, p);
      c++;
    }
    
    
    // Load similarities
    int simCount = 0;
    String line;
    BufferedReader reader = new BufferedReader(new FileReader(similarities));
    while ((line = reader.readLine()) != null) {  
      List<String> tokens = getTokens(line, delimiters);
      int i = Integer.parseInt(tokens.get(0)) - base;
      int j = Integer.parseInt(tokens.get(1)) - base;
      Double v = Double.parseDouble(tokens.get(2));
      s.set(i, j, v);
      simCount++;
    }
    reader.close();
        
    
    double full = 100d * (simCount + n) / (n * n);
    Logger.info("[Loader] Loaded %d similarities, matrix is %f%% populated", simCount, full);
  }
  
  @Override
  public DoubleMatrix2D getMatrix() {
    return s;
  }
  
  @Override
  public int size() {
    return n;
  }

  @Override
  public void addNoise() {
    Utils.addNoise(s);
  }

  
  
}
