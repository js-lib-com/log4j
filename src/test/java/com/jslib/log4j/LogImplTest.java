package com.jslib.log4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
  public void GivenDebugLevelEnabled_WhenSimpleMessage_ThenDebugPrint()
  {
    // given
    when(logger.isEnabled(Level.DEBUG)).thenReturn(true);

    // when
    log.debug("Debug message.");

    // then
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).debug(message.capture());

    assertThat(message.getValue(), equalTo("Debug message."));
  }

  /**
   * Syntax with named parameter is not supported by log4j and should be processed by this adapter implementation.
   */
  @Test
  public void GivenParameterizedMessage_WhenArgumentPresent_ThenFormatDebugPrint()
  {
    // given
    when(logger.isEnabled(Level.DEBUG)).thenReturn(true);

    // when
    log.debug("Phone {phone}.", "770 555-666");

    // then
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).debug(message.capture());

    assertThat(message.getValue(), equalTo("Phone 770 555-666."));
  }

  @Test
  public void GivenEmptyParameterizedMessage_WhenArgumentPresent_ThenFormatDebugPrint()
  {
    // given
    when(logger.isEnabled(Level.DEBUG)).thenReturn(true);

    // when
    log.debug("Phone {}.", "770 555-666");

    // then
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).debug(message.capture());

    assertThat(message.getValue(), equalTo("Phone 770 555-666."));
  }

  @Test
  public void GivenJavaFormatMessage_WhenArgumentPresent_ThenFormatDebugPrint()
  {
    // given
    when(logger.isEnabled(Level.DEBUG)).thenReturn(true);

    // when
    log.debug("Phone %s.", "770 555-666");

    // then
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).debug(message.capture());

    assertThat(message.getValue(), equalTo("Phone 770 555-666."));
  }

  @Test
  public void GivenParameterizedMessage_WhenArgumentMissing_ThenPrintOriginalText()
  {
    // given
    when(logger.isEnabled(Level.DEBUG)).thenReturn(true);

    // when
    log.debug("Phone {phone}.");

    // then
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).debug(message.capture());

    assertThat(message.getValue(), equalTo("Phone {phone}."));
  }

  @Test
  public void GivenEmptyParameterizedMessage_WhenArgumentMissing_ThenPrintOriginalText()
  {
    // given
    when(logger.isEnabled(Level.DEBUG)).thenReturn(true);

    // when
    log.debug("Phone {}.");

    // then
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    verify(logger, times(1)).debug(message.capture());

    assertThat(message.getValue(), equalTo("Phone {}."));
  }

  @Test
  public void GivenDebugLevelEnabled_WhenNullMessage_ThenPassNullToLog4j()
  {
    // given
    when(logger.isEnabled(Level.DEBUG)).thenReturn(true);

    // when
    log.debug(null);

    // then
    verify(logger, times(1)).debug((String)null);
  }

  @Test
  public void GivenFatalLevelEnabled_WhenDump_ThenStackTrace()
  {
    // given
    when(logger.isEnabled(Level.FATAL)).thenReturn(true);

    // when
    Throwable exception = new IOException("IO exception.");
    log.dump("Dump message:", exception);

    // then
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Throwable> throwable = ArgumentCaptor.forClass(Throwable.class);
    verify(logger, times(1)).fatal(message.capture(), throwable.capture());

    assertThat(message.getValue(), equalTo("Dump message:"));
    assertThat(throwable.getValue(), equalTo(exception));
  }

  @Test
  public void GivenFatalLevelEnabled_WhenDumpWithEmptyMessage_ThenAddGenericMesssage()
  {
    // given
    when(logger.isEnabled(Level.FATAL)).thenReturn(true);

    // when
    Throwable exception = new IOException("IO exception.");
    log.dump(exception);

    // then
    ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Throwable> throwable = ArgumentCaptor.forClass(Throwable.class);
    verify(logger, times(1)).fatal(message.capture(), throwable.capture());

    assertThat(message.getValue(), equalTo("Stack trace dump:"));
    assertThat(throwable.getValue(), equalTo(exception));
  }

  @Test
  public void GivenFatalLevelDisabled_WhenDump_ThenNoStackTrace()
  {
    // given
    when(logger.isEnabled(Level.FATAL)).thenReturn(false);

    // when
    log.dump("Dump message:", new IOException("IO exception."));

    // then
    verify(logger, times(0)).fatal(anyString(), any(Throwable.class));
  }
}
