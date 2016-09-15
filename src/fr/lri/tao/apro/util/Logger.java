package fr.lri.tao.apro.util;

import java.util.logging.Level;


public class Logger {

  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("fr.lri.tao.apro");
  
  public static void info(String msg, Object... params) {
    logger.info(String.format(msg, params));
  }
  
  public static void warn(String msg, Object... params) {
    logger.warning(String.format(msg, params));
  }
  
  public static void warn(Throwable t) {
    logger.warning(t.getMessage());
  }
  
  public static void warn(Throwable t, String msg, Object... params) {
    logger.warning(t.getMessage() + "\n" + String.format(msg, params));
  }
  
  public static void error(String msg, Object... params) {
    logger.severe(String.format(msg, params));
  }
  
  public static void error(Throwable t) {
    logger.severe(t.getMessage());
  }
  
  public static void error(Throwable t, String msg, Object... params) {
    logger.severe(t.getMessage() + "\n" + String.format(msg, params));
  }
  
  public static void log(Level level, String msg) {
    logger.log(level, msg);
  }
  
  public static void main(String[] args) {
    Logger.info("Test %d", 5);
  }
}
