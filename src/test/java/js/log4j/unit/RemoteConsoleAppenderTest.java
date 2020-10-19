package js.log4j.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import js.log4j.RemoteConsoleAppender;
import js.log4j.RemoteConsoleWriter;

@RunWith(MockitoJUnitRunner.class)
public class RemoteConsoleAppenderTest
{
  @Mock
  private RemoteConsoleWriter writer;
  @Mock
  private Layout layout;
  @Mock
  private LoggingEvent event;

  private RemoteConsoleAppender appender;

  @Before
  public void beforeTest()
  {
    appender = new RemoteConsoleAppender(layout, writer);
  }

  @Test
  public void append() throws IOException
  {
    when(layout.format(event)).thenReturn("Debug message.");
    appender.append(event);

    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(writer, times(1)).write(message.capture());
    assertThat(message.getValue(), equalTo("Debug message."));

    verify(writer, times(1)).flush();
  }
  
  @Test
  public void close() throws IOException {
    appender.close();
    verify(writer, times(1)).close();
  }
}
