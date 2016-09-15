package fr.lri.tao.apro.data.points;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.util.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class PointsProvider implements DataProvider {

  private Points points;
  private SimilarityMeasure measure;
  private double[][] s;
  
  /** Load points from the DSV file. EuclideanSimilarityMeasure used as the default similarity measure */
  public PointsProvider(File file) throws IOException {
    this(file, new EuclideanSimilarityMeasure());
  }
  
  /** Load points from the DSV file. Custom point similarity used
   * @param file File containing delimiter separated values. Each row is one point, each value in a row is a feature value
   * @param measure Custom point similarity measure
   */
  public PointsProvider(File file, SimilarityMeasure measure) throws IOException {
    this.points = load(file);
    this.measure = measure;
    s = measure.getMatrix(this.points);
  }
  
  /** Provide list of points and similarity measure in order to obtain the similarity matrix
   * @param points List of points
   * @param measure Custom point similarity measure
   */
  public PointsProvider(Points points, SimilarityMeasure measure) {
    this.points = points;
    this.measure = measure;
    this.s = measure.getMatrix(this.points);
  }
  
  /** Provide list of points. EuclideanSimilarityMeasure used as the default similarity measure
   * @param points List of points
   */
  public PointsProvider(Points points) {
    this(points, new EuclideanSimilarityMeasure());
  }
  
  private static Points load(File file) throws IOException {
    Points points = new Points();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    int nextId = 0;
    List<Double> vals = new ArrayList<Double>();
    while ((line = reader.readLine()) != null) {
      StringTokenizer tokenizer = new StringTokenizer(line, " ,;\t");
      vals.clear();
      while (tokenizer.hasMoreTokens()) {
        String token = tokenizer.nextToken();
        Double d = Double.valueOf(token);
        vals.add(d);
      }
      Point point = new Point(nextId++, vals);
      points.add(point);
    }
    reader.close();
    return points;
  }
  
  public Points getPoints() {
    return points;
  }
  
  public SimilarityMeasure getSimilarityMeasure() {
    return measure;
  }
  
  @Override
  public DoubleMatrix2D getMatrix() {
    return new DenseDoubleMatrix2D(s);
  }
  
  public double[][] getS() {
    return s;
  }

  @Override
  public int size() {
    return points.size();
  }

  @Override
  public void addNoise() {
    Utils.addNoise(s);
  }

}
