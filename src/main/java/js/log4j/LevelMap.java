package js.log4j;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;

import js.log.LogLevel;

/**
 * Maps <code>j(s)-lib</code> logger levels to log4j {@link Level}. Current implementation is based on
 * <code>log4j</code>. In order to match <code>j(s)-lib</code> levels there are two <code>log4j</code> custom levels:
 * {@link LevelEx#TRACE} and {@link LevelEx#BUG}.
 * 
 * @author Iulian Rotaru
 */
final class LevelMap
{
  /** Levels map is actually a <code>log4j</code> levels list organized on <code>j(s)-lib</code> levels order. */
  private static final Level[] LOG4J_LEVEL_MAP = new Level[]
  {
      //
      LevelEx.TRACE, // LogLevel.TRACE
      Level.DEBUG, // LogLevel.DEBUG
      Level.INFO, // LogLevel.INFO
      Level.WARN, // LogLevel.WARN
      Level.ERROR, // LogLevel.ERROR
      Level.FATAL, // LogLevel.FATAL
      LevelEx.BUG, // LogLevel.BUG
      Level.OFF // LogLevel.OFF
  };

  /**
   * Get <code>log4j</code> level assigned to <code>j(s)-lib</code> level. It is considered a bug if given log level
   * dimension exceeds the {@link #LOG4J_LEVEL_MAP} size.
   * 
   * @param logLevel <code>j(s)-lib</code> log level.
   * @return <code>log4j</code> level.
   */
  static Level log4jLevel(LogLevel logLevel)
  {
    if(logLevel.ordinal() >= LOG4J_LEVEL_MAP.length) {
      throw new IllegalStateException(String.format("Log level |%s| dimension exceed map size.", logLevel));
    }
    return LOG4J_LEVEL_MAP[logLevel.ordinal()];
  }

  private static Map<Integer, LogLevel> LOG_LEVEL_MAP = new HashMap<>();
  static {
    LOG_LEVEL_MAP.put(Level.ALL_INT, LogLevel.TRACE);
    LOG_LEVEL_MAP.put(LevelEx.TRACE_INT, LogLevel.TRACE);
    LOG_LEVEL_MAP.put(Level.DEBUG_INT, LogLevel.DEBUG);
    LOG_LEVEL_MAP.put(Level.INFO_INT, LogLevel.INFO);
    LOG_LEVEL_MAP.put(Level.WARN_INT, LogLevel.WARN);
    LOG_LEVEL_MAP.put(Level.ERROR_INT, LogLevel.ERROR);
    LOG_LEVEL_MAP.put(Level.FATAL_INT, LogLevel.FATAL);
    LOG_LEVEL_MAP.put(LevelEx.BUG_INT, LogLevel.BUG);
    LOG_LEVEL_MAP.put(Level.OFF_INT, LogLevel.OFF);
  }

  /**
   * Get <code>j(s)-lib</code> log level assigned to requested <code>log4j</code> level. Missing <code>j(s)-lib</code>
   * log level assignment is considered a bug.
   * 
   * @param level <code>log4j</code> log level.
   * @return <code>j(s)-lib</code> log level.
   */
  static LogLevel logLevel(Level level)
  {
    LogLevel logLevel = LOG_LEVEL_MAP.get(level.toInt());
    if(logLevel == null) {
      throw new IllegalStateException(String.format("Log4j level |%s| not mapped.", level));
    }
    return logLevel;
  }

  /** Forbid default constructor synthesis. */
  private LevelMap()
  {
  }
}
