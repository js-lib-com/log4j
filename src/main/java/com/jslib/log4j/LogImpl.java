package com.jslib.log4j;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import com.jslib.api.log.Log;

/**
 * Thin wrapper on Apache log4j {@link Logger}, implementing j(s)-lib {@link Log} interface. Apache log4j engine should
 * be properly initialized for this class to actually record any logging message.
 * 
 * @author Iulian Rotaru
 */
final class LogImpl implements Log
{
  /** Underlying Apache log4j logger delegated for actual logging record writing. */
  private final Logger logger;

  public LogImpl(Logger logger)
  {
    this.logger = logger;
  }

  @Override
  public void trace(String message, Object... args)
  {
    if(logger.isEnabled(Level.TRACE)) {
      logger.trace(message(message, args));
    }
  }

  @Override
  public void debug(String message, Object... args)
  {
    if(logger.isEnabled(Level.DEBUG)) {
      logger.debug(message(message, args));
    }
  }

  @Override
  public void info(String message, Object... args)
  {
    if(logger.isEnabled(Level.INFO)) {
      logger.info(message(message, args));
    }
  }

  @Override
  public void warn(String message, Object... args)
  {
    if(logger.isEnabled(Level.WARN)) {
      logger.warn(message(message, args));
    }
  }

  @Override
  public void warn(Throwable throwable)
  {
    if(logger.isEnabled(Level.WARN)) {
      logger.warn(throwable(throwable));
    }
  }

  @Override
  public void error(String message, Object... args)
  {
    if(logger.isEnabled(Level.ERROR)) {
      logger.error(message(message, args));
    }
  }

  @Override
  public void error(Throwable throwable)
  {
    if(logger.isEnabled(Level.ERROR)) {
      logger.error(throwable(throwable));
    }
  }

  @Override
  public void fatal(String message, Object... args)
  {
    if(logger.isEnabled(Level.FATAL)) {
      logger.fatal(message(message, args));
    }
  }

  @Override
  public void fatal(Throwable throwable)
  {
    if(logger.isEnabled(Level.FATAL)) {
      logger.fatal(throwable(throwable));
    }
  }

  @Override
  public void dump(Throwable throwable)
  {
    if(logger.isEnabled(Level.FATAL)) {
      logger.fatal("Stack trace dump:", throwable);
    }
  }

  @Override
  public void dump(String message, Throwable throwable)
  {
    if(logger.isEnabled(Level.FATAL)) {
      logger.fatal(message, throwable);
    }
  }

  private static String message(String message, Object... arguments)
  {
    if(message == null) {
      return null;
    }
    if(arguments.length == 0) {
      return message;
    }

    StringBuilder messageBuilder = new StringBuilder();

    // flag true while state machine is inside parameter, detected by '{' and lasting till '}'
    boolean parameter = false;

    for(int i = 0, argumentIndex = 0; i < message.length(); ++i) {
      char c = message.charAt(i);

      if(c == '%') {
        messageBuilder.append(format(message.substring(i), arguments));
        break;
      }

      // ignore parameter name that is not supported by log4j, if any present
      if(parameter) {
        if(c == '}') {
          parameter = false;
          // parameter without argument just print original text
          if(argumentIndex++ >= arguments.length) {
            messageBuilder.append(c);
          }
        }
        continue;
      }

      if(c == '{') {
        parameter = true;
        if(argumentIndex < arguments.length) {
          // if argument is present replace {} with argument string representation
          messageBuilder.append(arguments[argumentIndex]);
        }
        else {
          // otherwise append {
          messageBuilder.append(c);
        }
        continue;
      }

      // text content
      messageBuilder.append(c);
    }

    return messageBuilder.toString();
  }

  private static String throwable(Throwable throwable)
  {
    if(throwable == null) {
      return null;
    }
    if(throwable.getCause() == null) {
      String s = throwable.getMessage();
      return s != null? s: throwable.getClass().getCanonicalName();
    }

    int nestingLevel = 0;
    StringBuilder sb = new StringBuilder();
    for(;;) {
      sb.append(throwable.getClass().getName());
      sb.append(":");
      sb.append(" ");
      if(++nestingLevel == 8) {
        sb.append("...");
        break;
      }
      if(throwable.getCause() == null) {
        String s = throwable.getMessage();
        if(s == null) {
          s = throwable.getClass().getCanonicalName();
        }
        sb.append(s);
        break;
      }
      throwable = throwable.getCause();
    }
    return sb.toString();
  }
  
  /**
   * Return formatted string with arguments injected or original message if format or arguments are invalid. This method
   * does not throw exception on bad format; it simply returns original message.
   * <p>
   * This method takes care to pre-process arguments as follow:
   * <ul>
   * <li>replace {@link Class} with its canonical name,
   * <li>replace {@link Throwable} with exception message or exception class canonical name if null message,
   * <li>replace {@link Thread} with concatenation of thread name and thread ID,
   * <li>replace {@link File} with file absolute path,
   * <li>dump first 3 items from arrays like argument.
   * </ul>
   * All pre-processed arguments are replaced with string value and format specifier should be also string (%s).
   * 
   * @param message formatted message,
   * @param args variable number of formatting arguments.
   * @return built string or original message if format or arguments are not valid.
   */
  private static final String format(String message, Object... args)
  {
    if(message == null) {
      return null;
    }
    if(message.isEmpty()) {
      return "";
    }
    if(args.length == 0) {
      return message;
    }

    for(int i = 0; i < args.length; i++) {
      // at this point args[i] could be null
      if(args[i] == null) {
        continue;
      }

      if(args[i] instanceof Class) {
        args[i] = ((Class<?>)args[i]).getCanonicalName();
      }
      else if(args[i] instanceof Throwable) {
        String s = ((Throwable)args[i]).getMessage();
        if(s == null) {
          s = args[i].getClass().getCanonicalName();
        }
        args[i] = s;
      }
      else if(args[i] instanceof Thread) {
        Thread thread = (Thread)args[i];
        StringBuilder sb = new StringBuilder();
        sb.append(thread.getName());
        sb.append(':');
        sb.append(thread.getId());
        args[i] = sb.toString();
      }
      else if(args[i] instanceof File) {
        args[i] = ((File)args[i]).getAbsolutePath();
      }
      else if(isArrayLike(args[i])) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int index = 0;
        for(Object object : iterable(args[i])) {
          if(object == null) {
            continue;
          }
          String value = object instanceof String ? (String)object : object.toString();
          if(value.isEmpty()) {
            continue;
          }
          if(index++ > 0) {
            sb.append(',');
          }
          if(index == 4) {
            sb.append("...");
            break;
          }
          sb.append(object);
        }
        sb.append(']');
        args[i] = sb.toString();
      }
    }

    try {
      return String.format(message, args);
    }
    catch(Throwable unused) {
      // return unformatted message if format fails
      return message;
    }
  }

  /** Ellipsis constant. */
  private static final String ELLIPSIS = "...";

  /**
   * Ensure message is not larger than requested maximum length. If message length is larger than allowed size shorten
   * it and append ellipsis. This method guarantee maximum length is not exceed also when ellipsis is appended.
   * <p>
   * This method returns given <code>message</code> argument if smaller that requested maximum length or a new created
   * string with trailing ellipsis if larger.
   * 
   * @param message message string, possible null,
   * @param maxLength maximum allowed space.
   * @return given <code>message</code> argument if smaller that requested maximum length or new created string with
   *         trailing ellipsis.
   */
  protected static final String ellipsis(String message, int maxLength)
  {
    if(message == null) {
      return "null";
    }
    return message.length() < maxLength ? message : message.substring(0, maxLength - ELLIPSIS.length()) + ELLIPSIS;
  }

  /**
   * An object is array like if is an actual array or a collection.
   * 
   * @param object object to test if array like.
   * @return true if <code>object</code> argument is array like.
   */
  private static final boolean isArrayLike(Object object)
  {
    return object != null && (object.getClass().isArray() || object instanceof Collection);
  }

  /**
   * Create an iterator supplied via Iterable interface. If <code>object</code> argument is a collection just returns it
   * since collection is already iterbale. If is array, create an iterator able to traverse generic arrays.
   * 
   * @param object collection or array.
   * @return Iterable instance.
   */
  private static final Iterable<?> iterable(final Object object)
  {
    // at this point object cannot be null and is array or collection

    if(object instanceof Iterable) {
      return (Iterable<?>)object;
    }

    // at this point object is an array
    // create a iterator able to traverse generic arrays

    return new Iterable<Object>()
    {
      private Object array = object;
      private int index;

      @Override
      public Iterator<Object> iterator()
      {
        return new Iterator<Object>()
        {
          @Override
          public boolean hasNext()
          {
            return index < Array.getLength(array);
          }

          @SuppressWarnings("unqualified-field-access")
          @Override
          public Object next()
          {
            return Array.get(array, index++);
          }

          @Override
          public void remove()
          {
            throw new UnsupportedOperationException("Array iterator has no remove operation.");
          }
        };
      }
    };
  }
}
