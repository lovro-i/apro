package fr.lri.tao.numa;

import com.sun.jna.*;
import java.util.Arrays;
import java.util.List;

public interface NUMALibrary extends Library {

  NUMALibrary INSTANCE = (NUMALibrary) Native.loadLibrary("numa", NUMALibrary.class);

  /** Structure used by numa library */
  public static class Bitmask extends Structure {

    
    public NativeLong size;
    public Pointer maskp;

    @Override
    protected List getFieldOrder() {
      return Arrays.asList(new String[] {"size", "maskp"});
    }
    
    @Override
    public String toString() {
      return "Size: " + size;
    }

  }

  int numa_available();

  int numa_max_node();

  int numa_num_task_nodes();

  int numa_num_task_cpus();

  int numa_node_of_cpu(int cpu);

  int numa_run_on_node(int node);
  
  
  // Initialize bitmask
  Bitmask numa_allocate_cpumask();
  Bitmask numa_bitmask_alloc(int n);
  int numa_bitmask_isbitset(Bitmask bmp, int n);

  // Map of cores on a node
  int numa_node_to_cpus(int node, Bitmask mask);
  
  // Memory alloc
  void numa_set_localalloc();
  void numa_set_preferred(int node);
  
    

}
