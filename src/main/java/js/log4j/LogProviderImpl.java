package js.log4j;

import java.util.Enumeration;

import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import js.lang.Config;
import js.log.Log;
import js.log.LogContext;
import js.log.LogProvider;

/**
 * Implementation for {@link LogProvider} interface. This implementation is rather simple: it is a factory for loggers
 * and log contexts.
 * <p>
 * Current implementation uses underlying <code>log4j.properties</code> configuration; therefore {@link #config(Config)}
 * is not used, that is, is empty.
 * 
 * @author Iulian Rotaru
 */
public final class LogProviderImpl implements LogProvider
{
  /** Reusable log context instance. */
  private final LogContext logContext = new LogContextImpl();

  /**
   * Current implementation uses underlying <code>log4j.properties</code> configuration and this method is NOP.
   * 
   * @param config configuration object, not used.
   */
  @Override
  public void config(Config config)
  {
  }

  @Override
  public Log getLogger(String loggerName)
  {
    return new LogImpl(loggerName);
  }

  @Override
  public LogContext getLogContext()
  {
    return logContext;
  }

  @Override
  public void forceImmediateFlush()
  {
    Enumeration<?> loggersEnumeration = LogManager.getCurrentLoggers();
    while(loggersEnumeration.hasMoreElements()) {
      Object loggerElement = loggersEnumeration.nextElement();
      if(!(loggerElement instanceof Logger)) {
        continue;
      }
      Logger logger = (Logger)loggerElement;

      Enumeration<?> appendersEnumeration = logger.getAllAppenders();
      while(appendersEnumeration.hasMoreElements()) {
        Object appenderElement = appendersEnumeration.nextElement();
        if(appenderElement instanceof FileAppender) {
          FileAppender appender = (FileAppender)appenderElement;
          appender.setBufferedIO(false);
          appender.setImmediateFlush(true);
          // next log is not only informative; it actually triggers the flush process
          logger.info(String.format("Flush appender |%s|.", appender.getName()));
        }
      }
    }
  }
}
