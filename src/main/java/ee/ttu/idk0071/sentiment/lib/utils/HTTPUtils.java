package ee.ttu.idk0071.sentiment.lib.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

public class HTTPUtils {
	public static final int DEFAULT_TIMEOUT = 2000;

	public static class HtmlRetrievalException extends Exception {
		private static final long serialVersionUID = 1000451880777957727L;
	
		public HtmlRetrievalException(Throwable t) {
			super(t);
		}
	}

	public static class HTTPException extends Exception {
		private static final long serialVersionUID = 478702341159106923L;
		
		public HTTPException(Throwable t) {
			super(t);
		}
	}

	public static HttpResponse get(String URL, Header... headers) throws HTTPException {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(URL);
			
			for (Header header : headers) {
				get.addHeader(header);
			}
			
			return client.execute(get);
		} catch (Throwable t) {
			throw new HTTPException(t);
		}
	}

	public static HttpResponse get(URL fromURL, Header...headers) throws HTTPException {
		return get(fromURL.toString(), headers);
	}

	public static HttpResponse head(String URL) throws HTTPException {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpHead head = new HttpHead(URL);
			return client.execute(head);
		} catch (Throwable t) {
			throw new HTTPException(t);
		}
	}

	public static boolean checkHeadOK(String URL) {
		try {
			HttpResponse headResponse = HTTPUtils.head(URL);
			int statusCode = headResponse.getStatusLine().getStatusCode();
			return statusCode >= 200 && statusCode <= 299;
		} catch (Throwable t) {
			return false;
		}
	}

	public static String getStringWithTimeout(URL fromURL) throws HtmlRetrievalException {
		return getStringWithTimeout(fromURL, DEFAULT_TIMEOUT);
	}

	public static String getStringWithTimeout(URL fromURL, int timeout) throws HtmlRetrievalException {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeout)
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
			HttpGet get = new HttpGet(fromURL.toURI());
			get.setConfig(requestConfig);
			
			return EntityUtils.toString(client.execute(get).getEntity());
		} catch (Exception ex) {
			throw new HtmlRetrievalException(ex);
		}
	}

	public static Header extractCookieHeader(HttpResponse response) {
		StringBuilder cookieBuilder = new StringBuilder();
		
		for (Header setCookieHeader : response.getHeaders("Set-Cookie")) {
			cookieBuilder.append(setCookieHeader.getValue());
		}
		
		return new BasicHeader("Cookie", cookieBuilder.toString());
	}

	public static String urlEncode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}
}
