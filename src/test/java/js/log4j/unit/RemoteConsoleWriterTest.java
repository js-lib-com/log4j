package js.log4j.unit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import js.log4j.RemoteConsoleWriter;

@RunWith(MockitoJUnitRunner.class)
public class RemoteConsoleWriterTest
{
  @Mock
  private ServerSocket server;

  private RemoteConsoleWriter writer;
  private BlockingQueue<String> queue;

  @Before
  public void beforeTest()
  {
    writer = new RemoteConsoleWriter(server);
    queue = writer.getQueue();
  }

  @Test
  public void write() throws IOException
  {
    char[] message = "Debug message.\r\n".toCharArray();
    writer.write(message, 0, message.length);
    writer.flush();

    assertThat(queue, hasSize(1));
    assertThat(queue.poll(), equalTo("Debug message."));
  }

  @Test
  public void write_Lines() throws IOException
  {
    char[] message = "Debug message.\r\nInfo message.\r\n".toCharArray();
    writer.write(message, 0, message.length);
    writer.flush();

    assertThat(queue, hasSize(2));
    assertThat(queue.poll(), equalTo("Debug message."));
    assertThat(queue.poll(), equalTo("Info message."));
  }

  @Test
  public void write_Offset() throws IOException
  {
    char[] message = "Debug message.\r\n".toCharArray();
    writer.write(message, 0, message.length - 5);
    writer.write(message, message.length - 5, 5);
    writer.flush();

    assertThat(queue, hasSize(1));
    assertThat(queue.poll(), equalTo("Debug message."));
  }

  @Test
  public void flush_Empty() throws IOException
  {
    writer.flush();
    assertThat(queue, hasSize(0));
  }

  @Test
  public void server() throws IOException
  {
    // a debug message followed by a shutdown command - SD
    char[] message = "Debug message.\r\nSD\r\n".toCharArray();
    writer.write(message, 0, message.length);
    writer.flush();

    Socket client = Mockito.mock(Socket.class);
    OutputStream stream = new ByteArrayOutputStream();
    when(server.accept()).thenReturn(client);
    when(client.getOutputStream()).thenReturn(stream);

    writer.run();

    assertThat(stream.toString(), equalTo("Debug message.\r\n"));
    verify(server, times(1)).close();
  }

  @Test
  public void server_ClientClose() throws IOException
  {
    char[] message = "Debug message.\r\nSD\r\n".toCharArray();
    writer.write(message, 0, message.length);
    writer.flush();

    Socket client = Mockito.mock(Socket.class);
    OutputStream stream = Mockito.mock(OutputStream.class);
    when(server.accept()).thenReturn(client);
    when(client.getOutputStream()).thenReturn(stream);

    doThrow(IOException.class).when(stream).write(any(byte[].class), anyInt(), anyInt());

    writer.run();

    verify(client, times(1)).close();
  }
}
