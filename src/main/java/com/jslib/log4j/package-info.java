/**
 * Logging system. js-lib logging is a thin wrapper for log4j with next features:
 * <ul>
 * <li>log record with formatted message,
 * <li>BUG level, see {@link com.jslib.api.log.LogLevel#BUG},
 * <li>simplified configuration and seamless integration with Tomcat,
 * <li>remote console appender with Eclipse console, see {@link com.jslib.log4j.RemoteConsoleAppender}.
 * </ul>
 * <h3>Formatted Log</h3>
 * Is not uncommon to need adding variables to log record and often it is solved using string concatenation.
 * The problem with string concatenation is it is performed even if log level is disabled. Solution to this problem
 * is to wrap into log level enabled check resulting in more verbose code.
 * <pre>
 *	if(log.isEnabledFor(Level.DEBUG)) {
 *		log.debug("Class loaded " + class.getName());
 *	}
 * </pre>
 * instead of
 * <pre>
 *	log.debug("Class loaded {java_type}.", class.getName());
 * </pre>
 * The second solution executes string formatting only if log level is enabled but check for level is performed into logger writer.
 * <p>   
 * This package contains a service provider interface and a server side implementation based on log4j.
 * SPI is implemented also for Android, see j(s)-android project. 
 *
 * @author Iulian Rotaru
 */
package com.jslib.log4j;

