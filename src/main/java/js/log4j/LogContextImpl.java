package js.log4j;

import org.apache.log4j.MDC;

import js.log.LogContext;

public class LogContextImpl implements LogContext
{
  @Override
  public void put(String name, Object value)
  {
    if(name == null || name.isEmpty()) {
      return;
    }
    if(value != null) {
      MDC.put(name, value);
    }
    else {
      MDC.remove(name);
    }
  }

  @Override
  public void clear()
  {
    MDC.clear();
  }
}
