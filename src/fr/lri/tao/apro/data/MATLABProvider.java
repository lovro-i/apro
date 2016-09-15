package fr.lri.tao.apro.data;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSingle;
import com.jmatio.types.MLSparse;
import fr.lri.tao.apro.util.Logger;
import fr.lri.tao.apro.util.Utils;
import java.io.File;
import java.io.IOException;

/** Loader for MATLAB .mat files. Reads a sparse matrix in MATLAB format */
public class MATLABProvider implements DataProvider {

  private int n;
  private DoubleMatrix2D matrix;
  
  /**
   * @param file MATLAB file
   * @param arrayName Name of the matrix to load */
  public MATLABProvider(File file, String arrayName) throws IOException {
    this(file, arrayName, null);
  }
  
  public MATLABProvider(File file, String arrayName, String prefValue) throws IOException {
    load(file, arrayName, prefValue);
  }
  
  
  private void load(File file, String arrayName, String prefValue) throws IOException {
    MatFileReader mfr = new MatFileReader(file);
    MLArray mlArray = mfr.getMLArray(arrayName);
    
    assert(mlArray.getN() == mlArray.getM());
    this.n = mlArray.getN();
    Logger.info("[MatLoader] Matrix size: "+n+" x "+n);
    
    if (mlArray instanceof MLSparse) {
      MLSparse array = (MLSparse) mlArray;    
      int[][] indices = array.getIndices();

      matrix = new SparseDoubleMatrix2D(n, n);

      for (int j=0; j<indices.length; j++) {
        int i = indices[j][0];
        int k = indices[j][1];
        matrix.set(i, k, array.get(i, k));
      }
      Logger.info("[MatLoader] Elements: %d, density: %f%%", indices.length, (100d * indices.length / (n * n)));
    }
    else if (mlArray instanceof MLDouble) {
      MLDouble array = (MLDouble) mlArray;
      matrix = new DenseDoubleMatrix2D(array.getArray());
    }
    else if (mlArray instanceof MLSingle) {
      MLSingle array = (MLSingle) mlArray;
      double[][] a = new double[n][n];
      for (int i = 0; i < a.length; i++) {
        for (int j = 0; j < a.length; j++) {
          a[i][j] = array.getReal(i, j); 
        }        
      }
      matrix = new DenseDoubleMatrix2D(a);
    }
    else {
      throw new IOException("Unknown format, class " + mlArray.getClass().getName());
    }    
    
    if (prefValue != null) {
      MLArray prefArray = mfr.getMLArray(prefValue);
      if (prefArray instanceof MLDouble) {
        MLDouble ps = (MLDouble) prefArray;
        if (ps.getM() == 1 && ps.getN() == 1) {
          double pref = ps.get(0);
          for (int i = 0; i < n; i++) {
            matrix.set(i, i, pref);            
          }
        }
        else if (ps.getM() == n && ps.getN() == 1) {
          for (int i = 0; i < n; i++) {
            matrix.set(i, i, ps.get(i));
          }
        }
        else {
          throw new IOException("Preference must be a scalar or a vector of size N");
        }
      }
    }
  }
  
  @Override
  public DoubleMatrix2D getMatrix() {
    return matrix; // matrix.toArray();
  }

  @Override
  public int size() {
    return n;
  }
  
  @Override
  public void addNoise() {
    Utils.addNoise(matrix);
  }
  
}
