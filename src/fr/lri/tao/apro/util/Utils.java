package fr.lri.tao.apro.util;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class Utils {
  
  private static final double EPSILON = 1E-14;
  private static final double REALMIN100 = 100 * Double.MIN_NORMAL;
  

  public static String toString(double[][] a) {
    int maxLength = 12;
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<a.length; i++) {
      for (int j=0; j<a[i].length; j++) {
        String val = String.valueOf(a[i][j]);
        maxLength = Math.max(maxLength, val.length());
        while (val.length() < maxLength) val = " " + val;
        sb.append(val);
      }
      sb.append("\n");
    }
    return sb.toString();
  }
  
  public static String toString(double[][] a, int decimals) {
    if (decimals < 0) throw new IllegalArgumentException("Number of decimals must be positive");
    StringBuilder decs = new StringBuilder("0.");
    for (int i = 0; i < decimals; i++) decs.append("0");
    NumberFormat decimalsFormat = new DecimalFormat(decs.toString());
    
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<a.length; i++) {
      for (int j=0; j<a[i].length; j++) {
        sb.append(decimalsFormat.format(a[i][j])).append(" ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }
  
  public static void addNoise(double[][] s) {
    for (int i=0; i<s.length; i++) {
      for (int j=0; j<s[i].length; j++) {
        s[i][j] = s[i][j] + (EPSILON * s[i][j] + REALMIN100) * Math.random();
      }
    }
  }
  
  public static void addNoise(DoubleMatrix2D s) {
    IntArrayList is = new IntArrayList();
    IntArrayList ks = new IntArrayList();
    DoubleArrayList vs = new DoubleArrayList();            
    s.getNonZeros(is, ks, vs);
    
    for (int j=0; j<is.size(); j++) {
      int i = is.get(j);
      int k = ks.get(j);
      double v = vs.get(j);
      v = v + (EPSILON * v + REALMIN100) * Math.random();
      s.setQuick(i, k, v);
    }    
  }
  
}
