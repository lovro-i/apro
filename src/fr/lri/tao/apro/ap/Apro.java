package fr.lri.tao.apro.ap;


import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MATLABProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import fr.lri.tao.apro.util.Histogram;
import fr.lri.tao.apro.util.Logger;
import fr.lri.tao.numa.NUMA;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* This was parallelnuma2 in the previous version 
   Does not create Workers every time, instead reuses them through a queue
*/

/** Sparse matrix Affinity Propagation implementation, uses Colt
 *  Uses NUMA to control worker threads
 */
public class Apro extends AbstractApro {

  List<Group> groups = new ArrayList<Group>();
  private double damp = 0.5d;
  private int n;

  private DataProvider provider;
  private DoubleMatrix2D s;
  double[][] r;
  double[] sumr;
  
  private final int groupCount;
  private Long runTime = null;
  
  private final boolean useNuma;
  private int numaStartNode = 0;
  private Integer numaCoresPerNode = null;
  private Integer numaNumNodes = null;
  
  private boolean debug = true;
  
  
  
  public Apro(DataProvider provider, int groupCount) {
    this(provider, groupCount, false, null, null, null);
  }
  
  public Apro(DataProvider provider, int groupCount, boolean useNuma) {
    this(provider, groupCount, useNuma, null, null, null);
  }
    
  public Apro(DataProvider provider, int groupCount, Integer numaNumNodes, Integer numaCoresPerNode, Integer numaStartNode) {   
    this(provider, groupCount, true, numaNumNodes, numaCoresPerNode, numaStartNode);
  }
    
    
  public Apro(DataProvider provider, int groupCount, boolean useNuma, Integer numaNumNodes, Integer numaCoresPerNode, Integer numaStartNode) {         
    if (useNuma && !NUMA.isAvailable()) {      
      Logger.warn("[NUMA] NUMA library not available. Switching off.");
      this.useNuma = false;
    }
    else {
      this.useNuma = useNuma;    
    }
    
    if (this.useNuma) {
      this.setNumaCoresPerNode(numaCoresPerNode);
      this.setNumaNumNodes(numaNumNodes);
      this.setNumaStartNode(numaStartNode);
      Logger.info("[NUMA] Number of nodes: %d, cores per node: %d, start node: %d", this.numaNumNodes, this.numaCoresPerNode, this.numaStartNode);
    }
    else {
      Logger.warn("[NUMA] NUMA is off");
    }
    
    this.provider = provider;
    n = provider.size();
    this.groupCount = groupCount;
    if (groupCount > n) {
      Logger.warn("Number of required threads is greater than the number of nodes. Setting groupCount to " + n);
      groupCount = n;
    }
  }
    
    
  private void init() {
    long startInit = System.currentTimeMillis();
    
    s = provider.getMatrix();   
    r = new double[n][n];
    sumr = new double[n];
        
    int minSize = Integer.MAX_VALUE;
    int maxSize = Integer.MIN_VALUE;
    
    
    for (int gid=0; gid<groupCount; gid++) {
      if (useNuma) NUMA.allocOnNode(this.getNumaNode(gid));
      Group group = new Group(this, gid);
      groups.add(group);      
      minSize = Math.min(minSize, group.size);
      maxSize = Math.max(maxSize, group.size);
    }
    if (useNuma) NUMA.localAlloc();
    
    
    // populating groups
    IntArrayList is = new IntArrayList();
    IntArrayList ks = new IntArrayList();
    DoubleArrayList vs = new DoubleArrayList();            
    getS().getNonZeros(is, ks, vs);
    
    for (int j=0; j<is.size(); j++) {
      int i = is.get(j);
      Group group = this.getGroup(i);
      group.lengths[i-group.startIndex]++;
    }    

    for (Group group: groups) {
      if (useNuma) NUMA.allocOnNode(group.getNumaNode());
      for (int i=0; i<group.size; i++) {
        int len = group.lengths[i];
        group.indices[i] = new int[len];      
        group.s[i] = new double[len];
        //r1[i] = new double[len];
        group.a[i] = new double[len];
      }
    }
    if (useNuma) NUMA.localAlloc();
    
    for (int j=0; j<is.size(); j++) {
      int i = is.get(j);
      Group group = this.getGroup(i);
      int k = ks.get(j);
      double v = vs.get(j);
      int next = group.t[i-group.startIndex];
      group.indices[i-group.startIndex][next] = k;
      group.s[i-group.startIndex][next] = v;
      group.t[i-group.startIndex]++;        
    }
    
    long initTime = System.currentTimeMillis() - startInit;
    Logger.info("[Init] %d groups created (%d - %d elements each). Init time %d ms", groupCount, minSize, maxSize, initTime);
  }
  
  public void setDebug(boolean debug) {
    this.debug = debug;
  }
  
  /** Set damping factor
   * @param dampingFactor Dampening value between 0 and 1
   */
  public void setDamping(double dampingFactor) {
    this.damp = dampingFactor;
  }
  
  public double getDamping() {
    return damp;
  }
  
  private void setNumaStartNode(Integer node) {
    if (node == null) this.numaStartNode = NUMA.getNode();
    else this.numaStartNode = node;
  }
  
  private void setNumaCoresPerNode(Integer cpn) {
    if (cpn == null) this.numaCoresPerNode = NUMA.getCoresPerNode();
    else this.numaCoresPerNode = cpn;
  }
  
  private void setNumaNumNodes(Integer nn) {    
    if (nn == null) this.numaNumNodes = NUMA.getNumNodes();
    else this.numaNumNodes = nn;
  }
  
  /** @return Node to run this group on */
  Integer getNumaNode(int gid) {    
    int node = gid / numaCoresPerNode;
    node = (node + numaStartNode) % numaNumNodes;
    return node;
  }
  
  
  int startIndex(int gid) {
    return ((n / groupCount) * gid);
  }
  
  
  /** Non-inclusive */
  int endIndex(int gid) {
    int groupSize = n / groupCount;
    int start = groupSize * gid;
    if (groupCount == gid+1) {
      return n;
    }
    if (start + groupSize > n) {
      return n;
    }
    return start + groupSize;
  }
  
  public final int getGroupId(int nodeId) {    
    int size = n / groupCount;
    int gid = nodeId / size;
    if (gid >= groupCount) gid = groupCount-1;
    return gid;
  }
  
  public Group getGroup(int nodeId) {
    Group group = groups.get(getGroupId(nodeId));
    // System.out.println(String.format("For requested node %d returned group %d [%d, %d]", nodeId, group.gid, group.startIndex, group.endIndex));
    return group;
  }
  
  
  
  synchronized void updateSums(Group group) {
    for (int k=0; k<n; k++) {      
      sumr[k] += group.sums[k];
    }
    
    
//    System.out.println("Updating sum from "+group);
//    
//    for (int k=0; k<n; k++) {      
//      System.out.println(String.format("oldSums[%d] = %f", k, group.oldSums[k]));
//      sumrSmart[k] += group.sums[k];
//
//      double bef = sumr[k];
//      sumr[k] = 0;
//      for (int i=0; i<n; i++) {
//        sumr[k] += Math.max(0,r[i][k]);
//        System.out.print("+"+Math.max(0, r[i][k]));
//      }
//      
//      //sumrSmart[k] = sumrSmart[k] - bef;
//      
//      System.out.println(String.format(": sumr[%d] before: %f; after: %f; diff: %f", k, bef, sumr[k], sumr[k] - bef));
//      
//      if (Math.abs(sumrSmart[k] - sumr[k]) > 0.01) System.out.println(String.format("ERROR for k = %d: %f, should be %f (diff %f)", k, sumrSmart[k], sumr[k], sumr[k] - sumrSmart[k]));
//    }
//    
  }

  
  /** The main method for running the Affinity Propagation
   * @param iters Number of iterations to run */
  public void run(int iters) {
    this.init();
    
    long startTime = System.currentTimeMillis();
    List<GroupWorker> workers = new ArrayList<GroupWorker>();
    for (Group group: groups) {
      GroupWorker worker = new GroupWorker(group, useNuma);
      worker.start();
      workers.add(worker);
    }
    
    Logger.info("[Apro] Working...");    
    for (int i=0; i<iters; i++) {
      if (debug && (i + 1) % (iters / 10) == 0) System.out.print('.');
      
      // RESPONSIBILITIES
      Arrays.fill(sumr, 0d);
      for (GroupWorker worker: workers) {
        worker.responsibilities();
      }
      for (GroupWorker worker: workers) {
        worker.waitTask();
      }
      
      // AVAILABILITIES
      for (GroupWorker worker: workers) {
        worker.availabilities();
      }
      for (GroupWorker worker: workers) {
        worker.waitTask();
      }
      
    }
    if (debug) System.out.println();
    
    for (GroupWorker worker: workers) {
      worker.done();
      try { worker.join(); }
      catch (InterruptedException e) {}
    }
    
    long endTime = System.currentTimeMillis();
    this.runTime = endTime - startTime;
    Logger.info("[Result] Groups: %d; Time: %d ms", groupCount, runTime);
  }
  
  public Long getRunTime() {
    return runTime;
  }

  @Override
  public int getN() {
    return n;
  }

  @Override
  public DoubleMatrix2D getR() {
    return new DenseDoubleMatrix2D(r);
  }
  
  @Override
  public DoubleMatrix2D getS() {
    return s;
  }


  @Override
  public DoubleMatrix2D getA() {
    SparseDoubleMatrix2D a = new SparseDoubleMatrix2D(n, n);
    for (Group group: groups) {
      for (int i=0; i<group.size; i++) {
        int len = group.lengths[i];
        int k;
        for (int j=0; j<len; j++) {
          k = group.indices[i][j];
          a.setQuick(i+group.startIndex, k, group.a[i][j]);
        }
      }
    }
    return a;
  }
  
  
  private void identifyExemplars() {
    DoubleMatrix2D ar = this.getAR();
    int c = 0;
    for (int i = 0; i < n; i++) {
      if (ar.get(i, i) > 0) c++;
    }
    System.out.println("AR diagonal > 0: " + c);
  }
  
}

