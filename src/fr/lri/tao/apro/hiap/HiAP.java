package fr.lri.tao.apro.hiap;

import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import fr.lri.tao.apro.data.points.Point;
import fr.lri.tao.apro.data.points.Points;
import fr.lri.tao.apro.data.points.PointsProvider;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.util.Logger;
import java.util.*;

/** Main class for Hierarchical Affinity Propagation 
 *  HiAP splits the point dataset into subsets, performs AP on each subset (first level)
 * and then gathers the results performing AP again on the set of exemplars from the first level
 */
public class HiAP {
  
  private PointsQueue queue;
  private final int workerIters;
  private final int wapIters;
  
  private final int threadsPerWorker;
  private final Integer[] nodes;
  private final int splits;
  private final Points allPoints;
  
  /** Temporary set of exemplars (before WAP) */
  private Exemplars allExemplars;
  
  /** The output, the final set of exemplars */
  private Exemplars exemplars;
  private final PointsProvider provider;
  private double damp;
  
  
  /**
   * @param provider PointsProvider that supplies set of points and a similarity measure between them
   * @param splits Number of subsets to split the points into
   * @param numaNodes Array of NUMA node IDs to execute workers on. If null, no NUMA thread pinning is used for the corresponding worker
   * @param threadsPerWorker Number of threads per worker on a node. Optimally, the number of cores on a node
   * @param workerIters Number of iterations for level 1 AP
   * @param wapIters Number of iterations for level 2 AP */
  public HiAP(PointsProvider provider, int splits, Integer[] numaNodes, int threadsPerWorker, int workerIters, int wapIters) {
    this.provider = provider;
    this.allPoints = provider.getPoints();
    this.splits = splits;
    this.nodes = numaNodes;
    this.threadsPerWorker = threadsPerWorker;
    this.workerIters = workerIters;
    this.wapIters = wapIters;
  }
  
  public HiAP(PointsProvider provider, int splits, int workers, int threadsPerWorker, int workerIters, int wapIters) {
    this.provider = provider;
    this.allPoints = provider.getPoints();
    this.splits = splits;
    this.nodes = new Integer[workers];
    for (int i = 0; i < nodes.length; i++) nodes[i] = null;
    this.threadsPerWorker = threadsPerWorker;
    this.workerIters = workerIters;
    this.wapIters = wapIters;
  }
  
  int getThreadsPerWorker() {
    return threadsPerWorker;
  }
  
  int getWorkerIterationCount() {
    return workerIters;
  }
  
  PointsQueue getQueue() {
    return queue;
  }
  
  PointsProvider getProvider() {
    return provider;
  }
  
  /** Temporary set of exemplars (before WAP), filled in by NodeWorkers */
  Exemplars getAllExemplars() {
    return allExemplars;
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
  
  /** Get the collection of points */
  public Points getPoints() {
    return this.allPoints;
  }
    
  public void run() throws InterruptedException {
    long start = System.currentTimeMillis();
    
    // Create subsets
    allPoints.shuffle();
    DataParts parts = new DataParts(allPoints, splits);
    queue = new PointsQueue(parts);
    allExemplars = new Exemplars(allPoints);
    
    
    // Initialize workers
    List<NodeWorker> workers = new ArrayList<NodeWorker>();
    for (int i=0; i<nodes.length; i++) {
      Integer node = nodes[i];
      NodeWorker worker = new NodeWorker(this, node);
      worker.start();
      workers.add(worker);
    }
    
    
    // Wait for workers to finish
    for (NodeWorker worker: workers) {
      worker.join();
    }
    
    long lapTime = System.currentTimeMillis() - start;
    
    // run WAP
    DataProvider wapProvider = new MatrixProvider(allExemplars.getWAPMatrix(provider.getSimilarityMeasure()));
    Apro wapro = new Apro(wapProvider, threadsPerWorker);
    wapro.setDamping(damp);
    wapro.run(wapIters);
    
    exemplars = new Exemplars(allPoints);
    int[] wex = wapro.getExemplars();    
    for (int i = 0; i < wex.length; i++) {
      int j = wex[i];
      Point point = allExemplars.getByIndex(i);
      Point exemplar = allExemplars.getByIndex(j);
      exemplars.add(point, exemplar);
    }
    
    long totalTime = System.currentTimeMillis() - start;
    long wapTime = totalTime - lapTime;
    Logger.info("[HiAP Done] From %d points to %d intermediate exemplars to %d final exemplars", allPoints.size(), allExemplars.size(), exemplars.size());
    Logger.info("[HiAP Time] %d nodes | %d splits | lap %d ms | wap %d ms | total %d ms", nodes.length, splits, lapTime, wapTime, totalTime);
  }

  public Point getExemplar(Point point) {
    Point e1 = allExemplars.getExemplar(point);
    return exemplars.getExemplar(e1);
  }
  
  public Point getExemplar(int id) {
    Point point = allPoints.getById(id);
    return getExemplar(point);
  }
  
}
