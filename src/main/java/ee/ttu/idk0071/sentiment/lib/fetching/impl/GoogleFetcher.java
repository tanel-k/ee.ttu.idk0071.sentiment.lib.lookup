package ee.ttu.idk0071.sentiment.lib.fetching.impl;

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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ee.ttu.idk0071.sentiment.lib.fetching.api.SearchEngineFetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.ScrapeException;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;

public class GoogleFetcher extends SearchEngineFetcher {
	private static final long RESULTS_PER_PAGE = 50L;
	private static final int THROTTLE_MILLIS = 1000;

	private static final String QUERY_PLACEHOLDER = "%QUERY%";
	private static final String RESULTS_PER_PAGE_PLACEHOLDER = "%COUNT%";
	private static final String OFFSET_PLACEHOLDER = "%OFFSET%";

	private static final String GOOGLE_SEARCH_ENDPOINT = "https://www.google.com/search";
	private static final String GOOGLE_QUERY_STRING = "?"
			+ "q=" + QUERY_PLACEHOLDER 
			+ "&num=" + RESULTS_PER_PAGE_PLACEHOLDER
			+ "&start=" + OFFSET_PLACEHOLDER;

	@Override
	protected List<URL> scrapeURLs(Query query) throws ScrapeException {;
		try
		{
			Set<URL> results = new HashSet<URL>();
			long maxResults = query.getMaxResults();
			
			long offset = 1L;
			Header cookieHeader = null;
			do {
				String queryString = buildQueryString(
					query.getKeyword(), 
					RESULTS_PER_PAGE, 
					offset);
				
				String endPoint = buildEndpointURL(queryString);
				
				HttpClient client = HttpClientBuilder.create().build();
				HttpGet get = new HttpGet(endPoint);
				
				HttpResponse response = client.execute(get);
				
				if (cookieHeader == null) {
					cookieHeader = HTTPUtils.extractCookieHeader(response);
				} else {
					get.addHeader(cookieHeader);
				}
				
				List<URL> pageResults = parseSearchResults(
					EntityUtils.toString(response.getEntity()));
				
				if (pageResults.size() == 0)
					break;
				
				Iterator<URL> pageResultIterator = pageResults.iterator();
				while (results.size() < maxResults 
					&& pageResultIterator.hasNext()) {
					results.add(pageResultIterator.next());
				}
				
				offset += RESULTS_PER_PAGE;
				
				// throttle search frequency to avoid scrape detection
				Thread.sleep(THROTTLE_MILLIS);
			} while (results.size() < maxResults);
			
			return new LinkedList<URL>(results);
		} catch (Throwable t) {
			throw new ScrapeException(t);
		}
	}

	private String buildQueryString(String keyword, long resultsPerPage, long offset) 
			throws UnsupportedEncodingException {
		return GOOGLE_QUERY_STRING
		.replace(QUERY_PLACEHOLDER, HTTPUtils.urlEncode(keyword))
		.replace(RESULTS_PER_PAGE_PLACEHOLDER, String.valueOf(resultsPerPage))
		.replace(OFFSET_PLACEHOLDER, String.valueOf(offset));
	}

	private String buildEndpointURL(String queryString) {
		return GOOGLE_SEARCH_ENDPOINT + queryString;
	}

	private List<URL> parseSearchResults(String response) {
		Document searchDoc = Jsoup.parse(response);
		Elements contentDiv = searchDoc.select("div#search");
		
		List<URL> hits = new LinkedList<URL>();
		Elements anchors = contentDiv.select("h3 a[href]");
		
		for (Element anchor : anchors) {
			String anchorHref = anchor.attr("href");
			
			try {
				hits.add(new URL(anchorHref));
			} catch (MalformedURLException ex) {
				// no recovery
				continue;
			}
		}
		
		return hits;
	}
}
