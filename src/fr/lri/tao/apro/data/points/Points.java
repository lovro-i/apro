package fr.lri.tao.apro.data.points;

import java.util.*;

public class Points extends ArrayList<Point> {
  
  private final Map<Long, Point> pointMap = new HashMap<Long, Point>();
  
  @Override
  public boolean add(Point point) {    
    this.pointMap.put(point.getId(), point);
    return super.add(point);
  }
  
  public Point getByIndex(int index) {
    return this.get(index);
  }
  
  public Point getById(long id) {
    return pointMap.get(id);
  }
  
  public int indexOf(Point point) {
    long id = point.getId();
    for (int i = 0; i < this.size(); i++) {
      if (this.get(i).getId() == id) return i;
    }
    return -1;  
  }
  
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(size()).append(" points: ");
    for (Point point: this) sb.append(" ").append(point);
    return sb.toString();
  }
  
  public void shuffle() {
    Random random = new Random();
    for (int i = 0; i < size(); i++) {
      int j = random.nextInt(size());
      Point point1 = this.get(i);
      Point point2 = this.get(j);
      this.set(i, point2);
      this.set(j, point1);      
    }
  }
  
     
}
