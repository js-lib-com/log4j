package js.log4j;

import org.apache.logging.log4j.core.LoggerContext;

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
  private final LogContext logContext;

  public LogProviderImpl()
  {
    this.logContext = new LogContextImpl(); 
    Log4jMXBeanImpl.create();
  }

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
    LoggerContext.getContext(false).stop();
  }
}
