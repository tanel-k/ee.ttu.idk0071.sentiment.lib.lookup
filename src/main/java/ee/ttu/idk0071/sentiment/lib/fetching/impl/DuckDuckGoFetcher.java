package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import ee.ttu.idk0071.sentiment.lib.browsing.api.Browser;
import ee.ttu.idk0071.sentiment.lib.browsing.impl.BrowserFactory;
import ee.ttu.idk0071.sentiment.lib.fetching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.utils.HTMLUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;

public class DuckDuckGoFetcher implements Fetcher {
	private static final String SCROLL_STOP_VALUE = "No more results";
	private static final String DUCK_DUCK_GO_URL = "https://duckduckgo.com";
	private static final String RESULT_ANCHOR_SELECTOR = ".result a.result__a";

	@Override
	public List<String> fetch(Query query) throws FetchException {
		try {
			Browser browser = BrowserFactory.getBrowser();
			String URL = getSearchPageURL(query);
			browser.open(URL);

			while (!browser.getHTML().contains(SCROLL_STOP_VALUE)
					&& browser.countElements(RESULT_ANCHOR_SELECTOR) < query.getMaxResults()) {
				browser.scrollToBottom();
				try {
					// wait for results to load after scroll
					Thread.sleep(500L);
				} catch (InterruptedException ex) {
					// not interested
				}
			}

			List<String> URLs = browser.getAttributeValues(RESULT_ANCHOR_SELECTOR, "href");
			// close browser
			browser.close();

			List<String> results = new LinkedList<String>();
			// consider using HTTPUtils.checkHeadOK for each URL to see if a GET
			// would work
			results = getTextsForURLs(URLs);

			return results;
		} catch (Throwable t) {
			throw new FetchException(t);
		}
	}

	private static String getSearchPageURL(Query query) throws UnsupportedEncodingException {
		return DUCK_DUCK_GO_URL + "/?q=" + HTTPUtils.urlEncode(query.getKeyword());
	}

	protected List<String> getTextsForURLs(List<String> URLs) {
		List<String> results = new LinkedList<String>();

		for (String URL : URLs) {
			try {
				HttpClient client = HttpClientBuilder.create().build();
				RequestConfig requestConfig = RequestConfig.custom().build();
				HttpGet get = new HttpGet(URL);
				get.setConfig(requestConfig);

				String html = EntityUtils.toString(client.execute(get).getEntity());
				String text = HTMLUtils.getText(html);
				results.add(text);

			} catch (Throwable e) {
				// no recovery
			}
		}

		return results;
	}
}
