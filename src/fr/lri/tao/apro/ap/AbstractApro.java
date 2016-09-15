package fr.lri.tao.apro.ap;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import java.util.HashSet;
import java.util.Set;

/** Used as a base class for different Affinity Propagation implementations */
public abstract class AbstractApro {

  public abstract int getN();
  
  public abstract DoubleMatrix2D getR();
  public abstract DoubleMatrix2D getA();
  public abstract DoubleMatrix2D getS();
  
  private DoubleMatrix2D ar;
  private int[] exemplars;
  
  
  /** Sum of responsibilities and availabilities. Used for find an exemplar (argmax)
   * @return Matrix of responsibilities and availabilities summed together
   */
  public synchronized DoubleMatrix2D getAR() {
    if (ar != null) return ar;

    int n = getN();
    DoubleMatrix2D s = getS();
    DoubleMatrix2D a = getA();
    DoubleMatrix2D r = getR();
    
    if (s instanceof SparseDoubleMatrix2D) ar = new SparseDoubleMatrix2D(n, n);
    else ar = new DenseDoubleMatrix2D(n, n);
    
    IntArrayList is = new IntArrayList();
    IntArrayList ks = new IntArrayList();
    DoubleArrayList vs = new DoubleArrayList();            
    s.getNonZeros(is, ks, vs);
    
    for (int j=0; j<is.size(); j++) {
      int i = is.get(j);
      int k = ks.get(j);
      double v = a.getQuick(i, k) + r.getQuick(i, k);
      ar.setQuick(i, k, v);
    }    
    
    return ar;
  }
  
  
  public void clear() {
    this.ar = null;
  }
  
  
  /** Get the collection of exemplars
   * @return Set of exemplar IDs */
  public synchronized Set<Integer> getExemplarSet() {
    Set<Integer> exes = new HashSet<Integer>();
    
    /** Positive values on the main diagonal *
    DoubleMatrix2D ar = this.getAR();
    for (int i = 0; i < getN(); i++) {
      if (ar.get(i, i) > 0) exes.add(i);
    }
    */ 
    
    int[] exemplars = getExemplars();
    for (int i = 0; i < exemplars.length; i++) {
      exes.add(exemplars[i]);      
    }
    
    return exes;
  }
  
  /** The result of the clustering 
    * @return Array of indexes of exemplars.
    * idx[j] == j indicates data point j is itself an exemplar. */
  public synchronized int[] getExemplars() {
    if (exemplars != null) return exemplars;
    getAR();
    
    int n = getN();
    
    IntArrayList is = new IntArrayList();
    IntArrayList ks = new IntArrayList();
    DoubleArrayList vs = new DoubleArrayList();            
    ar.getNonZeros(is, ks, vs);
    

    exemplars = new int[n];
    double[] maxs = new double[n];
    for (int i=0; i<n; i++) {
      exemplars[i] = -1;
      maxs[i] = Double.NEGATIVE_INFINITY;
    }
        
    for (int j=0; j<is.size(); j++) {
      int i = is.get(j);
      double v = vs.get(j);
      if (v > maxs[i]) {
        maxs[i] = v;
        exemplars[i] = ks.get(j);
      }
    }    

    return exemplars;
  }
  
  /** Exemplar of an element
   * @param Node (element) index
   * @return Index of node's exemplar
   */
  public synchronized int getExemplar(int node) {    
    int[] exemplars = getExemplars();
    return exemplars[node];
  }
    
}
