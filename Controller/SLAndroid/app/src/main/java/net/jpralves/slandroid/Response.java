package net.jpralves.slandroid;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Response Object. * HTTP response. Return one of these from serve(). Class
 * made public from {@link NanoHTTPD} Class to create hooks
 *
 * @author Jarno Elonen
 * @version 1.0
 */
public class Response {
	/**
	 * Default constructor: response = HTTP_OK, data = mime = 'null'
	 */
	public Response() {
		this.status = HTTP_OK;
	}

	/**
	 * Basic constructor.
	 */
	public Response(String status, String mimeType, InputStream data) {
		this.status = status;
		this.mimeType = mimeType;
		this.data = data;
	}

	/**
	 * Convenience method that makes an InputStream out of given text.
	 */
	public Response(String status, String mimeType, String txt) {
		this.status = status;
		this.mimeType = mimeType;
        this.data = new ByteArrayInputStream(txt.getBytes());
    }

	/**
	 * Adds given line to the header.
	 */
	public void addHeader(String name, String value) {
		header.put(name, value);
	}

	/**
	 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
	 */
	public String status;

	/**
	 * MIME type of content, e.g. "text/html"
	 */
	public String mimeType;

	/**
	 * Data of the response, may be null.
	 */
	public InputStream data;

	/**
	 * Headers for the HTTP response. Use addHeader() to add lines.
	 */
	public Properties header = new Properties();

	/**
	 * Is streaming mode
	 */
	public boolean isStreaming = false;

	/**
	 * Some HTTP response status codes
	 */
	public static final String HTTP_OK = "200 OK", HTTP_PARTIALCONTENT = "206 Partial Content",
			HTTP_RANGE_NOT_SATISFIABLE = "416 Requested Range Not Satisfiable",
			HTTP_REDIRECT = "301 Moved Permanently", HTTP_NOTMODIFIED = "304 Not Modified",
			HTTP_FORBIDDEN = "403 Forbidden", HTTP_NOTFOUND = "404 Not Found",
			HTTP_BADREQUEST = "400 Bad Request", HTTP_INTERNALERROR = "500 Internal Server Error",
			HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String MIME_PLAINTEXT = "text/plain", MIME_HTML = "text/html",
			MIME_DEFAULT_BINARY = "application/octet-stream", MIME_XML = "text/xml",
			MIME_JSON = "application/json", MIME_JPEG = "image/jpeg",
			MIME_JAVASCRIPT = "application/javascript";
}
