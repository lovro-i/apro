package fr.lri.tao.apro.ap;


public class Group {

  final int gid;
  private final Apro apro;
  
  final int startIndex;
  final int endIndex;
  
  int n, size;
  
  int[] lengths;
  int[] t;
  int[][] indices;
  
  double[] sums;
  
  double[][] s;
  double[][] a;
  
  
  Group(Apro apro, int gid) {
    this.gid = gid;
    this.apro = apro;
    this.startIndex = apro.startIndex(gid);
    this.endIndex = apro.endIndex(gid);
        
    this.n = apro.getN();
    sums = new double[n];
    this.size = endIndex-startIndex;
    
    
    lengths = new int[size];
    t = new int[size];
    indices = new int[size][];
    
    s = new double[size][];    
    a = new double[size][];
  }
  
  public boolean containsNode(int nid) {
    return (startIndex <= gid) && (gid < endIndex);
  }
  
  @Override
  public String toString() {
    return "Group " + gid + " [" + startIndex + ", " + endIndex + ")";
  }
  
  double max1, max2;
  int forK;
  
  
  public int getId() {
    return gid;
  }
  
  private void calcMax(int i) {
    max1 = max2 = Double.NEGATIVE_INFINITY;
    for (int j=0; j<lengths[i]; j++) {
      double t = a[i][j] + s[i][j];
      if (t > max1) {
        max2 = max1;
        max1 = t;
        forK = indices[i][j];
      }
      else if (t >= max2) {
        max2 = t;
      }
    }
  }
  
  private final double max(int k) {
    if (k == forK) return max2;
    else return max1;
  }
 
  int getNumaNode() {
    return apro.getNumaNode(gid);
  }
  
  
  void computeResponsibilities() {
    double damp = apro.getDamping();    
    for (int i=0; i<size; i++) {
      int i1 = i+startIndex;
      this.calcMax(i);
      // System.out.println(String.format("%d, %f, %f", forK, max1, max2));
      double temp;
      for (int j=0; j<lengths[i]; j++) {
        int k = indices[i][j];
        temp = s[i][j] - max(k);
        
        double oldValue = Math.max(0, apro.r[i1][k]);        
        apro.r[i1][k] = (1-damp) * temp + damp * apro.r[i1][k];
        double newValue = Math.max(0, apro.r[i1][k]);
        
        sums[k] += newValue - oldValue;
      }
    }
    
    apro.updateSums(this);
  }



  
  private double sumR(int i, int k) {
    if (i != k) return apro.sumr[k] - Math.max(0, apro.r[i][k]) - Math.max(0, apro.r[k][k]);
    else return apro.sumr[k] - Math.max(0, apro.r[k][k]);
  }
  
  private double sumRBasic(int i, int k) {
    double sum = 0;
    for (int j=0; j<n; j++) {
      if (j != i && j != k) sum += Math.max(0, apro.r[j][k]);
    }
    return sum;
  }
  
  
  void computeAvailabilities() {
    double damp = apro.getDamping();
    double temp;
    for (int i=0; i<size; i++) {
      int len = lengths[i];
      for (int j=0; j<len; j++) {
        int k = indices[i][j];
        if (i+startIndex != k) {
          //!!double t = apro.r.get(k, k) + sumR3(i+startIndex, k);
          double t = apro.r[k][k] + sumR(i+startIndex, k);
          temp = Math.min(0, t);
        }
        else {
          temp = sumR(k, k);
        }
        a[i][j] = (1-damp) * temp + damp * a[i][j];
        // System.out.println(String.format("a[%d][%d] = %f", i, j, a[i][j]));
      }
    }
  }


  
}







