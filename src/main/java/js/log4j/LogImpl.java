package js.log4j;

import js.log.AbstractLog;
import js.log.Log;
import js.log.LogLevel;

import org.apache.log4j.Logger;

/**
 * Thin wrapper on Apache log4j {@link Logger}, implementing j(s)-lib {@link Log} interface. Apache log4j engine should be
 * properly initialized for this class to actually record any logging message.
 * <p>
 * There is a limit on logging record length, see {@link #MAX_MESSAGE_LENGTH}. If limit is reached logging message is truncated
 * with ellipsis.
 * 
 * @author Iulian Rotaru
 * @version draft
 */
final class LogImpl extends AbstractLog {
	/** Maximum logging record length. Messages is truncated with ellipsis if exceed this limit. */
	private static final int MAX_MESSAGE_LENGTH = 2048;

	/** Underlying Apache log4j logger delegated for actual logging record writing. */
	private Logger log4jLogger;

	/**
	 * Construct logger instance for specified target class.
	 * 
	 * @param loggerName name to identify logger instance.
	 */
	public LogImpl(String loggerName) {
		this.log4jLogger = Logger.getLogger(loggerName);
	}

	@Override
	protected boolean isLoggable(LogLevel level) {
		return log4jLogger.isEnabledFor(LevelMap.log4jLevel(level));
	}

	@Override
	protected void log(LogLevel level, String message) {
		if (message != null) {
			log4jLogger.log(LevelMap.log4jLevel(level), ellipsis(message, MAX_MESSAGE_LENGTH));
		}
	}

	@Override
	public void dump(Object message, Throwable throwable) {
		if (isLoggable(LogLevel.FATAL)) {
			log4jLogger.log(LevelMap.log4jLevel(LogLevel.FATAL), ellipsis(message(message), MAX_MESSAGE_LENGTH), throwable);
		}
	}

	@Override
	public void print(LogLevel level, String message) {
		if (isLoggable(level)) {
			log4jLogger.log(LevelMap.log4jLevel(level), ellipsis(message(message), MAX_MESSAGE_LENGTH));
		}
	}
}
