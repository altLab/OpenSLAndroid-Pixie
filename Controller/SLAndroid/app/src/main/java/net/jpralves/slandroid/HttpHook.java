package net.jpralves.slandroid;

import java.util.Properties;

/**
 * Simple abstract class to create Hooks in webserver
 * <p>
 * Requires implementation of execute method
 *
 * @author Joao Alves
 * @version 1.0
 */
public abstract class HttpHook {

	public HttpHook() {
	}

	public abstract Response execute(String uri, String method, Properties header,
			Properties parms, Properties files);
}
