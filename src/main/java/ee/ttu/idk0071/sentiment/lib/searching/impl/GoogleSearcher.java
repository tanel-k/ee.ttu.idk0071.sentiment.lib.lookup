package ee.ttu.idk0071.sentiment.lib.searching.impl;

import java.io.UnsupportedEncodingException;
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

import ee.ttu.idk0071.sentiment.lib.searching.api.Searcher;
import ee.ttu.idk0071.sentiment.lib.searching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.utils.HtmlUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HtmlUtils.TextExtractionException;
import ee.ttu.idk0071.sentiment.lib.utils.HttpUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HttpUtils.HtmlRetrievalException;

public class GoogleSearcher implements Searcher {
	private static final String QUERY_PLACEHOLDER = "%QUERY%";
	private static final String RESULT_COUNT_PLACEHOLDER = "%COUNT%";
	private static final String GOOGLE_SEARCH_ENDPOINT = "https://www.google.com/search";
	private static final String GOOGLE_QUERY_STRING = "?q=" + QUERY_PLACEHOLDER + "&num=" + RESULT_COUNT_PLACEHOLDER;
	private static final Pattern URL_PATTERN = Pattern.compile("/url\\?q=(.*)&sa.*");
	
	public List<String> search(Query query) {
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
			return null;
		}
	}

	private String urlEncode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}

	private List<String> parseSearchResults(String response) {
		Document searchDoc = Jsoup.parse(response);
		Elements contentDiv = searchDoc.select("div#search");
		
		List<String> hits = new LinkedList<String>();
		Elements anchors = contentDiv.select("h3 a[href]");
		
		for (Element anchor : anchors) {
			String anchorHref = anchor.attr("href");
			Matcher urlMatcher = URL_PATTERN.matcher(anchorHref);
			
			if (urlMatcher.matches()) {
				String url = urlMatcher.group(1);
				
				try {
					String html = HttpUtils.getHtml(url);
					String text = HtmlUtils.getText(html);
					
					hits.add(text);
				} catch (TextExtractionException e) {
					// no recovery
					continue;
				} catch (HtmlRetrievalException e) {
					// no recovery
					continue;
				}
			}
		}
		
		return hits;
	}
}
