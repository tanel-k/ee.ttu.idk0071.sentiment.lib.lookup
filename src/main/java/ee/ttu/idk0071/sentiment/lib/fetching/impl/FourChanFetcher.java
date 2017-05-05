package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ee.ttu.idk0071.sentiment.lib.fetching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.utils.HTMLUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HTMLUtils.TextExtractionException;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils.HTMLRetrievalException;

public class FourChanFetcher implements Fetcher {
	public static final String CRED_KEY_GOOGLE_4CHAN_CUSTOM_SEARCH_KEY = "google-4chan-cse-key";
	public static final String CRED_KEY_GOOGLE_API_KEY = "google-api-key";

	private static final long THROTTLE_MILLIS = 500L;
	private static final String TEMPLATE_KEY_SEARCH_STRING = "{searchTerms}";
	private static final String TEMPLATE_KEY_COUNT = "{count}";
	private static final String TEMPLATE_KEY_START= "{startIndex}";
	private static final String TEMPLATE_KEY_LANGUAGE = "{language}";
	private static final String TEMPLATE_KEY_CUSTOM_SEARCH_KEY = "{cx}";
	private static final String TEMPLATE_KEY_API_KEY = "{apiKey}";
	private static final String GOOGLE_CUSTOM_SEARCH_URL_TEMPLATE = "https://www.googleapis.com/customsearch/v1"
			+ "?q=" + TEMPLATE_KEY_SEARCH_STRING
			+ "&num=" + TEMPLATE_KEY_COUNT
			+ "&start=" + TEMPLATE_KEY_START 
			+ "&lr=" + TEMPLATE_KEY_LANGUAGE
			+ "&cx=" + TEMPLATE_KEY_CUSTOM_SEARCH_KEY
			+ "&key=" + TEMPLATE_KEY_API_KEY;
	private static final String RESULT_SET_LANG_EN = "lang_en";
	private static final String PROP_ITEMS = "items";
	private static final String PROP_STARTINDEX = "startIndex";
	private static final String PROP_NEXTPAGE = "nextPage";
	private static final String PROP_QUERIES = "queries";
	private static final String PROP_LINK = "link";

	private static String get4chanCustomSearchURL(String searchString, FourChanCustomSearchCredentials credentials, int linksPerPage, long linkStartIndex) throws UnsupportedEncodingException {
		return GOOGLE_CUSTOM_SEARCH_URL_TEMPLATE
				.replace(TEMPLATE_KEY_SEARCH_STRING, HTTPUtils.urlEncode(searchString))
				.replace(TEMPLATE_KEY_COUNT, String.valueOf(linksPerPage))
				.replace(TEMPLATE_KEY_START, String.valueOf(linkStartIndex))
				.replace(TEMPLATE_KEY_LANGUAGE, RESULT_SET_LANG_EN)
				.replace(TEMPLATE_KEY_CUSTOM_SEARCH_KEY, credentials.googleCustomSearchKey)
				.replace(TEMPLATE_KEY_API_KEY, credentials.googleAPIKey);
	}

	private static class FourChanCustomSearchCredentials {
		String googleCustomSearchKey;
		String googleAPIKey;
	
		public boolean hasMissingElements() {
			return StringUtils.isEmpty(googleCustomSearchKey) || StringUtils.isEmpty(googleAPIKey);
		}
	}

	@Override
	public List<String> fetch(Query query) throws FetchException {
		try {
			FourChanCustomSearchCredentials credentials = new FourChanCustomSearchCredentials();
			credentials.googleAPIKey = query.getCredentials().get(CRED_KEY_GOOGLE_API_KEY);
			credentials.googleCustomSearchKey = query.getCredentials().get(CRED_KEY_GOOGLE_4CHAN_CUSTOM_SEARCH_KEY);
			if (credentials.hasMissingElements()) {
				throw new FetchException("Missing credentials");
			}
			
			String searchString = query.getKeyword();
			// max count = 10
			int linksPerPage = 10;
			long linkStartIndex = 1L;
			HttpClient client = HttpClientBuilder.create().build();
			Set<URL> activeThreadURLs = new HashSet<URL>();
			
			THREAD_FINDER_LOOP: do {
				String customSearchURL = get4chanCustomSearchURL(searchString, credentials, linksPerPage, linkStartIndex);
				HttpGet get = new HttpGet(customSearchURL);
				HttpResponse response = client.execute(get);
				
				if (!HTTPUtils.checkResponseOK(response)) {
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST
						&& linkStartIndex >= 100) {
						// CSE free tier API limited to 100 results
						break;
					}
					throw new FetchException("Google CSE responded with code " + response.getStatusLine().getStatusCode());
				}
				
				String responseString = EntityUtils.toString(response.getEntity());
				JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
				JsonElement itemsElement = responseJson.get(PROP_ITEMS);
				
				if (!itemsElement.isJsonNull()) {
					JsonArray items = itemsElement.getAsJsonArray();
					for (JsonElement itemElement : items) {
						JsonObject item = itemElement.getAsJsonObject();
						String link = item.get(PROP_LINK).getAsString();
						
						if (!HTTPUtils.checkHeadOK(link)) {
							continue;
						}
						
						try {
							activeThreadURLs.add(new URL(link));
						} catch (MalformedURLException e) {
							// should never happen
						}
						
						if (activeThreadURLs.size() >= query.getMaxResults()) {
							break THREAD_FINDER_LOOP;
						}
					}
				} else {
					break;
				}
				
				JsonObject queries = responseJson.get(PROP_QUERIES).getAsJsonObject();
				JsonElement nextPageArrayElement = queries.get(PROP_NEXTPAGE);
				if (nextPageArrayElement != null) {
					JsonArray nextPageArray = nextPageArrayElement.getAsJsonArray();
					if (nextPageArray.size() < 1) {
						break;
					}
					
					linkStartIndex = nextPageArray.get(0).getAsJsonObject().get(PROP_STARTINDEX).getAsLong();
				} else {
					break;
				}
				
				try {
					Thread.sleep(THROTTLE_MILLIS);
				} catch (InterruptedException ex) {
					// ignore
				}
				
			} while (activeThreadURLs.size() < query.getMaxResults());
			
			List<String> results = new LinkedList<String>();
			
			for (URL URL : activeThreadURLs) {
				try {
					String html = HTTPUtils.getStringWithTimeout(URL);
					String text = HTMLUtils.getText(html);
					results.add(text);
				} catch (HTMLRetrievalException e) {
					// no recovery
				} catch (TextExtractionException e) {
					// no recovery
				}
			}
			
			return results;
		} catch (FetchException ex) {
			throw ex;
		} catch (Throwable t) {
			throw new FetchException(t);
		}
	}

	public FourChanFetcher() { }
}
