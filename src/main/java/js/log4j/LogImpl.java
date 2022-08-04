package js.log4j;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jslib.api.log.Log;

/**
 * Thin wrapper on Apache log4j {@link Logger}, implementing j(s)-lib {@link Log} interface. Apache log4j engine should
 * be properly initialized for this class to actually record any logging message.
 * <p>
 * There is a limit on logging record length, see {@link #MAX_MESSAGE_LENGTH}. If limit is reached logging message is
 * truncated with ellipsis.
 * 
 * @author Iulian Rotaru
 */
public final class LogImpl extends AbstractLog
{
  /** Maximum logging record length. Messages is truncated with ellipsis if exceed this limit. */
  private static final int MAX_MESSAGE_LENGTH = 2048;

  /** Underlying Apache log4j logger delegated for actual logging record writing. */
  private final Logger log4jLogger;

  /**
   * Construct logger instance for specified target class.
   * 
   * @param loggerName name to identify logger instance.
   */
  public LogImpl(String loggerName)
  {
    this.log4jLogger = LogManager.getLogger(loggerName);
  }

  // test constructor
  public LogImpl(Logger log4jLogger)
  {
    this.log4jLogger = log4jLogger;
  }

  @Override
  public boolean isLoggable(Level level)
  {
    return log4jLogger.isEnabled(level);
  }

  @Override
  public void log(Level level, String message)
  {
    if(message != null) {
      log4jLogger.log(level, ellipsis(message, MAX_MESSAGE_LENGTH));
    }
  }

  @Override
  public void dump(Object message, Throwable throwable)
  {
    if(isLoggable(Level.FATAL)) {
      log4jLogger.log(Level.FATAL, ellipsis(message(message), MAX_MESSAGE_LENGTH), throwable);
    }
  }
}
