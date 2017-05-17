package ee.ttu.idk0071.sentiment.lib.fetching.api;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.ScrapeException;
import ee.ttu.idk0071.sentiment.lib.utils.HTMLUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HTMLUtils.TextExtractionException;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils.HTMLRetrievalException;

/**
 * General scaffolding for a search engine scraper.
 */
public abstract class SearchEngineFetcher implements Fetcher {
	protected static final String QUERY_PLACEHOLDER = "%QUERY%";
	protected static final String RESULTS_PER_PAGE_PLACEHOLDER = "%RESULTS_PER_PAGE%";
	protected static final String OFFSET_PLACEHOLDER = "%OFFSET%";

	private static final int USELESS_QUERY_LIMIT = 3;

	private Long minThrottleMillis;
	private long maxResultsPerPage;
	private String searchPageURLTemplate;
	private String anchorSelector;

	/**
	 * Fetch texts found by the search engine for the specified query.
	 */
	public List<String> fetch(Query query) throws FetchException {
		List<URL> URLs;
		
		try {
			URLs = scrapeURLs(query);
		} catch (ScrapeException e) {
			throw new FetchException(e);
		}
		
		return getTextsForURLs(URLs);
	}

	/**
	 * Perform a search against the engine and retrieve (scrape) a list of URLs.<br/>
	 * This method also handles paging.
	 * 
	 * @throws ScrapeException when the search fails
	 */
	public List<URL> scrapeURLs(Query query) throws ScrapeException {
		try {
			Set<URL> results = new HashSet<URL>();
			String searchTerms = query.getKeyword();
			long maxResults = query.getMaxResults();
			long currentOffset = 1L;
			Header cookieHeader = null;
			
			long uselessQueriesLeft = USELESS_QUERY_LIMIT;
			Integer lastResultCount = null;
			
			do {
				String endPoint = buildSearchPageURL(searchTerms, currentOffset);
				
				HttpResponse response = cookieHeader != null 
					? HTTPUtils.get(endPoint)
					: HTTPUtils.get(endPoint, cookieHeader);
				
				if (cookieHeader == null) {
					cookieHeader = HTTPUtils.extractCookieHeader(response);
				}
				
				List<URL> pageResults = getURLsFromSearchPage(EntityUtils.toString(response.getEntity()));
				
				if (pageResults.size() == 0)
					break;
				
				Iterator<URL> pageResultIterator = pageResults.iterator();
				while (results.size() < maxResults 
					&& pageResultIterator.hasNext()) {
					results.add(pageResultIterator.next());
				}
				
				// no new pages added = useless query
				if (lastResultCount != null && lastResultCount == results.size()) {
					uselessQueriesLeft--;
				} else {
					// reset
					uselessQueriesLeft = USELESS_QUERY_LIMIT;
				}
				
				currentOffset += maxResultsPerPage;
				// optional throttle to avoid detection
				if (minThrottleMillis != null) {
					Thread.sleep(minThrottleMillis);
				}
			} while (results.size() < maxResults && uselessQueriesLeft > 0);
			
			return new LinkedList<URL>(results);
		} catch (Throwable t) {
			throw new ScrapeException(t);
		}
	}

	/**
	 * Given a search engine page, retrieves the URLs (search results) from said page.
	 */
	protected List<URL> getURLsFromSearchPage(String searchPageHTML) {
		Document searchDoc = Jsoup.parse(searchPageHTML);
		List<URL> pageURLs = new LinkedList<URL>();
		Elements anchors = searchDoc.select(anchorSelector);
		
		for (Element anchor : anchors) {
			String anchorHref = extractURLFromAnchor(anchor);
			
			try {
				pageURLs.add(new URL(anchorHref));
			} catch (MalformedURLException ex) {
				// no recovery
				continue;
			}
		}
		
		return pageURLs;
	}

	/**
	 * Builds a URL for the search engine's search page.
	 * 
	 * @param offset number used to control paging for various search engines
	 */
	protected String buildSearchPageURL(String searchTerm, long offset) 
			throws UnsupportedEncodingException {
		return searchPageURLTemplate
			.replace(QUERY_PLACEHOLDER, HTTPUtils.getURLEncodedValue(searchTerm))
			.replace(RESULTS_PER_PAGE_PLACEHOLDER, String.valueOf(maxResultsPerPage))
			.replace(OFFSET_PLACEHOLDER, String.valueOf(offset));
	}

	/**
	 * Given an anchor HTML element, extracts the href attribute and converts it into a URL String
	 */
	protected String extractURLFromAnchor(Element anchor) {
		return anchor.attr("href");
	}

	/**
	 * Retrieves text responses for the specified list of URLs
	 */
	protected List<String> getTextsForURLs(List<URL> URLs) {
		List<String> results = new LinkedList<String>();
		
		for (URL URL : URLs) {
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
	}

	/**
	 * Builds a throttled search engine scraper.
	 * 
	 * @param minThrottleMillis minimum amount of time to wait between opening a search page
	 * @param maxResultsPerPage maximum number of links allowable per search page
	 * @param searchPageURLTemplate template for building a search page URL
	 * @param searchPageResultAnchorSelector used to extract the relevant <a> elements from a result page
	 */
	public SearchEngineFetcher(
			long minThrottleMillis,
			long maxResultsPerPage,
			String searchPageURLTemplate,
			String searchPageResultAnchorSelector) {
		this.minThrottleMillis = minThrottleMillis;
		this.maxResultsPerPage = maxResultsPerPage;
		this.searchPageURLTemplate = searchPageURLTemplate;
		this.anchorSelector = searchPageResultAnchorSelector;
	}

	/**
	 * Builds an un-throttled search engine scraper.
	 * 
	 * @param maxResultsPerPage maximum number of links allowable per search page
	 * @param searchPageURLTemplate template for building a search page URL
	 * @param searchPageResultAnchorSelector used to extract the relevant <a> elements from a result page
	 */
	public SearchEngineFetcher(
			long maxResultsPerPage,
			String searchPageURLTemplate,
			String searchPageResultAnchorSelector) {
		this.maxResultsPerPage = maxResultsPerPage;
		this.searchPageURLTemplate = searchPageURLTemplate;
		this.anchorSelector = searchPageResultAnchorSelector;
	}
}
