package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.io.UnsupportedEncodingException;
import java.util.List;

import ee.ttu.idk0071.sentiment.lib.browsing.api.Browser;
import ee.ttu.idk0071.sentiment.lib.browsing.impl.BrowserFactory;
import ee.ttu.idk0071.sentiment.lib.fetching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;

public class TumblrFetcher implements Fetcher {

	private static final String SCROLL_STOP_VALUE = "Try another search?";
	private static final String TUMBLR_URL = "https://tumblr.com";
	private static final String RESULT_ANCHOR_SELECTOR = ".posts div.post_body";
	private static final String FILTER_ANCHOR_SELECTOR = "span.control_text";
	private static final String FILTER_ITEM_ANCHOR_SELECTOR = "span.menu_item_text";

	@Override
	public List<String> fetch(Query query) throws FetchException {
		try {
			Browser browser = BrowserFactory.getBrowser();
			String URL = getSearchPageURL(query);
			browser.open(URL);
			
			//does not work so far
			browser.click(FILTER_ANCHOR_SELECTOR);
			browser.click(FILTER_ITEM_ANCHOR_SELECTOR);

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
			List<String> results = browser.getTexts(RESULT_ANCHOR_SELECTOR);
			
			browser.close();

			return results;
		} catch (Throwable t) {
			throw new FetchException(t);
		}
	}

	private static String getSearchPageURL(Query query) throws UnsupportedEncodingException {
		return TUMBLR_URL + "/search/" + HTTPUtils.urlEncode(query.getKeyword());
	}

	public static void main(String[] args) throws FetchException {
		Query q = new Query();
		TumblrFetcher tf = new TumblrFetcher();
		
		q.setKeyword("text");
		q.setMaxResults(100L);
		
		List<String> results;
		
		try {
			 results = tf.fetch(q);
		} catch (Throwable t) {
			throw new FetchException(t);
		}
		
		for (String result : results)
		{
		    System.out.println("------" + result);
		}
		
		
	}
}
