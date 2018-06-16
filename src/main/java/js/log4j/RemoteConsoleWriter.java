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

import org.apache.log4j.helpers.LogLog;

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
final class RemoteConsoleWriter extends Writer implements Runnable
{
  /**
   * Remote console print writer instance.
   */
  private static RemoteConsoleWriter instance;

  /**
   * Remote console print writer factory.
   * 
   * @param port optional port mandatory when create for the first time.
   * @return remote console print writer instance.
   */
  static Writer getInstance(int port)
  {
    if(instance == null) {
      instance = new RemoteConsoleWriter(port);
    }
    return instance;
  }

  static Writer getInstance()
  {
    assert instance != null;
    return instance;
  }

  /**
   * Shutdown message used to stop socket server.
   */
  private static final String SHUTDOWN = "SD";

  /**
   * Socket server thread stop timeout.
   */
  private static final int SHUTDOWN_TIMEOUT = 2000;

  /**
   * Log messages queue capacity.
   */
  private static final int QUEUE_CAPACITY = 1000;

  /**
   * Socket server listening port.
   */
  private int port;

  /**
   * String builder for temporary log messages storage.
   */
  private StringBuilder builder = new StringBuilder();

  /**
   * Log messages queue.
   */
  private BlockingQueue<String> queue;

  /**
   * Remote connection thread.
   */
  private Thread thread;

  /**
   * Create remote console writer instance.
   * 
   * @param port remote console port.
   */
  private RemoteConsoleWriter(int port)
  {
    debug("Create remote console server instance.");

    this.port = port;
    this.queue = new LinkedBlockingQueue<String>(QUEUE_CAPACITY);
    this.thread = new Thread(this);
    this.thread.setDaemon(true);
    this.thread.start();
  }

  /**
   * Writes a portion of an array of characters.
   */
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException
  {
    this.builder.append(cbuf, off, len);
  }

  /**
   * Flushes this stream by writing any buffered output to the underlying stream.
   */
  @Override
  public void flush() throws IOException
  {
    for(;;) {
      int index = indexOneOf(this.builder, '\r', '\n');
      if(index == -1) {
        break;
      }
      // ignore false returned by offer when queue is full
      this.queue.offer(this.builder.substring(0, index));

      if(this.builder.charAt(index) == '\r') {
        ++index;
      }
      if(this.builder.charAt(index) != '\n') {
        throw new IllegalStateException();
      }
      ++index;
      this.builder.delete(0, index);
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
        socket.connect(new InetSocketAddress(this.port), 500);
        socket.close();
      }
      catch(IOException e) {
        error(e);
      }

      this.queue.clear();
      this.queue.offer(SHUTDOWN);

      try {
        wait(SHUTDOWN_TIMEOUT);
      }
      catch(InterruptedException unused) {
        this.thread.interrupt();
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

    ServerSocket server;
    try {
      server = new ServerSocket();
      server.setReuseAddress(true);
      server.bind(new InetSocketAddress(this.port));
    }
    catch(IOException e) {
      throw new IllegalStateException(String.format("Fail to create remote console server socket on port |%d|. Abort execution thread.", this.port));
    }

    debug("Open remote console server for listening on |%s:%d|. Waiting for console client.", server.getInetAddress(), this.port);
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
          message = this.queue.take();
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
    LogLog.debug(String.format(message, args));
  }

  /**
   * Internal error message.
   * 
   * @param message error message,
   * @param args optional formatted message arguments.
   */
  private static void error(Object message, Object... args)
  {
    LogLog.error(String.format(message.toString(), args));
  }
}