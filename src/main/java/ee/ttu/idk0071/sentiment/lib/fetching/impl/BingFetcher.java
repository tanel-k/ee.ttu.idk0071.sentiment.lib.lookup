package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

public class BingFetcher extends SearchEngineFetcher {
	private static final long RESULTS_PER_PAGE = 100L;
	private static final int USELESS_QUERY_LIMIT = 3;

	private static final String QUERY_PLACEHOLDER = "%QUERY%";
	private static final String RESULTS_PER_PAGE_PLACEHOLDER = "%COUNT%";
	private static final String OFFSET_PLACEHOLDER = "%OFFSET%";

	private static final String BING_SEARCH_ENDPOINT = "http://www.bing.com/search";
	private static final String BING_SEARCH_QUERY_STRING = "?"
			+ "q=" + QUERY_PLACEHOLDER
			+ "&count=" + RESULTS_PER_PAGE_PLACEHOLDER
			+ "&first=" + OFFSET_PLACEHOLDER;

	@Override
	protected List<URL> scrapeURLs(Query query) throws ScrapeException {
		try {
			Set<URL> results = new HashSet<URL>();
			
			long maxResults = query.getMaxResults();
			long offset = 1L;
			
			// Bing may end up repeating the last page
			long allowedUselessQueriesCnt = USELESS_QUERY_LIMIT;
			int lastResultCount = results.size();
			
			do {
				String queryString = buildQueryString(query.getKeyword(), RESULTS_PER_PAGE, offset);
				
				String endPoint = buildEndpointURL(queryString);
				
				HttpClient client = HttpClientBuilder.create().build();
				HttpGet get = new HttpGet(endPoint);
				HttpResponse response = client.execute(get);
				
				List<URL> pageResults = parseSearchResults(
					EntityUtils.toString(response.getEntity()));
				
				if (pageResults.size() < 1)
					break;
				
				Iterator<URL> pageResultIterator = pageResults.iterator();
				while (results.size() < maxResults 
					&& pageResultIterator.hasNext()) {
					results.add(pageResultIterator.next());
				}
				
				// no new pages added = useless query
				if (lastResultCount == results.size()) {
					allowedUselessQueriesCnt--;
				} else {
					allowedUselessQueriesCnt = 0;
				}
				
				lastResultCount = results.size();
				offset += RESULTS_PER_PAGE;
			} while (results.size() < maxResults 
				&& allowedUselessQueriesCnt > 0);
			
			return new LinkedList<URL>(results);
		} catch (Throwable t) {
			throw new ScrapeException(t);
		}
	}

	private String buildQueryString(String keyword, Long resultsPerPage, Long offset) 
		throws UnsupportedEncodingException {
		return BING_SEARCH_QUERY_STRING
			.replace(QUERY_PLACEHOLDER, HTTPUtils.urlEncode(keyword))
			.replace(RESULTS_PER_PAGE_PLACEHOLDER, resultsPerPage.toString())
			.replace(OFFSET_PLACEHOLDER, offset.toString());
	}

	private String buildEndpointURL(String queryString) {
		return BING_SEARCH_ENDPOINT + queryString;
	}

	private List<URL> parseSearchResults(String response) {
		Document searchDoc = Jsoup.parse(response);
		
		Elements contentDiv = searchDoc.select("ol#b_results");
		
		List<URL> pageURLs = new LinkedList<URL>();
		Elements anchors = contentDiv.select("h2 a[href]");
		
		for (Element anchor : anchors) {
			String anchorHref = anchor.attr("href");
			
			try {
				pageURLs.add(new URL(anchorHref));
			} catch (MalformedURLException ex) {
				// no recovery
				continue;
			}
		}
		
		return pageURLs;
	}
}
