package fr.lri.tao.apro.data.points;

import fr.lri.tao.apro.util.Percentile;

/** Negative squared Euclidean distance as the similarity measure between points. Preferences set to median value */
public class EuclideanSimilarityMeasure implements SimilarityMeasure {

  
  @Override
  public double[][] getMatrix(Points points) {
    int n = points.size();
    double[][] s = new double[n][n];
    Percentile percentile = new Percentile();
    
    // Calculate similarities
    for (int i=0; i<n; i++) {
      Point p1 = points.getByIndex(i);
      for (int j=i+1; j<n; j++) {
        Point p2 = points.getByIndex(j);
        s[i][j] = s[j][i] = similarity(p1, p2);
        percentile.add(s[i][j]);
      }
    }
    
    double median = percentile.getMedian();
    
    for (int i = 0; i < n; i++) {
      s[i][i] = median;
    }
    
    return s;
  }
  
  @Override
  public double similarity(Point p1, Point p2) {
    return p1.similarity(p2);
  }

}
