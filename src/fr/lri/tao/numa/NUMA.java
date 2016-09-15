package fr.lri.tao.numa;

import java.util.*;

/** Class containing static NUMA methods, which further call native libnuma */
public class NUMA {


  public static boolean isAvailable() {
    try { return NUMALibrary.INSTANCE.numa_available() != -1; }
    catch (Throwable e) { return false; }
  }
  
  public static int getMaxNode() {
    return NUMALibrary.INSTANCE.numa_max_node();
  }
  
  public static int getNumNodes() {
    return NUMALibrary.INSTANCE.numa_num_task_nodes();
  }
  
  public static int getNumCores() {
    return NUMALibrary.INSTANCE.numa_num_task_cpus();
  }
  
  public static int getCoresPerNode() {
    return getNumCores() / getNumNodes();
  }
  
  /** Get NUMA node of the specified cpu */
  public static int getNode(int cpu) {
    return NUMALibrary.INSTANCE.numa_node_of_cpu(cpu);
  }
  
//  public static int getNode(Object obj) {
//    Pointer[] ptrs = new Pointer[1];
//    IntByReference c = new IntByReference();
//    PointerByReference pref = new PointerByReference();
//    //ptrs[0] = obj.;
//    int[] status = new int[1];
//    NativeLong nl = new NativeLong(1l);
//    NUMALibrary.INSTANCE.numa_move_pages(0, nl, ptrs, null, status, 0);
//    return status[0];
//  }
  
  public static void allocOnNode(Integer node) {
    if (node != null) NUMALibrary.INSTANCE.numa_set_preferred(node);
    else localAlloc();
  }
  
  public static void localAlloc() {
    if (!isAvailable()) return;
    NUMALibrary.INSTANCE.numa_set_localalloc();
  }
  
  public static int runOnNode(int node) {
    int res = NUMALibrary.INSTANCE.numa_run_on_node(node);
    NUMALibrary.INSTANCE.numa_set_localalloc();
    return res;
  }
  
  /** @return Current CPU */
  public static int getCore() {
    return CLibrary.INSTANCE.sched_getcpu();
  }
  
  /** @return Current node */
  public static int getNode() {
    return getNode(getCore());
  }
  
  public static List<Integer> getCores(int node) {   
    List<Integer> cores = new ArrayList<Integer>();
    //    for (int cpu=0; cpu<getNumCores(); cpu++) {
    //      if (getNode(cpu) == node) cores.add(cpu);
    //    }
    //    return cores;
    
    NUMALibrary.Bitmask mask = NUMALibrary.INSTANCE.numa_allocate_cpumask();
    // or... NUMALibrary.Bitmask mask2 = NUMALibrary.INSTANCE.numa_bitmask_alloc(16);
    
    NUMALibrary.INSTANCE.numa_node_to_cpus(node, mask);
    int cc = getNumCores();
    for (int cpu = 0; cpu < cc; cpu++) {
      if (NUMALibrary.INSTANCE.numa_bitmask_isbitset(mask, cpu) > 0) cores.add(cpu);
    }
    
    return cores;
  }
  
  public static void main(String[] args) throws InterruptedException {
    System.out.println("NUMA Available: " + NUMA.isAvailable());
    System.out.println("NUMA Nodes: " + NUMA.getNumNodes());
    System.out.println("NUMA Cores: " + NUMA.getNumCores());
    System.out.println("NUMA Node of core " + 44 +": "+NUMA.getNode(44));
    System.out.println("Running on node "+getNode()+", core "+getCore());
    
    
    Random random = new Random();
    List<Thread> threads = new ArrayList<Thread>();
    for (int i = 0; i < 100; i++) {
      LittleThread t = new LittleThread(random.nextInt(NUMA.getNumNodes()));
      t.start();
      threads.add(t);
    }
    
    for (int i = 0; i < 100; i++) {
      threads.get(i).join();
    }
    
    
    List<Integer> cores = NUMA.getCores(5);
    System.out.println(cores.size());
    System.out.println(Arrays.toString(cores.toArray()));
    
    System.out.println("Done.");
  }
}


class LittleThread extends Thread {
  
  private static Random random = new Random();
  private int id;
  
  LittleThread(int id) {
    this.id = id;
    System.out.println("Initializing thread "+id);
  }
  
  @Override
  public void run() {
    NUMA.runOnNode(id);
    try { Thread.sleep(1000+random.nextInt(500)); }
    catch (InterruptedException ex) { }
    
    System.out.println("Thread "+id+" running on core "+NUMA.getCore()+", node "+NUMA.getNode());
    try { Thread.sleep(1000); }
    catch (InterruptedException ex) { }
  }
  
  
  
  
}