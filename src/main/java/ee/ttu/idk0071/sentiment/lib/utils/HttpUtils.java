package ee.ttu.idk0071.sentiment.lib.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HttpUtils {
	public static String getHtml(String url, int timeout) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		RequestConfig requestConfig = RequestConfig.custom()
			.setConnectionRequestTimeout(timeout)
			.setConnectTimeout(timeout)
			.setSocketTimeout(timeout)
			.build();
		HttpGet get = new HttpGet(url);
		get.setConfig(requestConfig);
		return EntityUtils.toString(client.execute(get).getEntity());
	}
}
