package js.log4j;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.status.StatusLogger;

/**
 * Remote console writer. Send logging messages to connected remote console; if none connected messages are stored up to
 * {@link #QUEUE_CAPACITY} then ignored.
 * <p>
 * Although this class is a sender acting like a client is implemented as a server: there is a separated thread with a
 * socket listening for connections from remote client. When client is connected enter a loop waiting for messages on
 * queue and send them. There is an Eclipse plugin enabling server log messages display on IDE console; note that this
 * server implementation allows only one connection at a time.
 * 
 * @author Iulian Rotaru
 */
public class RemoteConsoleWriter extends Writer implements Runnable
{
  /** Shutdown message used to stop socket server. */
  private static final String SHUTDOWN = "SD";

  /** Socket server thread stop timeout. */
  private static final int SHUTDOWN_TIMEOUT = 2000;

  /** Log messages queue capacity. */
  private static final int QUEUE_CAPACITY = 1000;

  /** String builder for temporary log messages storage. */
  private final StringBuilder builder = new StringBuilder();

  /** Log messages queue. */
  private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>(QUEUE_CAPACITY);

  /** Socket server listening port. */
  private final int port;

  /** Server socket waiting for remote client connection. Only one client at a time is accepted. */
  private final ServerSocket server;

  /** Thread running the server. */
  private final Thread thread;

  /**
   * Create remote console writer instance.
   * 
   * @param port remote console port.
   */
  public RemoteConsoleWriter(int port)
  {
    debug("Create remote console server instance.");

    ServerSocket server = null;
    Thread thread = null;
    try {
      server = new ServerSocket();
      thread = new Thread(this);
      thread.setDaemon(true);
      thread.start();
    }
    catch(IOException e) {
      error("Error creating the server. Remote console writer is unable to process appender messages.");
    }

    this.port = port;
    this.server = server;
    this.thread = thread;
  }

  /**
   * Test constructor.
   * 
   * @param server server socket mock.
   */
  public RemoteConsoleWriter(ServerSocket server)
  {
    this.port = 0;
    this.thread = null;
    this.server = server;
  }

  public BlockingQueue<String> getQueue()
  {
    return queue;
  }

  /**
   * Writes a portion of an array of characters.
   */
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException
  {
    builder.append(cbuf, off, len);
  }

  /**
   * Flushes this stream by writing any buffered output to the underlying stream.
   */
  @Override
  public void flush() throws IOException
  {
    for(;;) {
      int index = indexOneOf(builder, '\r', '\n');
      if(index == -1) {
        break;
      }
      // ignore false returned by offer when queue is full
      queue.offer(builder.substring(0, index));

      if(builder.charAt(index) == '\r') {
        ++index;
      }
      if(builder.charAt(index) != '\n') {
        throw new IllegalStateException();
      }
      ++index;
      builder.delete(0, index);
    }
  }

  private static int indexOneOf(CharSequence string, char... chars)
  {
    for(int i = 0; i < string.length(); ++i) {
      for(int j = 0; j < chars.length; ++j) {
        if(string.charAt(i) == chars[j]) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Closes the stream, flushing it first.
   */
  @Override
  public void close() throws IOException
  {
    debug("Dispose remote console server.");
    // this method tries its best and as a least resort uses thread interruption
    synchronized(this) {
      try {
        // try to break server.accept in order to force queue reading on thread
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(port), 500);
        socket.close();
      }
      catch(IOException e) {
        error(e);
      }

      queue.clear();
      queue.offer(SHUTDOWN);

      try {
        wait(SHUTDOWN_TIMEOUT);
      }
      catch(InterruptedException unused) {
        thread.interrupt();
      }
    }
  }

  /**
   * Implement runnable interface.
   */
  @Override
  public void run()
  {
    debug("Start remote console thread |%s|.", Thread.currentThread().getId());

    try {
      server.setReuseAddress(true);
      server.bind(new InetSocketAddress(port));
    }
    catch(IOException e) {
      throw new IllegalStateException(String.format("Fail to create remote console server socket on port |%d|. Abort execution thread.", port));
    }

    debug("Open remote console server for listening on |%s:%d|. Waiting for console client.", server.getInetAddress(), port);
    SERVER_LOOP: for(;;) {
      if(Thread.interrupted()) {
        debug("Server thread has been interrupted. Exit server loop.");
        break;
      }

      Socket client;
      try {
        client = server.accept();
      }
      catch(InterruptedIOException unused) {
        debug("Server waiting for connection was interrupted.");
        continue;
      }
      catch(IOException e) {
        error(e);
        continue;
      }
      debug("Open connection with remote console from |%s|.", client.getRemoteSocketAddress());

      PrintStream stream;
      try {
        stream = new PrintStream(client.getOutputStream());
      }
      catch(IOException e) {
        error(e);
        continue;
      }

      for(;;) {
        String message = null;
        try {
          message = queue.take();
        }
        catch(InterruptedException unused) {
          Thread.currentThread().interrupt();
        }
        if(Thread.interrupted()) {
          break;
        }
        if(message == null) {
          continue;
        }
        if(message.equals(SHUTDOWN)) {
          break SERVER_LOOP;
        }

        stream.print(message);
        stream.print("\r\n");
        if(stream.checkError()) {
          debug("Remote console has been closed. Stop messages transmission.");
          break;
        }
      }

      stream.close();
      try {
        client.close();
      }
      catch(IOException e) {
        error(e);
      }
    }

    try {
      server.close();
    }
    catch(IOException e) {
      error(e);
    }

    // notify main thread so that shutdown method can finish
    synchronized(this) {
      notify();
    }
    debug("Remote console server thread |%s| finished.", Thread.currentThread().getId());
  }

  // internal logging helpers
  // uses log4j internal logger LogLog in order to avoid circular dependencies

  /**
   * Internal debug message.
   * 
   * @param message debug message,
   * @param args optional formatted message arguments.
   */
  private static void debug(String message, Object... args)
  {
    StatusLogger.getLogger().debug(String.format(message, args));
  }

  /**
   * Internal error message.
   * 
   * @param message error message,
   * @param args optional formatted message arguments.
   */
  private static void error(Object message, Object... args)
  {
    StatusLogger.getLogger().error(String.format(message.toString(), args));
  }
}