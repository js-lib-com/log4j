package js.log4j;

import org.apache.log4j.Level;

/**
 * Custom log4j levels for TRACE and BUG.
 * 
 * @author Iulian Rotaru
 */
final class LevelEx extends Level {
	/** Java serialization version. */
	private static final long serialVersionUID = -7471659043722589379L;

	/** Numeric value for custom TRACE level. Numeric value is used by levels filtering. */
	public static final int TRACE_INT = Level.TRACE_INT + 1;
	/** Custom TRACE level. */
	public static final Level TRACE = new LevelEx(TRACE_INT, "TRACE", 10);
	/** Numeric value for custom BUG level. If want BUG to be visible when FATAL is active BUG numeric value should be greater. */
	public static final int BUG_INT = Level.FATAL_INT + 1;
	/** Custom BUG level. */
	public static final Level BUG = new LevelEx(BUG_INT, "BUG", 1);

	/**
	 * Delegates log4j custom level constructor.
	 * 
	 * @param levelValue level numeric value,
	 * @param levelName level string representation,
	 * @param syslogEquivalent numeric value used by syslog service.
	 */
	protected LevelEx(int levelValue, String levelName, int syslogEquivalent) {
		super(levelValue, levelName, syslogEquivalent);
	}

	/**
	 * Get level instance identified by its name.
	 * 
	 * @param levelName level name.
	 * @return named level.
	 */
	public static Level toLevel(String levelName) {
		if (levelName != null) {
			if (levelName.equals("TRACE")) {
				return TRACE;
			}
			if (levelName.equals("BUG")) {
				return BUG;
			}
		}
		return toLevel(levelName);
	}

	/**
	 * Get level instance identified by its level value.
	 * 
	 * @param levelValue level numeric value.
	 * @return level instance identified by its numeric value.
	 */
	public static Level toLevel(int levelValue) {
		if (levelValue == TRACE_INT) {
			return TRACE;
		}
		if (levelValue == BUG_INT) {
			return BUG;
		}
		return toLevel(levelValue, Level.DEBUG);
	}

	/**
	 * Get level instance identified by its name or given default level.
	 * 
	 * @param levelName level string representation,
	 * @param defaultLevel default level returned when <code>levelName</code> not found.
	 * @return name level or default level, if name not found.
	 */
	public static Level toLevel(String levelName, Level defaultLevel) {
		if (levelName != null) {
			if (levelName.equals("TRACE")) {
				return TRACE;
			}
			if (levelName.equals("BUG")) {
				return BUG;
			}
		}
		return Level.toLevel(levelName, defaultLevel);
	}

	/**
	 * Get level instance identified by its numeric value or given default level.
	 * 
	 * @param levelValue level numeric value,
	 * @param defaultLevel default level returned when <code>levelValue</code> not found.
	 * @return level instance identified by its numeric value or <code>defaultLevel</code>.
	 */
	public static Level toLevel(int levelValue, Level defaultLevel) {
		if (levelValue == TRACE_INT) {
			return TRACE;
		}
		if (levelValue == BUG_INT) {
			return BUG;
		}
		return Level.toLevel(levelValue, defaultLevel);
	}
}
