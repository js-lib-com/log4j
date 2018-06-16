package js.log4j;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import js.log.Log;
import js.log.LogFactory;
import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

public class RemoteConsoleUnitTest extends TestCase {
	public void testRemoteConsoleAppender() throws IOException {
		String cfg = "" + //
				"log4j.appender.A=js.log.RemoteConsoleAppender\r\n" + //
				"log4j.appender.A.layout=org.apache.log4j.PatternLayout\r\n" + //
				"log4j.appender.A.layout.ConversionPattern=%-4r %-5p [%t] %c - %m%n\r\n" + //
				"log4j.appender.A.Port=8001\r\n" + //
				"log4j.rootLogger=ALL,A\r\n";

		Properties props = new Properties();
		props.load(new StringReader(cfg));
		PropertyConfigurator.configure(props);

		Log log = LogFactory.getLog("js,core.AppFactory");

		log.debug("message");
	}

	@SuppressWarnings("unused")
	public void _testRunServer() throws Exception {
		Class<?> remoteConsoleClass = Class.forName("gnotis.bb.test.RemoteConsoleServer");
		final Object server = remoteConsoleClass.newInstance();
		// server.postConstruct();

		new Thread() {
			@Override
			public void run() {
				for (int i = 0;; i++) {
					// server.print(i + ". message");
					try {
						Thread.sleep(10);
					} catch (InterruptedException unused) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}.start();

		// Thread thread = Classes.getFieldValue(server, "jsThread");
		// thread.join();
	}
}
