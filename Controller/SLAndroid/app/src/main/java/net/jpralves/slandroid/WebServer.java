package net.jpralves.slandroid;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

/**
 * WebServer Object.
 *
 * <P>
 * Extends the {@link NanoHTTPD} to support hooks.
 *
 * @author Joao Alves
 * @version 1.0
 */
public class WebServer extends NanoHTTPD {
	private static final String TAG = WebServer.class.getSimpleName();

	private ControllerApp app = null;

	private String htmlResponseInvalidRequest = "<html><body><h1>Invalid request</h1></body></html>\n";

	private Hashtable<String, HttpHook> hooks = new Hashtable<String, HttpHook>();

	public interface OnRequestListen {
		InputStream onRequest();

		void requestDone();
	}

	public WebServer(ControllerApp app, int port, String wwwroot) throws IOException {
		super(port, new File(wwwroot).getAbsoluteFile());
		this.app = app;
	}

	/**
	 * adds a hook for the specified uri
	 *
	 * @param uri
	 *            the uri of the hook
	 * @param httpHook
	 *            the hook object
	 */
	public void addHook(String uri, HttpHook httpHook) {
		hooks.put(uri.toLowerCase(Locale.US), httpHook);
	}

	/**
	 * based on the uri returns the associated hook
	 *
	 * @param uri
	 * @param method
	 * @param header
	 * @param parms
	 * @param files
	 * @return the httpHook associated with uri
	 */
	public HttpHook getHook(String uri, String method, Properties header, Properties parms,
			Properties files) {
		HttpHook r = hooks.get(uri.toLowerCase(Locale.US));
		return r;
	}

	/**
	 * clears/removes all hooks
	 */
	public void clearHooks() {
		hooks.clear();
	}

	/**
	 * serves request based on hooks
	 *
	 */
	public Response serve(String uri, String method, Properties header, Properties parms,
			Properties files) {

		Response resp;
		HttpHook hook = getHook(uri, method, header, parms, files);
		if (BuildConfig.DEBUG)
			Log.d(TAG, method + " '" + uri + "' (" + (hook != null ? "hookmode" : "normalfile")
					+ ")");
		if (hook != null) {
			resp = hook.execute(uri, method, header, parms, files);
			return resp;
		} else {
			resp = new Response(HTTP_NOTFOUND, MIME_HTML, htmlResponseInvalidRequest);
			return resp;
			// TODO: Resolver este problema.
			// return serve\(uri, header, homeDir, true);
		}
	}

	/**
	 * register inbytes and outbytes from request
	 */
	public void serveDone(Response r) {
		app.addTableInt("webserver.requests", 1);
		app.addTableLong("webserver.inbytes", getInBytes());
		app.addTableLong("webserver.outbytes", getOutBytes());
		if (BuildConfig.DEBUG)
			Log.d(TAG, "HTTP in: " + getInBytes() + ", out: " + getOutBytes());
	}
}
