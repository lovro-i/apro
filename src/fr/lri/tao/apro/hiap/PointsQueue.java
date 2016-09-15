package fr.lri.tao.apro.hiap;

import fr.lri.tao.apro.data.points.Points;
import java.util.LinkedList;
import java.util.List;

/** The queue of point sets from which Workers take their data */
public class PointsQueue {

  private final List<Points> queue = new LinkedList<Points>();
  
  public PointsQueue(List<Points> points) {
    this.queue.addAll(points);
  }
    
  public synchronized Points get() {
    if (this.queue.isEmpty()) return null;
    return this.queue.remove(0);
  }
  
  public synchronized boolean isEmpty() {
    return queue.isEmpty();
  }
  
}
