package fr.lri.tao.apro.hiap;

import fr.lri.tao.apro.data.points.Points;
import fr.lri.tao.apro.data.points.PointsProvider;
import fr.lri.tao.apro.data.points.SimilarityMeasure;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.util.Logger;
import fr.lri.tao.numa.NUMA;
import fr.lri.tao.numa.SysUtils;

/** Worker on a NUMA node that takes one set of points and performs Affinity Propagation on it. 
 * The resulting set of exemplars is fed back to HiAP set of all exemplars for later performing WAP
 */ 
public class NodeWorker extends Thread {

  private final HiAP hiap;
  private final Integer node;
  
  /** Create a worker for performing Affinity Propagation on data splits. The worker will be ran on a specified node
   * @param hiap The main Hierarchical Affinity Propagation controller
   * @param numaNode The NUMA node this worker will run on
   */
  public NodeWorker(HiAP hiap, Integer numaNode) {
    this.hiap = hiap;
    this.node = numaNode;
  }
  
  /** Create a worker for performing Affinity Propagation on data splits. The node for worker is not specified (any)
   * @param hiap The main Hierarchical Affinity Propagation controller
   */
  public NodeWorker(HiAP hiap) {
    this(hiap, null);
  }
  
  @Override
  public void run() {
    if (node != null) {
      if (!NUMA.isAvailable()) Logger.warn("NUMA library not available");
      else NUMA.runOnNode(node);
    }
    
    PointsQueue queue = hiap.getQueue();
    int coresPerNode = hiap.getThreadsPerWorker();
    int iters = hiap.getWorkerIterationCount();
    
    Points points = queue.get();
    while (points != null) {
      SimilarityMeasure measure = hiap.getProvider().getSimilarityMeasure();
      PointsProvider partProvider = new PointsProvider(points, measure);
      Apro apro = new Apro(partProvider, coresPerNode, false); // False, because it's already pinned on the current node
      apro.setDebug(false);
      apro.setDamping(hiap.getDamping());
      apro.run(iters);

      int[] exa = apro.getExemplars();
      Exemplars exes = hiap.getAllExemplars();
      for (int i = 0; i < exa.length; i++) {
        int j = exa[i];
        exes.add(points.getByIndex(i), points.getByIndex(j));
      }
      points = queue.get();
    }
  }
  
}
