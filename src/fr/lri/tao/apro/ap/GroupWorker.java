package fr.lri.tao.apro.ap;

import fr.lri.tao.numa.NUMA;


public class GroupWorker extends Thread {
  

  public static enum Mode {
    RESPONSIBILITIES,
    AVAILABILITIES
  }
  
  
  private final Group group;
  private final boolean useNuma;
  private Mode mode;
  private Mode todo;
  
  private boolean go = false;
  private boolean done = false;
  private boolean busy = false;
  
  GroupWorker(Group group, boolean useNuma) {
    this.group = group;
    this.mode = null;
    this.useNuma = useNuma;
  }
  
  @Override
  public void run() {
    if (useNuma) {
      int node = group.getNumaNode();
      NUMA.runOnNode(node);
    }
    
    while (!done) {
      
      synchronized (this) {
        try { while (!go && !done) this.wait(); }
        catch (InterruptedException e) {}
        if (done) break;
        go = false;
        todo = mode;
        busy = true;
      }
      
      if (todo == Mode.RESPONSIBILITIES) group.computeResponsibilities();
      else if (todo == Mode.AVAILABILITIES) group.computeAvailabilities();
      
      synchronized (this) {
        busy = false;
        this.notify();
      }
      
    }
    
  }   
  
  /** Do one cycle of responsibilities calculation, then wait */
  public void responsibilities() {
    go(GroupWorker.Mode.RESPONSIBILITIES);
  }
  
  /** Do one cycle of availabilities calculation, then wait */
  public void availabilities() {
    go(GroupWorker.Mode.AVAILABILITIES);
  }
  
  private synchronized void go(Mode mode) {
    waitTask();
    this.go = true;
    this.mode = mode;
    busy = true;
    this.notify();
  }
  
  /** Block until the worker has finished a running cycle */
  public synchronized void waitTask() {
    try { while (busy) this.wait(); }
    catch (InterruptedException e) {}
  }
  
  /** Finish the thread */
  public synchronized void done() {
    this.done = true;
    this.notify();
  }
}
