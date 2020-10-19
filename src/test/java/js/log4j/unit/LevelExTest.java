package js.log4j.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.apache.log4j.Level;
import org.junit.Test;

import js.log4j.LevelEx;

public class LevelExTest
{
  @Test
  public void toLevel_String()
  {
    assertThat(LevelEx.toLevel("TRACE"), equalTo(LevelEx.TRACE));
    assertThat(LevelEx.toLevel("BUG"), equalTo(LevelEx.BUG));
  }

  /** If level name does not match, LevelEx delegates log4j Level that returns default DEBUG. */
  @Test
  public void toLevel_String_NoMatch()
  {
    assertThat(LevelEx.toLevel("FAKE"), equalTo(Level.DEBUG));
  }

  /** If level name argument is null, LevelEx delegates log4j Level that returns default DEBUG. */
  @Test
  public void toLevel_String_Null()
  {
    assertThat(LevelEx.toLevel(null), equalTo(Level.DEBUG));
  }

  @Test
  public void toLevel_Integer()
  {
    assertThat(LevelEx.toLevel(5001), equalTo(LevelEx.TRACE));
    assertThat(LevelEx.toLevel(50001), equalTo(LevelEx.BUG));
  }

  /** If level number does not match LevelEx delegate log4j Level that returns default DEBUG. */
  @Test
  public void toLevel_Integer_NoMatch()
  {
    assertThat(LevelEx.toLevel(1964), equalTo(Level.DEBUG));
  }

  @Test
  public void toLevel_String_Default()
  {
    assertThat(LevelEx.toLevel("TRACE", Level.INFO), equalTo(LevelEx.TRACE));
    assertThat(LevelEx.toLevel("BUG", Level.INFO), equalTo(LevelEx.BUG));
  }

  @Test
  public void toLevel_String_Default_NoMatch()
  {
    assertThat(LevelEx.toLevel("FAKE", Level.INFO), equalTo(Level.INFO));
  }

  @Test
  public void toLevel_String_Default_Null()
  {
    assertThat(LevelEx.toLevel(null, Level.INFO), equalTo(Level.INFO));
  }

  @Test
  public void toLevel_Integer_Default()
  {
    assertThat(LevelEx.toLevel(5001, Level.INFO), equalTo(LevelEx.TRACE));
    assertThat(LevelEx.toLevel(50001, Level.INFO), equalTo(LevelEx.BUG));
  }

  @Test
  public void toLevel_Integer_Default_NoMatch()
  {
    assertThat(LevelEx.toLevel(1964, Level.INFO), equalTo(Level.INFO));
  }
}
