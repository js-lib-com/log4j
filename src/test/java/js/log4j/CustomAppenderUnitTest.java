package js.log4j;

import js.lang.ConfigBuilder;
import js.lang.ConfigException;
import junit.framework.TestCase;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class CustomAppenderUnitTest extends TestCase
{
  private static final String CONFIG = "" + //
      "<?xml version='1.0' encoding='UTF-8'?>" + //
      "<log>" + //
      "<appender name='GRAY'>" + //
      "  <class>js.log4j.CustomAppenderUnitTest$MockAppender</class>" + //
      "  <format>%d{dd HH:mm:ss,SSS} [%t] %-5p %c %x- %m%n</format>" + //
      "  <parameters>" + //
      "    <threshold>WARN</threshold>" + //
      "  </parameters>" + //
      "  </appender>" + //
      "</log>";

  @Override
  protected void setUp() throws Exception
  {
    System.setProperty("logs", "fixture");
  }

  public void testSetThreshold() throws ConfigException
  {
    ConfigBuilder builder = new ConfigBuilder(CONFIG);

    LogProviderImpl provider = new LogProviderImpl();
    provider.config(builder.build());
  }

  // ----------------------------------------------------------------------------------------------
  // FIXTURE

  public static class MockAppender extends AppenderSkeleton
  {
    @Override
    public void close()
    {
    }

    @Override
    public boolean requiresLayout()
    {
      return false;
    }

    @Override
    protected void append(LoggingEvent event)
    {
    }
  }
}
