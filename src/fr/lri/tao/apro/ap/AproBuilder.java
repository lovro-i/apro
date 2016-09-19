package fr.lri.tao.apro.ap;

import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.util.Logger;
import fr.lri.tao.numa.NUMA;
import fr.lri.tao.numa.SysUtils;

/** Utility class for comprehensibly building Apro instance 
 *  By default, NUMA is switched off, and number of groups (threads) is set to the available number of cores
 */
public class AproBuilder {

  private int groupCount;
  private boolean useNuma = false;
  private Integer numNodes = null;
  private Integer coresPerNode = null;
  private Integer startNode = null;
  private Double damping;
  private Boolean debug;
  
  
  /** Constructor  */
  public AproBuilder() {
    this.setNumaOff();
    this.setThreadsAuto();
  }
  
  /** Manually set the number of groups for parallelization 
   *  Default is the available number of cores
   * @param threadCount Number of data groups (threads) to divide work between
   * @return this builder
   */
  public AproBuilder setThreads(int threadCount) {
    this.groupCount = threadCount;
    return this;
  }
  
  /** Automatically the number of groups for parallelization 
   *  to the available number of cores
   * @return this builder
   */
  public final AproBuilder setThreadsAuto() {
    this.groupCount = SysUtils.availableProcessors();
    return this;
  }
  
  /** Manually set NUMA parameters for running
   * @param numNodes Number of NUMA nodes to be used in the execution. null for auto detect all available
   * @param coresPerNode Number of cores (threads) per NUMA node. null for auto detect
   * @param startNode ID of the node to start from. null starts from the current one
   * @return this builder
   * 
   * Available only on Linux systems
   */
  public AproBuilder setNuma(Integer numNodes, Integer coresPerNode, Integer startNode) {
    if (!NUMA.isAvailable()) {
      Logger.warn("NUMA not available, ignoring");
      return setNumaOff();
    }
    
    if (numNodes == null) this.numNodes = NUMA.getNumNodes();
    else this.numNodes = numNodes;
    
    if (coresPerNode == null) this.coresPerNode = NUMA.getCoresPerNode();
    else this.coresPerNode = coresPerNode;
    
    if (startNode == null) this.startNode = NUMA.getNode();
    else this.startNode = startNode;
    
    return this;
  }
  
  /** Automatically set NUMA parameters:
   * Number of NUMA nodes is set to maximum available (auto detect)
   * Number of cores per NUMA node is auto detected
   * Starting node (for running threads) is the current one
   * @return this builder
   * 
   * Available only on Linux systems
   */
  public AproBuilder setNumaAuto() {
    if (NUMA.isAvailable()) {
      this.useNuma = true;
      this.numNodes = NUMA.getNumNodes();
      this.coresPerNode = NUMA.getCoresPerNode();
      this.startNode = NUMA.getNode();
      Logger.info("[NUMA] %d nodes, %d coresPerNode", numNodes, coresPerNode);
    }
    else {
      setNumaOff();
    }
    return this;
  }
  
  /** Switches off NUMA features */
  public AproBuilder setNumaOff() {
    this.useNuma = false;
    this.numNodes = this.coresPerNode = this.startNode = null;
    return this;
  }
  
  /** Sets all the parameters automatically (number of nodes, number of cores per node, starting node, number of groups */
  public AproBuilder setFullAuto() {
    this.setNumaAuto();
    if (numNodes != null && coresPerNode != null) this.groupCount = numNodes * coresPerNode;
    else this.groupCount = SysUtils.availableProcessors();
    return this;
  }
  
  public AproBuilder setDebug(boolean debug) {
    this.debug = debug;
    return this;
  }
  
  public AproBuilder setDamping(double dampingFactor) {
    this.damping = dampingFactor;
    return this;
  }
  
  /** Returns the properly initialized Apro instance for running the Affinity Propagation
   * @param provider DataProvider for similarity matrix */
  public Apro build(DataProvider provider) {
    if (provider == null) throw new NullPointerException("Similarity matrix provider required");
    Apro apro = new Apro(provider, groupCount, useNuma, numNodes, coresPerNode, startNode);
    if (debug != null) apro.setDebug(debug);
    if (damping != null) apro.setDamping(damping);
    return apro;    
  }

  @Override
  public String toString() {
    return "AproBuilder{groupCount=" + groupCount + ", useNuma=" + useNuma + ", numNodes=" + numNodes + ", coresPerNode=" + coresPerNode + ", startNode=" + startNode + '}';
  }
  
  

}
