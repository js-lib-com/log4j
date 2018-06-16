package js.log4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.converter.ConverterException;
import js.converter.ConverterRegistry;
import js.lang.Config;
import js.lang.ConfigException;
import js.log.Log;
import js.log.LogContext;
import js.log.LogLevel;
import js.log.LogProvider;
import js.util.Strings;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.OptionHandler;

/**
 * Logging manager initialize and configure underlying log4j implementation. Current js-lib logging implementation is
 * based on log4j. This class creates needed log4j instances and configure them. Configuration file is stored into
 * <code>$catalina.base/conf/log.xml</code> and has a proprietary format. Here is a sample configuration file, see
 * {@link #config(Config)}, {@link #createAppender(Config)} and {@link #createLogger(Config, Map)} for details.
 * <p>
 * This configuration declares three appenders user to write logging records to: a remote console, standard out and a
 * file from logs directory. Also declares two loggers: one for all classes belonging to <code>js</code> package and
 * sub-packages and the second for <code>net.dots</code>. One may note <code>${logs}</code> variable into
 * <code>file</code> parameter from file appender. It is a log4j syntax for system properties. In this particular case
 * <code>${logs}</code> system property is initialized to log files directory.
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;log&gt;
 *         &lt;appender name="CON"&gt;
 *                 &lt;class&gt;js.util.log.RemoteConsoleAppender&lt;/class&gt;
 *                 &lt;format&gt;%d{dd HH:mm:ss,SSS} [%t] %-5p %c %x- %m%n&lt;/format&gt;
 *                 &lt;parameters&gt;
 *                         &lt;port&gt;8001&lt;/port&gt;
 *                 &lt;/parameters&gt;
 *         &lt;/appender&gt;
 * 
 *         &lt;appender name="STD"&gt;
 *                 &lt;class&gt;org.apache.log4j.ConsoleAppender&lt;/class&gt;
 *                 &lt;format&gt;%d{dd HH:mm:ss,SSS} [%t] %-5p %c %x- %m%n&lt;/format&gt;
 *         &lt;/appender&gt;
 * 
 *         &lt;appender name="APP"&gt;
 *                 &lt;class&gt;org.apache.log4j.RollingFileAppender&lt;/class&gt;
 *                 &lt;format&gt;%d{dd HH:mm:ss,SSS} [%t] %-5p %c %x- %m%n&lt;/format&gt;
 *                 &lt;parameters&gt;
 *                         &lt;encoding&gt;UTF-8&lt;/encoding&gt;
 *                         &lt;file&gt;${logs}/app.log&lt;/file&gt;
 *                         &lt;max-file-size&gt;10MB&lt;/max-file-size&gt;
 *                         &lt;max-backup-index&gt;10&lt;/max-backup-index&gt;
 *                         &lt;immediate-flush&gt;true&lt;/immediate-flush&gt;
 *                 &lt;/parameters&gt;
 *         &lt;/appender&gt;
 * 
 *         &lt;logger name="js"&gt;
 *                 &lt;appender&gt;CON&lt;/appender&gt;
 *                 &lt;appender&gt;STD&lt;/appender&gt;
 *                 &lt;level&gt;OFF&lt;/level&gt;
 *         &lt;/logger&gt;
 * 
 *         &lt;logger name="net.dots"&gt;
 *                 &lt;appender&gt;CON&lt;/appender&gt;
 *                 &lt;appender&gt;STD&lt;/appender&gt;
 *                 &lt;appender&gt;APP&lt;/appender&gt;
 *                 &lt;level&gt;INFO&lt;/level&gt;
 *         &lt;/logger&gt;
 * &lt;/log&gt;
 * </pre>
 * <p>
 * Declaring appenders only is not enough. Without declaring loggers there is no log message recorded. The key of
 * configuration is to declare loggers, that depend on appenders. Although in sample appenders are grouped and declared
 * before loggers, sections order does not matter.
 * 
 * @author Iulian Rotaru
 * @version draft
 */
public final class LogProviderImpl implements LogProvider
{
  private volatile boolean configured;
  private LogContext logContext = new LogContextImpl();

  /**
   * Configure this server logging subsystem from given configuration. js-lib logging is based on log4j. This method
   * takes care to create needed log4j instances and initialize them from given configuration.
   * <p>
   * Logging configuration is based on log4j abstractions, appender and logger but have a proprietary syntax. Therefore
   * there are two majors configuration elements: <code>appender</code> and <code>logger</code>. See
   * {@link #createAppender(Config)} and {@link #createLogger(Config, Map)} for description. Below is a simplified
   * logging configuration sample file, for a global picture.
   * 
   * <pre>
   *    &lt;log&gt;
   *        &lt;appender name="SERVER"&gt;
   *            &lt;class&gt;org.apache.log4j.RollingFileAppender&lt;/class&gt;
   *            &lt;format&gt;%d{dd HH:mm:ss,SSS} [%t] %-5p %c %x- %m%n&lt;/format&gt;
   *            &lt;parameters&gt;
   *                &lt;file&gt;server.log&lt;/file&gt;
   *                &lt;max-file-size&gt;10MB&lt;/max-file-size&gt;
   *                &lt;max-backup-index&gt;10&lt;/max-backup-index&gt;
   *            &lt;/parameters&gt;
   *        &lt;/appender&gt;
   * 
   *        &lt;logger name="org.apache"&gt;
   *            &lt;appender&gt;SERVER&lt;/appender&gt;
   *            &lt;level&gt;DEBUG&lt;/level&gt;
   *        &lt;/logger&gt;
   *    &lt;/log&gt;
   * </pre>
   * <p>
   * Note that log4j root logger is created internally using all appenders and level <code>OFF</code>. So, if no logger
   * defined no logging event is recorded. Also, logging is disabled if this method is not called.
   * 
   * @param config configuration object.
   * @throws ConfigException if configuration object is not well formed.
   */
  public void config(Config config) throws ConfigException
  {
    if(System.getProperty("logs") == null) {
      throw new ConfigException("Missing <logs> system property required for log4j implementation.");
    }
    ConverterRegistry.getInstance().registerConverter(Priority.class, Log4jPriorityConverter.class);

    Map<String, Appender> appenders = new HashMap<String, Appender>();
    for(Config element : config.findChildren("appender")) {
      Appender appender = createAppender(element);
      appenders.put(appender.getName(), appender);
    }

    // logger config is invoked from server class init that is called from servlet context listener
    // there may be loggers created before this config is executed and those are using default configuration
    // if this is the case, remove all default appenders and create those from this config

    Logger rootLogger = Logger.getRootLogger();
    if(configured) {
      rootLogger.removeAllAppenders();
    }
    configured = true;

    for(Appender appender : appenders.values()) {
      rootLogger.addAppender(appender);
    }
    rootLogger.setLevel(Level.OFF);

    for(Config element : config.findChildren("logger")) {
      createLogger(element, appenders);
    }
  }

  @Override
  public Log getLogger(String loggerName)
  {
    if(!configured) {
      configDefault();
    }
    return new LogImpl(loggerName);
  }

  @Override
  public LogContext getLogContext()
  {
    return logContext;
  }

  // ----------------------------------------------------

  /**
   * Default log4j configuration used when external configuration is not performed. This is a minimal configuration that
   * append to console all logging levels.
   */
  private void configDefault()
  {
    configured = true;

    ConsoleAppender appender = new ConsoleAppender();
    appender.setLayout(new PatternLayout("%d{dd HH:mm:ss,SSS} [%t] %-5p %c %x- %m%n"));
    appender.activateOptions();

    Logger rootLogger = Logger.getRootLogger();
    rootLogger.addAppender(appender);
    rootLogger.setLevel(Level.ERROR);
  }

  /**
   * Create log4j appender instance and initialize it from js-lib configuration <code>element</code>. Appender has a
   * <code>name</code> used to map appenders to loggers. Appenders map given to {@link #createLogger(Config, Map)} uses
   * this appender <code>name</code> as key.
   * <p>
   * Every appender has a class identifying implementation and a message format describing log record. Formatting text
   * is passed as it is to {@link PatternLayout}, from log4j implementation.
   * 
   * <pre>
   * &lt;appender name="SERVER"&gt;
   * 	&lt;class&gt;org.apache.log4j.RollingFileAppender&lt;/class&gt;
   * 	&lt;format&gt;%d{dd HH:mm:ss,SSS} [%t] %-5p %c %x- %m%n&lt;/format&gt;
   * 	&lt;parameters&gt;
   * 		&lt;file&gt;${logs}/server.log&lt;/file&gt;
   * 		&lt;max-file-size&gt;10MB&lt;/max-file-size&gt;
   * 		&lt;max-backup-index&gt;10&lt;/max-backup-index&gt;
   * 	&lt;/parameters&gt;
   * &lt;/appender&gt;
   * </pre>
   * <p>
   * An appender can also have a variable number of parameters, specific to appender class. See appender class API for
   * supported parameters and parameters type. Parameter element name is converted to setter name, see
   * {@link #getSetterName(String)} and element value used to set appender parameter. For example in above configuration
   * sample, invoke RollingFileAppender#setFile("${logs}/server.log"); One may notice <code>${logs}</code> variable. It
   * is a log4j notation for system properties.
   * 
   * @param appenderConfig appender configuration element.
   * @return newly created appender instance.
   * @throws ConfigException if appender creation fails but not necessarily because of bad configuration.
   */
  @SuppressWarnings("unchecked")
  private static Appender createAppender(Config appenderConfig) throws ConfigException
  {
    String appenderName = appenderConfig.getAttribute("name");
    if(appenderName == null) {
      throw new ConfigException("Missing <name> attribute from appender.");
    }
    String className = appenderConfig.getChildValue("class");
    if(className == null) {
      throw new ConfigException("Missing <class> element from appender |%s|.", appenderName);
    }

    Class<? extends Appender> appenderClass;
    try {
      appenderClass = (Class<? extends Appender>)Class.forName(className);
    }
    catch(ClassNotFoundException e) {
      throw new ConfigException("Appender class |%s| not found.", className);
    }

    Appender appender;
    try {
      appender = appenderClass.newInstance();
    }
    catch(Exception e) {
      throw new ConfigException(e);
    }
    appender.setName(appenderName);

    String format = appenderConfig.getChildValue("format");
    if(format != null) {
      appender.setLayout(new PatternLayout(format));
    }

    Config parameters = appenderConfig.getChild("parameters");
    if(parameters != null) {
      String setterName = null;
      for(Config parameterConfig : parameters.getChildren()) {
        String parameterName = parameterConfig.getName();
        setterName = getSetterName(parameterName);

        Method setter = getAppenderSetter(appenderClass, setterName);
        if(!Modifier.isPublic(setter.getModifiers())) {
          throw new ConfigException("Private setter |%s| on appender |%s|.", setterName, className);
        }

        Class<?>[] formalParameters = setter.getParameterTypes();
        if(formalParameters.length != 1) {
          throw new ConfigException("Bad format parameters count |%d| for setter |%s| on appender |%s|.", formalParameters.length, setterName, className);
        }

        setAppenderParameter(appender, setter, getParameterValue(parameterConfig, formalParameters[0]));
      }
    }

    if(appender instanceof OptionHandler) {
      ((OptionHandler)appender).activateOptions();
    }
    return appender;
  }

  /**
   * Create log4j logger instance, add appenders and set logger level. See below for sample of logger configuration
   * element. A logger should have one or many appenders and a level. It is considered bad configuration if appender or
   * level is missing. Also appender value should match a configured appender, that is, found in <code>appenders</code>
   * map. Level value should be enumerated by {@link LogLevel}.
   * <p>
   * Logger <code>name</code> obeys log4j convention: all logger instances that starts with given <code>name</code> are
   * subject to this configuration element. In sample, all classes from <code>org.apache</code> package and all
   * sub-packages have DEBUG level and write to SERVER and CONSOLE appenders.
   * 
   * <pre>
   * &lt;logger name="org.apache"&gt;
   * 	&lt;appender&gt;SERVER&lt;/appender&gt;
   * 	&lt;appender&gt;CONSOLE&lt;/appender&gt;
   * 	&lt;level&gt;DEBUG&lt;/level&gt;
   * &lt;/logger&gt;
   * </pre>
   * <p>
   * For appenders map configuration see {@link #createAppender(Config)}. Finally, created logger has appender
   * additivity set to false so logger writes only to appenders explicitly configured.
   * 
   * @param config logger configuration element,
   * @param appenders appenders map, configured per logging system.
   * @return newly created and initialized log4j logger.
   * @throws ConfigException if configured appender is missing or is not present into given <code>appenders</code> list
   *           or level is missing.
   */
  private static Logger createLogger(Config config, Map<String, Appender> appenders) throws ConfigException
  {
    String loggerName = config.getAttribute("name");
    Logger logger = Logger.getLogger(loggerName);
    logger.setAdditivity(false);

    List<Config> appenderElements = config.findChildren("appender");
    if(appenderElements.isEmpty()) {
      throw new ConfigException("Invalid logger |%s|. Missing appender(s).", loggerName);
    }

    for(Config appenderElement : appenderElements) {
      String appenderName = appenderElement.getValue();
      if(appenderName == null) {
        throw new ConfigException("Invalid logger |%s|. Empty appender.", loggerName);
      }
      Appender appender = appenders.get(appenderName);
      if(appender == null) {
        throw new ConfigException("Invalid logger |%s|. Refered appender |%s| is not defined.", loggerName, appenderName);
      }
      logger.addAppender(appender);
    }

    Config levelElement = config.getChild("level");
    if(levelElement == null) {
      throw new ConfigException("Invalid logger |%s|. Missing logger level.", loggerName);
    }
    String levelName = levelElement.getValue();
    if(levelName == null) {
      throw new ConfigException("Invalid logger |%s|. Empty level.", loggerName);
    }
    LogLevel logLevel = null;
    try {
      logLevel = LogLevel.valueOf(levelName);
    }
    catch(Exception e) {
      throw new ConfigException("Invalid logger |%s|. Bad logger level value |%s|.", loggerName, levelName);
    }
    logger.setLevel(LevelMap.log4jLevel(logLevel));

    return logger;
  }

  /**
   * Get named setter method for appender class or its super-hierarchy, throwing exception if not found. This method
   * tries to locate named method into appender class or all super-classes till {@link Object}. Missing method is
   * considered bad configuration and throws exception.
   * 
   * @param appenderClass appender class,
   * @param setterName setter method name,
   * @return appender setter method.
   * @throws ConfigException if named setter method is not found.
   */
  @SuppressWarnings("unchecked")
  private static Method getAppenderSetter(Class<? extends Appender> appenderClass, String setterName) throws ConfigException
  {
    // not optimal but cannot access method by name since do not know formal parameters
    for(Method method : appenderClass.getMethods()) {
      if(method.getName().equals(setterName)) {
        if(method.getParameterTypes().length == 1) {
          return method;
        }
      }
    }
    Class<?> superClass = appenderClass.getSuperclass();
    if(!superClass.equals(Object.class)) {
      throw new ConfigException("Missing setter |%s| method from log4j appender |%s|.", setterName, appenderClass);
    }
    // do not use recursive calls counter since base Object is never too far
    return getAppenderSetter((Class<? extends Appender>)superClass, setterName);
  }

  /**
   * Set parameter value to log4j appender instance. This method is meant to invoke appender setter by name not to
   * access private ones. When this method is invoked appender setter is guaranteed to exist - so no need to be worried
   * about log4j API changes.
   * 
   * @param appender log4j appender instance,
   * @param setter existing setter method,
   * @param parameter appender parameter value.
   * @throws ConfigException if appender setter execution fails.
   */
  private static void setAppenderParameter(Appender appender, Method setter, Object parameter) throws ConfigException
  {
    try {
      setter.invoke(appender, new Object[]
      {
        parameter
      });
    }
    catch(InvocationTargetException e) {
      throw new ConfigException("Error executing setter |%s| on |%s|.", setter.getName(), appender.getClass().getCanonicalName());
    }
    catch(Exception e) {
      // here exception can be one of: SecurityException, IllegalAccessException or IllegalArgumentException
      // none of them are possible in this context
      throw new IllegalStateException(e);
    }
  }

  /**
   * Create method setter name from dashed parameter name. Parameter name is loaded from logger configuration and used
   * dash to separate words, if many. This method convert to camel case and prefix with <code>set</code>. For example,
   * returns <code>setParameterName</code> for <code>parameter-name</code>.
   * 
   * @param parameterName dashed parameter name.
   * @return method setter.
   */
  private static String getSetterName(String parameterName)
  {
    StringBuilder sb = new StringBuilder("set");
    // first character from parameter name should be promoted to upper case since will follow after 'set' prefix
    boolean upperCase = true;
    for(int i = 0; i < parameterName.length(); ++i) {
      char c = parameterName.charAt(i);
      if(c == '-') {
        upperCase = true;
        continue;
      }
      sb.append(upperCase ? Character.toUpperCase(c) : c);
      upperCase = false;
    }
    return sb.toString();
  }

  /**
   * Get parameter value from configuration object converted to requested type. This method also scans for standard
   * <code>${...}</code> variable pattern and replace with system property. This feature is especially added for log4j
   * logging directory that is stored into <code>${logs}</code> system environment variable. Anyway, implementation is
   * generic to allow for any variable name.
   * 
   * @param parameterConfig parameter configuration object,
   * @param type value type.
   * @param <T> object instance type.
   * @return value object of requested type.
   * @throws ConfigException if variable exist but is not well formed.
   * @throws ConverterException if there is no converter registered for requested type or string conversion fails.
   */
  private static <T> T getParameterValue(Config parameterConfig, Class<T> type) throws ConfigException
  {
    String text = parameterConfig.getValue();
    String variableName = getVariableName(text);
    if(variableName != null) {
      String variableValue = System.getProperty(variableName);
      if(variableValue != null) {
        text = text.replace(Strings.concat("${", variableName, '}'), variableValue);
      }
    }
    return ConverterRegistry.getConverter().asObject(text, type);
  }

  /**
   * Get variable name from text or null if no variable found. Scan for standard <code>${...}</code> variable pattern
   * and return variable name. Returned variable name does not contain variable mark-up. This method assume there is at
   * most one single variable; if more just return the first one. If none returns null.
   * 
   * @param text text to scan for variable.
   * @return variable name or null.
   * @throws ConfigException if variable start mark-up was found but no closing.
   */
  private static String getVariableName(String text) throws ConfigException
  {
    int variableStartIndex = text.indexOf("${");
    if(variableStartIndex == -1) {
      return null;
    }
    int variableEndIndex = text.indexOf('}', variableStartIndex);
    if(variableEndIndex == -1) {
      throw new ConfigException("Bad attribute value |%s|. Missing variable end mark.", text);
    }
    return text.substring(variableStartIndex + 2, variableEndIndex);
  }
}
