package com.jslib.log4j;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.jslib.api.log.Log;
import com.jslib.api.log.LogConfig;
import com.jslib.api.log.LogContext;
import com.jslib.api.log.LogProvider;
import com.jslib.lang.Config;

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

  @Override
  public Log getLogger(String loggerName)
  {
    return new LogImpl(LogManager.getLogger(loggerName));
  }

  @Override
  public LogConfig getLogConfig()
  {
    return null;
  }

  @Override
  public LogContext getLogContext()
  {
    return logContext;
  }

  @Override
  public void close()
  {
    LoggerContext.getContext(false).stop();
  }
}
