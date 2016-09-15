package fr.lri.tao.apro.hiap;

import fr.lri.tao.apro.data.points.Point;
import fr.lri.tao.apro.data.points.Points;
import fr.lri.tao.apro.data.points.SimilarityMeasure;
import fr.lri.tao.apro.util.Percentile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Set of exemplar points and their count */
public class Exemplars {

  /** The original set of points */
  private Points points; 
  
  /** Map of statistics by exemplar */
  private final Map<Point, ExemplarStat> exes = new HashMap<Point, ExemplarStat>();
  
  /** Map point -> its exemplar */
  private final Map<Point, Point> exeMap = new HashMap<Point, Point>();
  
  public Exemplars(Points points) {    
    this.points = points;
  }
  
  public Exemplars(Points points, int[] exemplars) {
    this(points);
    for (int i = 0; i < exemplars.length; i++) {
      Point point = points.getByIndex(i);
      Point exemplar = points.getByIndex(exemplars[i]);
      add(point, exemplar);
    }
  }
  
  public int size() {
    return exes.size();
  }
  
  public Points getPoints() {
    return points;
  }
  
  public Point getByIndex(int i) {
    return this.getExemplars().get(i);
  }

  public void add(int pointIndex, int exemplarIndex) {
    this.add(points.getByIndex(pointIndex), points.getByIndex(exemplarIndex));  
  }
  
  public synchronized void add(Point point, Point exemplar) {
    ExemplarStat stat = exes.get(exemplar);
    if (stat == null) {
      stat = new ExemplarStat(exemplar);
      exes.put(exemplar, stat);
    }
    stat.add(point);
    exeMap.put(point, exemplar);
  }
  
  public Point getExemplar(Point point) {
    return exeMap.get(point);
  }
  
  ExemplarStat getStat(Point exemplar) {
    return exes.get(exemplar);
  }
  
  public Points getExemplars() {
    Points points = new Points();
    for (Point point: exes.keySet()) {
      points.add(point);
    }
    return points;
  }
  
  /** Returns matrix for WAP */
  public double[][] getWAPMatrix(SimilarityMeasure measure) {
    int n = this.size();
    double[][] s = new double[n][n];
    Percentile percentile = new Percentile();
    Points exemplars = this.getExemplars();
    
    // Calculate similarities
    for (int i=0; i<n; i++) {
      Point p1 = exemplars.getByIndex(i);
      int count = this.getStat(p1).size();
      for (int j=0; j<n; j++) {
        if (i != j) {
          Point p2 = exemplars.getByIndex(j);
          double similarity = measure.similarity(p1, p2); // p1.similarity(p2);
          s[i][j] = count * similarity;
          percentile.add(similarity);
        }
        // else percentile.add(0);
      }
    }
    

    double median = percentile.getMedian();
//    double p25 = percentile.getPercentile(25);
//    double p75 = percentile.getPercentile(75);
//    double r = Math.min(median-p25, p75-median) / median;

    // Set preferences to median
    for (int i = 0; i < n; i++) {
      Point exemplar = exemplars.getByIndex(i);
      ExemplarStat stat = exes.get(exemplar);
      s[i][i] = median - (stat.size() - 1) * stat.mean();
    }
    
    return s;
  }
  
  
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Number of exemplars: ").append(exes.size()).append(" (out of ").append(points.size()).append(")");
    return sb.toString();
  }
  

}


class ExemplarStat {
  
  private Point exemplar;
  private double sum;
  private List<Double> sims = new ArrayList<Double>();

  ExemplarStat(Point exemplar) {    
    this.exemplar = exemplar;
  }    
  
  void add(Point point) {
    double d = exemplar.similarity(point);
    sum += d;
    sims.add(d);
  }
  
  double mean() {
    return sum / sims.size();
  }
    
  double std() {
    if (sims.size() == 1) return 0;
    double mean = mean();
    double s = 0;
    for (Double d: sims) {
      double d1 = mean - d;
      s += d1 * d1;
    }
    s = s / (sims.size() - 1);
    s = Math.sqrt(s);
    return s;
  }
  
  int size() {
    return sims.size();
  }
  
  Point getExemplar() {
    return exemplar;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Exemplar ").append(exemplar).append(": ");
    sb.append(size()).append(" points\n");
    sb.append("mean=").append(mean());
    sb.append(", std=").append(std());
    return sb.toString();
  }
  
}