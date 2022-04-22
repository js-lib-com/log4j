package js.log4j;

import static java.lang.String.format;
 
import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import js.log.LogLevel;

/**
 * Implementation for management bean.
 * 
 * @author Iulian Rotaru
 */
public class Log4jMXBeanImpl implements Log4jMXBean
{
  public static final String MX_BEAN_NAME = "com.js-lib:type=Log4j";

  private static Log4jMXBean instance;

  /** Create managed bean singleton instance. */
  public static void create()
  {
    if(instance == null) {
      synchronized(Log4jMXBeanImpl.class) {
        if(instance == null) {

          instance = new Log4jMXBeanImpl();
          ObjectName objName = null;
          try {
            objName = new ObjectName(MX_BEAN_NAME);
          }
          catch(MalformedObjectNameException e) {
            throw new IllegalStateException(format("Invalid hard coded MX bean name |%s|.", MX_BEAN_NAME));
          }

          if(objName != null) {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            try {
              if(!server.isRegistered(objName)) {
                server.registerMBean(instance, objName);
              }
            }
            catch(InstanceAlreadyExistsException e) {
              throw new IllegalStateException(format("Attempt to recreate MX bean |%s| singleton instance.", Log4jMXBean.class));
            }
            catch(MBeanRegistrationException e) {
              throw new IllegalStateException(format("MX bean |%s| registration exception: %s", Log4jMXBean.class, e));
            }
            catch(NotCompliantMBeanException e) {
              throw new IllegalStateException(format("Invalid MX bean |%s| format.", Log4jMXBean.class));
            }
          }
        }
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setRootLevel(String level)
  {
    setLevel(LogManager.getRootLogger(), level);
  }

  /** {@inheritDoc} */
  @Override
  public String getRootLevel()
  {
    return getLevel(LogManager.getRootLogger());
  }

  /** {@inheritDoc} */
  @Override
  public void setLevel(String name, String level)
  {
    if(name != null) {
      setLevel(LogManager.exists(name), level);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String getLevel(String name)
  {
    return name != null ? getLevel(LogManager.exists(name)) : null;
  }

  /**
   * Set underlying <code>log4j</code> logger level. Logger level should be a valid {@link LogLevel} name. If logger or
   * level arguments are null or invalid log level name this method does nothing.
   * 
   * @param logger <code>log4j</code> logger, null ignored,
   * @param level name of the <code>j(s)-lib</code> log level, null ignored.
   */
  private static void setLevel(Logger logger, String level)
  {
    if(logger != null && level != null) {
      try {
        logger.setLevel(LevelMap.log4jLevel(LogLevel.valueOf(level)));
      }
      catch(IllegalArgumentException unused) {}
    }
  }

  /**
   * Return the name of the <code>j(s)-lib</code> logger level, possible null if given logger has no level set. Please
   * note that this getter return logger set level, not the level inherited from its ancestors.
   * 
   * @param logger underlying <code>log4j</code> logger, null ignored.
   * @return <code>j(s)-lib</code> log level name.
   */
  private static String getLevel(Logger logger)
  {
    if(logger != null) {
      Level level = logger.getLevel();
      if(level == null) {
        return null;
      }
      LogLevel logLevel = LevelMap.logLevel(level);
      return logLevel != null ? logLevel.name() : null;
    }
    return null;
  }
}
