package js.log4j.unit;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import js.log.LogLevel;
import js.log4j.LogImpl;

@RunWith(MockitoJUnitRunner.class)
public class LogImplTest
{
  @Mock
  private Logger logger;

  private LogImpl log;

  @Before
  public void beforeTest()
  {
    log = new LogImpl(logger);
  }

  @Test
  public void isLoggable_DEBUG_true()
  {
    when(logger.isEnabledFor(Level.DEBUG)).thenReturn(true);
    assertThat(log.isLoggable(LogLevel.DEBUG), equalTo(true));
    assertThat(log.isLoggable(LogLevel.INFO), equalTo(false));
  }

  @Test
  public void isLoggable_DEBUG_false()
  {
    when(logger.isEnabledFor(Level.DEBUG)).thenReturn(false);
    assertThat(log.isLoggable(LogLevel.DEBUG), equalTo(false));
    assertThat(log.isLoggable(LogLevel.INFO), equalTo(false));
  }

  @Test
  public void log()
  {
    log.log(LogLevel.DEBUG, "Debug message.");

    ArgumentCaptor<Level> level = ArgumentCaptor.forClass(Level.class);
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).log(level.capture(), message.capture());

    assertThat(level.getValue(), equalTo(Level.DEBUG));
    assertThat(message.getValue(), equalTo("Debug message."));
  }

  @Test
  public void log_NullMessage()
  {
    log.log(LogLevel.DEBUG, null);
    verify(logger, times(0)).log(any(Priority.class), anyString());
  }

  @Test
  public void dump()
  {
    when(logger.isEnabledFor(Level.FATAL)).thenReturn(true);

    Throwable exception = new IOException("IO exception.");
    log.dump("Dump message:", exception);

    ArgumentCaptor<Level> level = ArgumentCaptor.forClass(Level.class);
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Throwable> throwable = ArgumentCaptor.forClass(Throwable.class);
    verify(logger, times(1)).log(level.capture(), message.capture(), throwable.capture());

    assertThat(level.getValue(), equalTo(Level.FATAL));
    assertThat(message.getValue(), equalTo("Dump message:"));
    assertThat(throwable.getValue(), equalTo(exception));
  }

  @Test
  public void dump_Disabled()
  {
    when(logger.isEnabledFor(Level.FATAL)).thenReturn(false);
    log.dump("Dump message:", new IOException("IO exception."));
    verify(logger, times(0)).log(any(Priority.class), anyString(), any(Throwable.class));
  }

  @Test
  public void print()
  {
    when(logger.isEnabledFor(Level.DEBUG)).thenReturn(true);
    log.print(LogLevel.DEBUG, "Debug message.");

    ArgumentCaptor<Level> level = ArgumentCaptor.forClass(Level.class);
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).log(level.capture(), message.capture());

    assertThat(level.getValue(), equalTo(Level.DEBUG));
    assertThat(message.getValue(), equalTo("Debug message."));
  }

  @Test
  public void print_NullMessage()
  {
    when(logger.isEnabledFor(Level.DEBUG)).thenReturn(true);
    log.print(LogLevel.DEBUG, null);

    ArgumentCaptor<Level> level = ArgumentCaptor.forClass(Level.class);
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).log(level.capture(), message.capture());

    assertThat(level.getValue(), equalTo(Level.DEBUG));
    assertThat(message.getValue(), equalTo("null"));
  }

  @Test
  public void print_Disabled()
  {
    when(logger.isEnabledFor(Level.DEBUG)).thenReturn(false);
    log.print(LogLevel.DEBUG, "Debug message.");
    verify(logger, times(0)).log(any(Level.class), anyString());
  }
}
