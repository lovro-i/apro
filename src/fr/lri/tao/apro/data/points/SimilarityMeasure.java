package fr.lri.tao.apro.data.points;

/** Interface for providing custom similarity measure between data points */
public interface SimilarityMeasure {

  public double[][] getMatrix(Points points);
  
  public double similarity(Point p1, Point p2);
}
