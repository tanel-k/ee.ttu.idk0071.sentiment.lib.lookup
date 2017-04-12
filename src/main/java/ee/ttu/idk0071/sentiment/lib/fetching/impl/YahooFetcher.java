package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import ee.ttu.idk0071.sentiment.lib.fetching.api.SearchEngineFetcher;

public class YahooFetcher extends SearchEngineFetcher {
	private static final long MIN_THROTTLE_MILLIS = 1000L;
	private static final String RESULTS_ANCHOR_SELECTOR = "body#shp h3 a[href]";
	private static final long MAX_RESULTS_PER_PAGE = 15L;
	private static final String SEARCH_PAGE_URL_TEMPLATE = "https://search.yahoo.com/search?"
			+ "p=" + QUERY_PLACEHOLDER 
			+ "&n=" + RESULTS_PER_PAGE_PLACEHOLDER
			+ "&b=" + OFFSET_PLACEHOLDER;

	public YahooFetcher() {
		super(MIN_THROTTLE_MILLIS, MAX_RESULTS_PER_PAGE, SEARCH_PAGE_URL_TEMPLATE, RESULTS_ANCHOR_SELECTOR);
	}
}
