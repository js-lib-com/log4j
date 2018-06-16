package js.log4j;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j appender used to send events to a remote console server. Instance of this appender obtain a remote console
 * {@link RemoteConsoleWriter writer} reference then simply write the formatted event. This appender support <code>Port</code>
 * property; below is a configuration example for log4j.properties:
 * 
 * <pre>
 * log4j.appender.CON=js.log.RemoteConsoleAppender
 * log4j.appender.CON.Port=8001
 * log4j.appender.CON.layout=org.apache.log4j.PatternLayout
 * log4j.appender.CON.layout.ConversionPattern=%d{dd HH:mm:ss,SSS} %-5p %c %x- %m%n
 * </pre>
 * 
 * @author Iulian Rotaru
 */
public class RemoteConsoleAppender extends AppenderSkeleton
{
  /**
   * Socket server listening port.
   */
  private static final int DEFAULT_PORT = 8001;

  /**
   * Remote console writer port. Use this value to initialize remote console {@link RemoteConsoleWriter writer}.
   */
  private int port = DEFAULT_PORT;

  /**
   * Remote console writer.
   */
  private Writer writer;

  /**
   * Get remote console writer listening port.
   * 
   * @return remote console port.
   */
  public int getPort()
  {
    return this.port;
  }

  /**
   * Set remote console writer listening port.
   * 
   * @param port remote console port.
   */
  public void setPort(int port)
  {
    this.port = port;
  }

  /**
   * Configurators call this method to determine if the appender requires a layout.
   * 
   * @return always return true.
   */
  @Override
  public boolean requiresLayout()
  {
    return true;
  }

  /**
   * Subclasses of AppenderSkeleton should implement this method to perform actual logging.
   * 
   * @param event logging event.
   */
  @Override
  protected void append(LoggingEvent event)
  {
    if(this.layout == null) {
      this.errorHandler.error("No layout for appender " + this.name, null, ErrorCode.MISSING_LAYOUT);
      return;
    }
    if(this.writer == null) {
      this.writer = RemoteConsoleWriter.getInstance(this.port);
    }
    try {
      this.writer.write(this.layout.format(event));
      this.writer.flush();
    }
    catch(IOException e) {
      this.errorHandler.error(e.getMessage(), null, ErrorCode.WRITE_FAILURE);
    }
  }

  /**
   * Release any resources allocated within the appender such as file handles, network connections, etc.
   */
  @Override
  public void close()
  {
    if(this.writer != null) {
      try {
        this.writer.close();
      }
      catch(IOException e) {
        this.errorHandler.error(e.getMessage(), null, ErrorCode.CLOSE_FAILURE);
      }
    }
  }
}
