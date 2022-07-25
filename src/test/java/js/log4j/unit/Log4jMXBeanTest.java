package js.log4j.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;

import js.log.Log;
import js.log.LogFactory;
import js.log4j.Log4jMXBean;
import js.log4j.Log4jMXBeanImpl;

public class Log4jMXBeanTest
{
  private static final String[] LEVELS = new String[]
  {
      "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"
  };

  private Log4jMXBean bean;

  @Before
  public void beforeTest()
  {
    bean = new Log4jMXBeanImpl();
  }

  @Test
  public void rootLevel()
  {
    String rootLevel = bean.getRootLevel();
    assertThat(rootLevel, notNullValue());

    for(String level : LEVELS) {
      bean.setRootLevel(level);
      assertThat(bean.getRootLevel(), equalTo(level));
    }
  }

  @Test
  public void level()
  {
    final String LOGGER_NAME = "test-logger";

    Log log = LogFactory.getLog(LOGGER_NAME);
    assertThat(log, notNullValue());
    assertThat(bean.getLevel(LOGGER_NAME), equalTo("OFF"));

    for(String level : LEVELS) {
      bean.setLevel(LOGGER_NAME, level);
      assertThat(bean.getLevel(LOGGER_NAME), equalTo(level));
    }
  }
}
