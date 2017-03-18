package ee.ttu.idk0071.sentiment.lib.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HttpUtils {
	public static class HtmlRetrievalException extends Exception {
		private static final long serialVersionUID = 1000451880777957727L;
	
		public HtmlRetrievalException(Throwable t) {
			super(t);
		}
	}
	
	public static String getHtml(String url, int timeout) throws HtmlRetrievalException {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeout)
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
			HttpGet get = new HttpGet(url);
			get.setConfig(requestConfig);
			
			return EntityUtils.toString(client.execute(get).getEntity());
		} catch (Exception ex) {
			throw new HtmlRetrievalException(ex);
		}
	}
}
