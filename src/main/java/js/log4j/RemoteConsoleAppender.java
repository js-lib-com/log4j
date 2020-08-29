package js.log4j;

import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j appender used to send events to a remote console server. Instance of this appender obtain a remote console
 * {@link RemoteConsoleWriter writer} reference then simply write the formatted event. This appender support
 * <code>Port</code> property; below is a configuration example for log4j.properties:
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
  /** Socket server listening port. */
  private static final int DEFAULT_PORT = 8001;

  /** Remote console writer port. Use this value to initialize remote console {@link RemoteConsoleWriter writer}. */
  private int port = DEFAULT_PORT;

  /** Remote console writer. */
  private Writer writer;

  public RemoteConsoleAppender()
  {
  }

  /**
   * Test constructor.
   * 
   * @param layout mock layout,
   * @param writer mock writer.
   */
  public RemoteConsoleAppender(Layout layout, Writer writer)
  {
    this.layout = layout;
    this.writer = writer;
  }

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
  public void append(LoggingEvent event)
  {
    if(layout == null) {
      errorHandler.error("No layout for appender " + name, null, ErrorCode.MISSING_LAYOUT);
      return;
    }
    if(writer == null) {
      writer = new RemoteConsoleWriter(port);
    }
    try {
      writer.write(layout.format(event));
      writer.flush();
    }
    catch(IOException e) {
      errorHandler.error(e.getMessage(), null, ErrorCode.WRITE_FAILURE);
    }
  }

  /**
   * Release any resources allocated within the appender such as file handles, network connections, etc.
   */
  @Override
  public void close()
  {
    if(writer != null) {
      try {
        writer.close();
      }
      catch(IOException e) {
        errorHandler.error(e.getMessage(), null, ErrorCode.CLOSE_FAILURE);
      }
    }
  }
}
