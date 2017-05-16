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

	private static final Long USUAL_PAGE_SIZE = 10L;
	private static final String TUMBLR_URL = "https://tumblr.com";
	private static final String RESULT_ANCHOR_SELECTOR = ".posts div.post_body";
	private static final String FILTER_ITEM_ANCHOR_SELECTOR = "i.icon_post_text_small";

	@Override
	public List<String> fetch(Query query) throws FetchException {
		try {
			int scrollCount = 0;
			int SCROLL_STOP_VALUE = (int) (query.getMaxResults() / USUAL_PAGE_SIZE);

			Browser browser = BrowserFactory.getBrowser();
			String URL = getSearchPageURL(query);
			browser.open(URL);

			browser.click(FILTER_ITEM_ANCHOR_SELECTOR);
			try {
				// wait for results to load after text filter
				Thread.sleep(800L);
			} catch (InterruptedException ex) {
				// not interested
			}

			while (scrollCount < SCROLL_STOP_VALUE
					&& browser.countElements(RESULT_ANCHOR_SELECTOR) < query.getMaxResults()) {
				browser.scrollToBottom();
				scrollCount++;
				try {
					// wait for results to load after scroll
					Thread.sleep(800L);
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

}
