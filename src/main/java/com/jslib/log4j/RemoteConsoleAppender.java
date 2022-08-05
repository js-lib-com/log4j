package com.jslib.log4j;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

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
@Plugin(name = "RemoteConsoleAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class RemoteConsoleAppender extends AbstractAppender
{
  @PluginFactory
  public static RemoteConsoleAppender createAppender( //
      @PluginAttribute("name") String name, //
      @PluginElement("Layout") Layout<? extends Serializable> layout, //
      @PluginElement("Filter") final Filter filter, //
      @PluginAttribute("otherAttribute") String otherAttribute)
  {
    if(name == null) {
      LOGGER.error("There is no name provided for MyCustomAppender");
      return null;
    }
    if(layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new RemoteConsoleAppender(name, filter, layout, true);
  }

  /** Socket server listening port. */
  private static final int DEFAULT_PORT = 8001;

  /** Remote console writer port. Use this value to initialize remote console {@link RemoteConsoleWriter writer}. */
  private int port = DEFAULT_PORT;

  /** Remote console writer. */
  private Writer writer;

  protected RemoteConsoleAppender(String name, Filter filter, Layout<?> layout, boolean ignoreExceptions)
  {
    super(name, filter, layout, ignoreExceptions, (Property[])null);
  }

  public RemoteConsoleAppender(Layout<?> layout, RemoteConsoleWriter writer)
  {
    super("test", null, layout, false, (Property[])null);
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
   * Subclasses of AppenderSkeleton should implement this method to perform actual logging.
   * 
   * @param event logging event.
   */
  @Override
  public void append(LogEvent event)
  {
    if(getLayout() == null) {
      error("No layout for appender " + getName());
      return;
    }
    if(writer == null) {
      writer = new RemoteConsoleWriter(port);
    }
    try {
      writer.write(new String(getLayout().toByteArray(event)));
      writer.flush();
    }
    catch(IOException e) {
      error(e.getMessage());
    }
  }

  /**
   * Release any resources allocated within the appender such as file handles, network connections, etc.
   */
  @Override
  public void stop()
  {
    if(writer != null) {
      try {
        writer.close();
      }
      catch(IOException e) {
        error(e.getMessage());
      }
    }
  }
}
