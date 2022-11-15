package com.jslib.log4j;

import org.apache.logging.log4j.ThreadContext;

import com.jslib.api.log.LogContext;

public class LogContextImpl implements LogContext
{
  @Override
  public void put(String name, String value)
  {
    if(name == null || name.isEmpty()) {
      return;
    }
    if(value != null) {
      ThreadContext.put(name, value);
    }
    else {
      ThreadContext.remove(name);
    }
  }

  @Override
  public String get(String name)
  {
    return ThreadContext.get(name);
  }

  @Override
  public void clear()
  {
    ThreadContext.clearAll();
  }
}
