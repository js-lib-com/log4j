package js.log4j;

import js.log.LogContext;

import org.apache.log4j.NDC;

public class LogContextImpl implements LogContext {
	@Override
	public void push(String diagnosticContext) {
		NDC.push(diagnosticContext);
	}

	@Override
	public void pop() {
		NDC.pop();
		NDC.clear();
	}
}
