package fr.lri.tao.apro.data;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import fr.lri.tao.apro.util.Utils;

/** Simple dummy source for backing up Java matrices as input */
public class MatrixProvider implements DataProvider {

  private final DenseDoubleMatrix2D s;

  public MatrixProvider(double[][] m) {
    this.s = new DenseDoubleMatrix2D(m);
  }


  @Override
  public int size() {
    return s.rows();
  }
  
  @Override
  public String toString() {
    return s.toString();
  }
  
  @Override
  public void addNoise() {
    Utils.addNoise(s);
  }

  @Override
  public DoubleMatrix2D getMatrix() {
    return s;
  }
  
}
