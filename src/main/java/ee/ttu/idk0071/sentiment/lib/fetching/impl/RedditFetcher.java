package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ee.ttu.idk0071.sentiment.lib.fetching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;
import ee.ttu.idk0071.sentiment.lib.utils.StringUtils;

public class RedditFetcher implements Fetcher {
	private static final long THROTTLE_MILLIS = 500;
	private static final String PROP_TITLE = "title";
	private static final String PROP_SELF_TEXT = "selftext";
	private static final String PROP_CHILDREN = "children";
	private static final String PROP_DATA = "data";
	private static final String PROP_AFTER = "after";
	private static final String SEARCH_ENDPOINT_URL = "https://www.reddit.com/search.json";
	private static final String PARAM_QUERY = "q";
	private static final String PARAM_AFTER = "after";

	@Override
	public List<String> fetch(Query query) throws FetchException {
		try {
			final String queryURL = SEARCH_ENDPOINT_URL + "?" + PARAM_QUERY + "=" + query.getKeyword();
			String pagedURL = queryURL;
			HttpClient client = HttpClientBuilder.create().build();
			
			String afterKey = null;
			List<String> results = new LinkedList<String>();
			PRIMARY: do {
				HttpGet get = new HttpGet(pagedURL);
				HTTPUtils.setUserAgentHeader(get);
				
				HttpResponse response = client.execute(get);
				
				if (!HTTPUtils.checkResponseOK(response)) {
					throw new FetchException("Endpoint responded with code " + response.getStatusLine().getStatusCode());
				}
				
				String responseString = EntityUtils.toString(response.getEntity());
				JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
				JsonObject responseData = responseJson.get(PROP_DATA).getAsJsonObject();
				
				if (responseData != null) {
					JsonElement afterKeyElement = responseData.get(PROP_AFTER);
					afterKey = !afterKeyElement.isJsonNull() ? afterKeyElement.getAsString() : null;
					
					for (JsonElement childElement : responseData.get(PROP_CHILDREN).getAsJsonArray()) {
						JsonObject child = childElement.getAsJsonObject();
						JsonObject childData = child.get(PROP_DATA).getAsJsonObject();
						String text = childData.get(PROP_SELF_TEXT).getAsString();
						if (StringUtils.isEmpty(text)) {
							text = childData.get(PROP_TITLE).getAsString();
						}
						
						if (!StringUtils.isEmpty(text)) {
							results.add(text);
						}
						
						if (results.size() >= query.getMaxResults()) {
							break PRIMARY;
						}
					}
				}
				
				try {
					Thread.sleep(THROTTLE_MILLIS);
				} catch (InterruptedException ex) {
					// don't care
				}
				pagedURL = queryURL + "&" + PARAM_AFTER + "=" + afterKey;
			} while (!StringUtils.isEmpty(afterKey) && results.size() < query.getMaxResults());
			
			return results;
		} catch (FetchException ex) {
			throw ex;
		} catch (Throwable t) {
			throw new FetchException(t);
		}
	}
}
