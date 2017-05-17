package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ee.ttu.idk0071.sentiment.lib.browsing.api.BrowserSimulator;
import ee.ttu.idk0071.sentiment.lib.browsing.impl.BrowserFactory;
import ee.ttu.idk0071.sentiment.lib.fetching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;
import ee.ttu.idk0071.sentiment.lib.utils.Poller;
import ee.ttu.idk0071.sentiment.lib.utils.Poller.PollFailedException;

public class TumblrFetcher implements Fetcher {
	private static final long SCROLL_THROTTLE_MILLIS = 2000L;

	private static final String TUMBLR_URL = "https://tumblr.com";

	private static final String POST_CONTENT_SELECTOR = ".posts div.post_body";
	private static final String TEXT_FILTER_BUTTON_SELECTOR = "i.icon_post_text_small";
	private static final String END_OF_RESULTS_SPAN_SELECTOR = "span.results_end_body";
	private static final String LOADER_DIV_SELECTOR = "div.search_flex_loader";
	private static final String END_OF_RESULTS_TEXT = "about it for";

	private static final long POLL_COOLDOWN_SECONDS = 2;
	private static final int POLL_RETRIES = 5;
	private static final int USELESS_SCROLL_LIMIT = 5;

	@SuppressWarnings("unused")
	@Override
	public List<String> fetch(Query query) throws FetchException {
		try {
			BrowserSimulator browser = BrowserFactory.getBrowser();
			String URL = getSearchPageURL(query);
			browser.open(URL);
			int uselessScrollsLeft = USELESS_SCROLL_LIMIT;
			Integer previousResultCount = null;
			Integer latestResultCount = null;
			
			try {
				// wait for filter button to appear (on page load)
				waitForElementToAppear(browser, TEXT_FILTER_BUTTON_SELECTOR);
			} catch (PollFailedException pex) {
				throw new FetchException("Failed to load page in time", pex);
			}
			
			browser.click(TEXT_FILTER_BUTTON_SELECTOR);
			
			SCROLL_LOOP: while (!checkNoMoreResults(browser) 
				&& (latestResultCount = browser.countElements(POST_CONTENT_SELECTOR)) < query.getMaxResults()) {
				browser.scrollToBottom();
				
				if (previousResultCount == null) {
					previousResultCount = latestResultCount;
				} else {
					if (previousResultCount == latestResultCount) {
						// result set size has not changed => useless scroll
						if (--uselessScrollsLeft <= 0) {
							break;
						}
					} else {
						// result set size has changed => reset
						previousResultCount = latestResultCount;
						uselessScrollsLeft = USELESS_SCROLL_LIMIT;
					}
				}
				
				try {
					// wait loading to begin
					Thread.sleep(SCROLL_THROTTLE_MILLIS);
				} catch (InterruptedException ex) {
					// not interested
				}
				
				try {
					waitForElementToDisappear(browser, LOADER_DIV_SELECTOR);
				} catch (PollFailedException pex) {
					// ignore for now
				}
			}
			
			List<String> results = browser.getTexts(POST_CONTENT_SELECTOR);
			browser.close();
			return results;
		} catch (Throwable t) {
			throw new FetchException(t);
		}
	}

	private static void waitForElementToAppear(BrowserSimulator browser, String querySelector) throws PollFailedException {
		waitForElement(browser, querySelector, true); 
	}

	private static void waitForElementToDisappear(BrowserSimulator browser, String querySelector) throws PollFailedException {
		waitForElement(browser, querySelector, false); 
	}

	private static void waitForElement(
		BrowserSimulator browser, String querySelector, boolean doWaitForAppear) throws PollFailedException {
		Poller loadPoller = new Poller();
		loadPoller
			.setCooldown(POLL_COOLDOWN_SECONDS, TimeUnit.SECONDS)
			.setIgnoreErrors(true)
			.setRetries(POLL_RETRIES)
			.setCondition(new Poller.Condition() {
				@Override
				public boolean isTrue() {
					if (doWaitForAppear) {
						return browser.doesElementExist(querySelector); 
					}
					
					return !browser.doesElementExist(querySelector);
				}
			})
			.poll();
	}

	private static boolean checkNoMoreResults(BrowserSimulator browser) {
		// FIXME: not working as expected (different UI for browser simulator?)
		if (browser.doesElementExist(END_OF_RESULTS_SPAN_SELECTOR)) {
			browser.getElementText(END_OF_RESULTS_SPAN_SELECTOR).contains(END_OF_RESULTS_TEXT);
		}
		
		return false;
	}

	private static String getSearchPageURL(Query query) throws UnsupportedEncodingException {
		return TUMBLR_URL + "/search/" + HTTPUtils.getURLEncodedValue(query.getKeyword());
	}
}
