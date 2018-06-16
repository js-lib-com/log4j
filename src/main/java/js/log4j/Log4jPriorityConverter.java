package js.log4j;

import org.apache.log4j.Priority;

import js.converter.Converter;
import js.converter.ConverterException;

public class Log4jPriorityConverter implements Converter
{
  @SuppressWarnings({
      "deprecation", "unchecked"
  })
  @Override
  public <T> T asObject(String string, Class<T> valueType) throws IllegalArgumentException, ConverterException
  {
    return (T)Priority.toPriority(string);
  }

  @Override
  public String asString(Object object) throws ConverterException
  {
    return object.toString();
  }
}
