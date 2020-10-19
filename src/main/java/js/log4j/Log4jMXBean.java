package js.log4j;

import js.log.LogLevel;

/**
 * Management bean for log4j adaptor. Allows for logging level manipulation on runtime.
 * 
 * @author Iulian Rotaru
 */
public interface Log4jMXBean
{
  /**
   * Set logging level for the root logger. Logging level should be a valid name as supported by {@link LogLevel}
   * enumeration. Bad level names are silently ignored.
   * 
   * @param level log level name.
   */
  void setRootLevel(String level);

  /**
   * Get logging level for the root logger. Returned value could be null if root logger level is not set.
   * 
   * @return root logger level, possible null.
   */
  String getRootLevel();

  /**
   * Set logging level for the logger identified by given name. Logger name should designate an existing logger. If name
   * argument is null or logger not found this setter does nothing. Also logging level argument should be a valid name
   * as supported by {@link LogLevel} enumeration. Bad level names are silently ignored.
   * 
   * @param name existing logger name,
   * @param level log level name.
   */
  void setLevel(String name, String level);

  /**
   * Get logging level for the logger identified by given name. Logger name should designate an existing logger. If name
   * argument is null or logger not found this getter returns null. Also returns null if this requested logger has no
   * level set. Please note that this getter return logger set level, not the level inherited from its ancestors.
   * 
   * @param name name of an existing logger.
   * @return logger level, possible null.
   */
  String getLevel(String name);
}
