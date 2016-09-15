package fr.lri.tao.apro.hiap;

import fr.lri.tao.apro.data.points.Points;
import fr.lri.tao.apro.data.points.Point;
import java.util.ArrayList;

/** Helper class for splitting the set of points into n subsets */
public class DataParts extends ArrayList<Points> {

  /**
   * @param points The integral set of points to be split
   * @param n Number of sets to split the set into
   */
  public DataParts(Points points, int n) {
    final Points[] parts = new Points[n];
    for (int i = 0; i < n; i++) {
      parts[i] = new Points();
      this.add(parts[i]);
    }
        
    for (int i = 0; i < points.size(); i++) {
      Point point = points.getByIndex(i);
      parts[i%n].add(point);
    }    
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(size()+" parts: ");
    for (int i = 0; i < this.size(); i++) {
      Points points = this.get(i);
      sb.append(points.size()).append(" ");
    }
    return sb.toString();
  }
  
  
}
