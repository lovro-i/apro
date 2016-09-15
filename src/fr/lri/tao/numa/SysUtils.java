package fr.lri.tao.numa;


public class SysUtils {

  public static String os() {
    return System.getProperty("os.name");
  }
    
  public static boolean isLinux1() {
    return os().toLowerCase().startsWith("linux");
  }

  public static boolean isWindows1() {
    return os().toLowerCase().startsWith("win");
  }
  
  public static int availableProcessors() {
    return Runtime.getRuntime().availableProcessors();
  }
  

}
