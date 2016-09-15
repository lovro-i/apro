package fr.lri.tao.apro.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Utility class for finding median and percentile */

public class Percentile {
  
  private List<Double> values = new ArrayList<Double>();
  
  public void add(double val) {
    values.add(val);
  }
  
  public double getMedian() {
    Collections.sort(values);
    int middle = values.size() / 2;
    double median;
    if (values.size() % 2 == 0) {
      median = (values.get(middle) + values.get(middle - 1)) / 2;
    }
    else {
      median = values.get(middle);
    }
    return median;
  }
  
  
  public double getPercentile(double percentile) {
    Collections.sort(values);
    int index = (int) Math.ceil(percentile * values.size() / 100);
    if (index == values.size()) index--;
    return values.get(index);
  }
  
}
