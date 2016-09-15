package fr.lri.tao.apro.data.points;

import java.util.Collection;


/** An instance of data point; a node; an example; an object to be clustered */
public class Point {

  public final long id;
  public final double[] features;
  
  /** Create a point with an ID and a list of numerical features */
  public Point(long id, double[] features) {
    this.id = id;
    this.features = features;
  }
  
  public long getId() {
    return id;
  }
  
  /** Create a point with an ID and a collection of numerical features */
  public Point(long id, Collection<Double> features) {
    this.id = id;
    this.features = new double[features.size()];
    int i = 0;
    for (Double d: features) {
      this.features[i++] = d;
    }
  }
  
  /** Array of point features */
  public double[] getFeatures() {
    return features;
  }
  
  /** Euclidean distance between two points */
  public double distance(Point p) {
    double s = 0;
    for (int i=0; i<Math.min(features.length, p.features.length); i++) {
      double d = features[i] - p.features[i];
      s += d * d;
    }
    return Math.sqrt(s);
  }
  
  /** Default similarity between two points defined as negative squared distance */
  public double similarity(Point p) {
    double s = 0;
    int len = Math.min(features.length, p.features.length);
    for (int i=0; i<len; i++) {
      double d = features[i] - p.features[i];
      s += d * d;
    }
    return -s;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Point ").append(id).append(" (");
    for (int i=0; i<features.length; i++) {
      if (i > 0) sb.append(", ");
      sb.append(features[i]);
    }
    sb.append(")");
    return sb.toString();
  }
  
  @Override
  public boolean equals(Object o) {
    Point p = (Point) o;
    return p.id == this.id;
  }

  @Override
  public int hashCode() {
    return (int) id;
  }
  
}
