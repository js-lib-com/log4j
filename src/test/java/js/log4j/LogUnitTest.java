package js.log4j;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import js.lang.ConfigBuilder;
import js.log.Log;
import js.log.LogFactory;
import junit.framework.TestCase;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class LogUnitTest extends TestCase {
	private static final String[] LEVELS = { "trace", "debug", "info", "warn", "error", "fatal", "bug" };

	private static final String[] LOG4J_LEVELS = { "trace", "debug", "info", "warn", "error", "fatal", "bug" };

	private MockAppender appender;
	private Log log;

	@Override
	protected void setUp() throws Exception {
		System.setProperty("logs", "fixture");
		ConfigBuilder builder = new ConfigBuilder(new File("fixture/log.xml"));
		LogFactory.config(builder.build());

		appender = new MockAppender();
		log = LogFactory.getLog(LogUnitTest.class);

		Logger logger = getField(log, "log4jLogger");
		logger.addAppender(appender);
		logger.setLevel(Level.ALL);
	}

	public void testLoggersInit() throws Throwable {
		@SuppressWarnings("unchecked")
		Enumeration<Category> loggers = LogManager.getCurrentLoggers();
		while (loggers.hasMoreElements()) {
			Logger logger = (Logger) loggers.nextElement();
			System.out.println(logger.getName() + ":" + logger.getParent().getName() + ":" + logger.getLevel());
		}

		log.error("test error");
	}

	public void testLoggersList() {
		Logger logger = Logger.getLogger("qwewrert");
		logger.setLevel(Level.OFF);

		@SuppressWarnings("unchecked")
		Enumeration<Category> loggers = LogManager.getCurrentLoggers();
		while (loggers.hasMoreElements()) {
			logger = (Logger) loggers.nextElement();
			System.out.println(logger.getName() + ":" + logger.getParent().getName() + ":" + logger.getLevel());
		}
	}

	public void testSimpleString() throws Exception {
		log.trace("%s");
		log.debug("%s");
		log.info("%s");
		log.warn("%s");
		log.error("%s");
		log.fatal("%s");

		for (int i = 0; i < appender.messages.size(); ++i) {
			TestCase.assertEquals(LOG4J_LEVELS[i] + " %s", appender.messages.get(i));
		}
	}

	public void testFormatedString() throws Exception {
		log.trace("%s", "message");
		log.debug("%s", "message");
		log.info("%s", "message");
		log.warn("%s", "message");
		log.error("%s", "message");
		log.fatal("%s", "message");

		for (int i = 0; i < appender.messages.size(); ++i) {
			TestCase.assertEquals(LOG4J_LEVELS[i] + " message", appender.messages.get(i));
		}
	}

	public void testSimpleObject() throws Exception {
		SimpleObject o = new SimpleObject();
		for (String level : LEVELS) {
			if (!level.equals("bug")) {
				Method m = getMethod(log, level, Object.class);
				m.invoke(log, o);
			}
		}
		for (int i = 0; i < appender.messages.size(); ++i) {
			TestCase.assertEquals(LOG4J_LEVELS[i] + " message", appender.messages.get(i));
		}
	}

	@SuppressWarnings("null")
	public void testThrowable() throws Exception {
		for (int i = 0; i < LEVELS.length; ++i) {
			try {
				appender.reset();
				Log l = null;
				l.debug("");
			} catch (Throwable t) {
				if (!LEVELS[i].equals("bug")) {
					Method m = getMethod(log, LEVELS[i], Object.class);
					m.invoke(log, t);
					TestCase.assertEquals(LOG4J_LEVELS[i] + " java.lang.NullPointerException", appender.messages.get(0));
				}
			}
		}
	}

	@SuppressWarnings("null")
	public void testStackTrace() throws Exception {
		try {
			appender.reset();
			Log log = null;
			log.debug("");
		} catch (Throwable t) {
			Method m = getMethod(log, "dump", Object.class, Throwable.class);
			m.invoke(log, "message", t);
			TestCase.assertTrue(appender.messages.size() > 2);
			TestCase.assertEquals("fatal" + " message", appender.messages.get(0));
			TestCase.assertEquals("java.lang.NullPointerException", appender.messages.get(1));
		}
	}

	public void testNullArgument() {
		Object o = null;

		log.trace("%s:%s", "message", o);
		log.debug("%s:%s", "message", o);
		log.info("%s:%s", "message", o);
		log.warn("%s:%s", "message", o);
		log.error("%s:%s", "message", o);
		log.fatal("%s:%s", "message", o);

		for (int i = 0; i < appender.messages.size(); ++i) {
			TestCase.assertEquals(LOG4J_LEVELS[i] + " message:null", appender.messages.get(i));
		}
	}

	public void testFormatedObjectsArray() throws Exception {
		log.debug("%s %s", "message", new Object[] { "string", true, 1.23 });
		log.debug("%s %s", "message", new Object[] { "string", true, 1.23, "ellipsis" });
		TestCase.assertEquals("debug message [string,true,1.23]", appender.messages.get(0));
		TestCase.assertEquals("debug message [string,true,1.23,...]", appender.messages.get(1));
	}

	public void _testAdaptiveTrace() throws ClassNotFoundException {
		try {
			getField(log, "");
		} catch (Throwable t) {
			StackTraceElement[] stack = t.getStackTrace();
			Set<String> classes = new HashSet<String>();
			for (StackTraceElement element : stack) {
				classes.add(element.getClassName());
			}
			for (String cls : classes) {
				LogFactory.getLog(Class.forName(cls));// .setLevel(Level.ALL);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T getField(Object object, String fieldName) throws Exception {
		Field f = object.getClass().getDeclaredField(fieldName);
		f.setAccessible(true);
		return (T) f.get(object);
	}

	private static Method getMethod(Object object, String methodName, Class<?>... parameterTypes) throws Exception {
		Method m = object.getClass().getMethod(methodName, parameterTypes);
		m.setAccessible(true);
		return m;
	}

    // --------------------------------------------------------------------------------------------
    // FIXTURE

	private static class SimpleObject {
		@Override
		public String toString() {
			return "message";
		}
	}
	
	private static class MockAppender implements Appender {
		List<Object> messages = new ArrayList<Object>();

		void reset() {
			messages.clear();
		}

		@Override
		public void addFilter(Filter filter) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clearFilters() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void doAppend(LoggingEvent event) {
			messages.add(event.getLevel().toString().toLowerCase() + " " + event.getMessage());
			if (event.getThrowableStrRep() != null) {
				for (String trace : event.getThrowableStrRep()) {
					messages.add(trace);
				}
			}
		}

		@Override
		public ErrorHandler getErrorHandler() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Filter getFilter() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Layout getLayout() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean requiresLayout() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setErrorHandler(ErrorHandler errorHandler) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setLayout(Layout layout) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setName(String name) {
			throw new UnsupportedOperationException();
		}
	}
}
