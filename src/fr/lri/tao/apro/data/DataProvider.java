package fr.lri.tao.apro.data;

import cern.colt.matrix.DoubleMatrix2D;

/** Interface to be implemented by all SparseDataProvider implementation classes - classes that provide similarity matrix from different formats */
public interface DataProvider {

  /** Similarity matrix as Java's array of arrays
   * @return Similarity matrix with preferences on the main diagonal, in Colt format */
  public DoubleMatrix2D getMatrix();  
  
  /** Number of elements in the dataset, i.e. the size of the matrix (size x size) */
  public int size();
  
  /** Add noise to the similarity matrix */
  public void addNoise();
}
