package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class GoogleFetcher extends SearchEngineFetcher {
	private static final String QUERY_PLACEHOLDER = "%QUERY%";
	private static final String RESULT_COUNT_PLACEHOLDER = "%COUNT%";
	private static final String GOOGLE_SEARCH_ENDPOINT = "https://www.google.com/search";
	private static final String GOOGLE_QUERY_STRING = "?q=" + QUERY_PLACEHOLDER + "&num=" + RESULT_COUNT_PLACEHOLDER;
	private static final Pattern URL_PATTERN = Pattern.compile("/url\\?q=(.*)&sa.*");

	private String urlEncode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}

	@Override
	protected List<URL> scrapeURLs(Query query) throws ScrapeException {
		try
		{
			String queryString = GOOGLE_QUERY_STRING.replace(QUERY_PLACEHOLDER, urlEncode(query.getKeyword()))
					.replace(RESULT_COUNT_PLACEHOLDER, String.valueOf(query.getMaxResults()));
			String endPoint = GOOGLE_SEARCH_ENDPOINT + queryString;
			
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(endPoint);
			HttpResponse response = client.execute(get);
			
			return parseSearchResults(EntityUtils.toString(response.getEntity()));
		} catch (Throwable t) {
			throw new ScrapeException(t);
		}
	}

	private List<URL> parseSearchResults(String response) {
		Document searchDoc = Jsoup.parse(response);
		Elements contentDiv = searchDoc.select("div#search");
		
		List<URL> hits = new LinkedList<URL>();
		Elements anchors = contentDiv.select("h3 a[href]");
		
		for (Element anchor : anchors) {
			String anchorHref = anchor.attr("href");
			Matcher urlMatcher = URL_PATTERN.matcher(anchorHref);
			
			if (urlMatcher.matches()) {
				try {
					String URLString = urlMatcher.group(1);
					hits.add(new URL(URLString));
				} catch (MalformedURLException ex) {
					// no recovery
					continue;
				}
			}
		}
		
		return hits;
	}
}
