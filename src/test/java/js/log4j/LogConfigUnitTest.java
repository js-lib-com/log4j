package js.log4j;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Ignore;

import js.lang.ConfigBuilder;
import js.lang.ConfigException;
import js.log.Log;
import js.log.LogFactory;
import js.log.LogProvider;
import junit.framework.TestCase;

@Ignore
public class LogConfigUnitTest extends TestCase {
	public void testLogFactoryConfig() throws FileNotFoundException, ConfigException {
		System.setProperty("logs", "fixture");
		// LogFactory.config(new FileReader("fixture/log.xml"));
	}

	/**
	 * Log4j implementation requires ${logs} system property. If missing, log provider throws exception and log factory falls
	 * back to default logger.
	 */
	public void testMissingLogsSystemProperty() throws FileNotFoundException, ConfigException {
		System.clearProperty("logs");
		ConfigBuilder builder = new ConfigBuilder(new File("fixture/log.xml"));
		LogFactory.config(builder.build());
		Log log = LogFactory.getLog(LogConfigUnitTest.class);
		assertEquals("js.log.DefaultLog", log.getClass().getName());
	}

	public void testLogProviderConfig() throws FileNotFoundException, ConfigException {
		System.setProperty("logs", "fixture");
		LogProvider provider = new LogProviderImpl();
		assertTrue(provider instanceof LogProviderImpl);
		// provider.config(new FileReader("fixture/log.xml"));

		Log log = LogFactory.getLog("org.apache.mock.Class");
		log.trace("trace message");
		log.debug("debug message");
		log.info("info message");
		log.warn("warn message");
		log.error("error message");
		log.fatal("fatal message");
		log.bug("bug message");
	}
}
