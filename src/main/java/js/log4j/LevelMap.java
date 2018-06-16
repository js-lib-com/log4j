package js.log4j;

import js.log.LogLevel;

import org.apache.log4j.Level;

/**
 * Maps <code>j(s)-lib</code> logger levels to log4j {@link Level}. Current implementation is based on <code>log4j</code>. In
 * order to match <code>j(s)-lib</code> levels there are two <code>log4j</code> custom levels: {@link LevelEx#TRACE} and
 * {@link LevelEx#BUG}.
 * 
 * @author Iulian Rotaru
 */
final class LevelMap {
	/** Levels map is actually a <code>log4j</code> levels list organized on <code>j(s)-lib</code> levels order. */
	private static final Level[] LEVEL_MAP = new Level[] {
			//
			LevelEx.TRACE, // LogLevel.TRACE
			Level.DEBUG, // LogLevel.DEBUG
			Level.INFO, // LogLevel.INFO
			Level.WARN, // LogLevel.WARN
			Level.ERROR, // LogLevel.ERROR
			Level.FATAL, // LogLevel.FATAL
			LevelEx.BUG, // LogLevel.BUG
			Level.OFF // LogLevel.OFF
	};

	/**
	 * Get <code>log4j</code> level assigned to <code>j(s)-lib</code> level.
	 * 
	 * @param logLevel <code>j(s)-lib</code> log level.
	 * @return <code>log4j</code> level.
	 */
	static Level log4jLevel(LogLevel logLevel) {
		return LEVEL_MAP[logLevel.ordinal()];
	}

	/** Forbid default constructor synthesis. */
	private LevelMap() {
	}
}
