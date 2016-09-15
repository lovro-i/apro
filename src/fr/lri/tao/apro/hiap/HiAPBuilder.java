package fr.lri.tao.apro.hiap;

import fr.lri.tao.apro.data.points.PointsProvider;
import fr.lri.tao.apro.util.Logger;
import fr.lri.tao.numa.NUMA;
import fr.lri.tao.numa.SysUtils;


public class HiAPBuilder {

  private int workers = 1;
  private boolean numaWorkers = false;
  private Integer[] workerNodes = null;
  
  private int splits;
  private int threadsPerWorker;
  private int workerIters = 100;
  private int wapIters = 100;
  
  public HiAPBuilder() {
    this.splits = 2;
    this.threadsPerWorker = SysUtils.availableProcessors();
  }
  
  /** Set the number of iterations for the first-level Affinity Propagation 
   *  Default is 100
   * @param workerIters Number of iterations
   * @return this builder
   */
  public HiAPBuilder setWorkerIters(int workerIters) {
    this.workerIters = workerIters;
    return this;
  }
  
  /** Set the number of iterations for the second-level Affinity Propagation.
   *  Default is 100
   * @param wapIters Number of iterations
   * @return this builder
   */
  public HiAPBuilder setWAPIters(int wapIters) {
    this.wapIters = wapIters;
    return this;
  }
  
  /** Set the number of subsets to split the data into. 
   * Affinity Propagation is ran separately on each subset, and then gathered using Weighted Affinity Propagation (WAP).
   * 
   * @param splits Number of splits
   * @return this builder
   */
  public HiAPBuilder setSplits(int splits) {
    this.splits = splits;
    return this;
  }
  
  /** Parallelization parameter. Set the number of workers that will run first-level Affinity Propagation in parallel. 
   * Each worker is further parallelized within with 'threadsPerWorker' parameter.
   * Good approach is to have one worker per NUMA node, and threadsPerWorker set to number of cores on a node. Use setNumaAuto() for autodetect.
   * @param workers Number of workers for first-level AP
   * @return this builder
   */
  public HiAPBuilder setWorkers(int workers) {
    this.workers = workers;
    return this;
  }

  
  /** Parallelization parameter. Set the number (length of the array) and nodes of workers that will run first-level Affinity Propagation in parallel. 
   * Each worker is further parallelized within with 'threadsPerWorker' parameter.
   * Good approach is to have one worker per NUMA node, and threadsPerWorker set to number of cores on a node. Use setNumaAuto() for autodetect.
   * @param workerNodes Array of NUMA nodes to run each worker on. Length of the array is the number of workers. If workerNodes[i] is null, the node will be auto-assigned
   * @return this builder
   */
  public HiAPBuilder setWorkers(Integer[] workerNodes) {
    this.workerNodes = workerNodes;
    return this;
  }
  
  /** Parallelization parameter. Set the number of threads per each worker.
   *
   * @param threadsPerWorker Number of threads per worker
   * @return this builder
   */
  public HiAPBuilder setThreadsPerWorker(int threadsPerWorker) {
    this.threadsPerWorker = threadsPerWorker;
    return this;
  }
  
  public HiAPBuilder setNumaAuto() {
    if (NUMA.isAvailable()) {
      workers = NUMA.getNumNodes();
      numaWorkers = true;      
      this.threadsPerWorker = NUMA.getCoresPerNode();
    }
    else {
      this.threadsPerWorker = SysUtils.availableProcessors();
    }
    return this;
  }
  

  
  /** Builder HiAP object from the set parameters
   * @param provider Input data
   * @return HiAP object for running Hierarchical Affinity Propagation
   */
  public HiAP build(PointsProvider provider) {
    if (numaWorkers) {
      this.workerNodes = new Integer[workers];
      for (int i = 0; i < workerNodes.length; i++) {
        workerNodes[i] = i;
      }
    }
    else if (workerNodes == null) {
      this.workerNodes = new Integer[workers];
      for (int i = 0; i < workers; i++) {
        workerNodes[i] = null;      
      }
    }

    Logger.info("Building HiAP with %d splits and %d workers", splits, workerNodes.length);
    return new HiAP(provider, splits, workerNodes, threadsPerWorker, workerIters, wapIters);
  }
}
