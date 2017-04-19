package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import ee.ttu.idk0071.sentiment.lib.fetching.api.SearchEngineFetcher;

public class BingFetcher extends SearchEngineFetcher {
	private static final long MAX_RESULTS_PER_PAGE = 100L;
	private static final String RESULTS_ANCHOR_SELECTOR = "ol#b_results h2 a[href]";
	private static final String SEARCH_PAGE_URL_TEMPLATE = "http://www.bing.com/search?"
			+ "q=" + QUERY_PLACEHOLDER
			+ "&count=" + RESULTS_PER_PAGE_PLACEHOLDER
			+ "&first=" + OFFSET_PLACEHOLDER;

	public BingFetcher() {
		super(MAX_RESULTS_PER_PAGE, SEARCH_PAGE_URL_TEMPLATE, RESULTS_ANCHOR_SELECTOR);
	}
}
