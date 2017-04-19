package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import ee.ttu.idk0071.sentiment.lib.fetching.api.SearchEngineFetcher;

public class GoogleFetcher extends SearchEngineFetcher {
	private static final Pattern URL_EXTRACTION_PATTERN = Pattern.compile("/url\\?q=(.*)&sa.*");

	private static final long MIN_THROTTLE_MILLIS = 1000L;
	private static final String RESULTS_ANCHOR_SELECTOR = "div#search h3 a[href]";
	private static final long MAX_RESULTS_PER_PAGE = 50L;
	private static final String SEARCH_PAGE_URL_TEMPLATE = "https://www.google.com/search?"
			+ "q=" + QUERY_PLACEHOLDER 
			+ "&num=" + RESULTS_PER_PAGE_PLACEHOLDER
			+ "&start=" + OFFSET_PLACEHOLDER;

	@Override
	protected String extractURLFromAnchor(Element anchor) {
		String anchorHref = anchor.attr("href");
		
		Matcher URLMatcher = URL_EXTRACTION_PATTERN.matcher(anchorHref);
		if (URLMatcher.matches()) {
			anchorHref = URLMatcher.group(1);
		}
		
		return anchorHref;
	}

	public GoogleFetcher() {
		super(MIN_THROTTLE_MILLIS, MAX_RESULTS_PER_PAGE, SEARCH_PAGE_URL_TEMPLATE, RESULTS_ANCHOR_SELECTOR);
	}
}
