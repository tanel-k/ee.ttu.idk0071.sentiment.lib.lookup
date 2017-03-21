package ee.ttu.idk0071.sentiment.lib.utils;

import java.net.URL;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HTTPUtils {
	public static final int DEFAULT_TIMEOUT = 2000;

	public static class HtmlRetrievalException extends Exception {
		private static final long serialVersionUID = 1000451880777957727L;
		
	
		public HtmlRetrievalException(Throwable t) {
			super(t);
		}
	}

	public static String getHtml(URL fromURL) throws HtmlRetrievalException {
		return getHtml(fromURL, DEFAULT_TIMEOUT);
	}

	public static String getHtml(URL fromURL, int timeout) throws HtmlRetrievalException {
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
}
