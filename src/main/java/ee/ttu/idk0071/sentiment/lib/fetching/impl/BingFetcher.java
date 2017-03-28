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

public class BingFetcher extends SearchEngineFetcher {
	
	private static final Long COUNT_CONST = (long) 50;
	private static Long RESULT_COUNT = (long) 0;
	private static Long RESULT_PAGE_COUNT = (long) 0;
	private static final int const2 = 49; 
	
	
	private static final String BING_SEARCH_ENDPOINT = "http://www.bing.com/search";
	private static final Pattern URL_PATTERN = Pattern.compile("(.*)");
	
	private String urlEncode(String value) throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}
		
	@Override
	protected List<URL> scrapeURLs(Query query) throws ScrapeException {
		try
		{	
			List<URL> results = new LinkedList<URL>();
			
			RESULT_COUNT = query.getMaxResults();
			long mod = RESULT_COUNT % COUNT_CONST;

			if (mod == 0){
				RESULT_PAGE_COUNT = RESULT_COUNT / COUNT_CONST;
			}else{
				RESULT_PAGE_COUNT = (RESULT_COUNT / COUNT_CONST) + 1;
			}
			
			String lstPg = Long.toString(COUNT_CONST - ((COUNT_CONST*RESULT_PAGE_COUNT) - RESULT_COUNT));
						
			for(Long i = 1L; i <= RESULT_PAGE_COUNT ; i++){
				System.out.print(i+"/"+RESULT_PAGE_COUNT + " - ");
				Long fiVal = (COUNT_CONST * i) - const2;
				
				if (i == RESULT_PAGE_COUNT){
								
					String queryString = "?q=" + urlEncode(query.getKeyword()) +
										"&count=" + lstPg +
										"&first=" + String.valueOf(fiVal);
					
					String endPoint = BING_SEARCH_ENDPOINT + queryString;
					System.out.println(endPoint);
					HttpClient client = HttpClientBuilder.create().build();
					HttpGet get = new HttpGet(endPoint);
					HttpResponse response = client.execute(get);
					
					results.addAll(parseSearchResults(EntityUtils.toString(response.getEntity())));
				}else{	
					
					String queryString = "?q=" + urlEncode(query.getKeyword()) +
										"&count=" + Long.toString(COUNT_CONST) +
										"&first=" + String.valueOf(fiVal);
					
					String endPoint = BING_SEARCH_ENDPOINT + queryString;
					System.out.println(endPoint);
					HttpClient client = HttpClientBuilder.create().build();
					HttpGet get = new HttpGet(endPoint);
					HttpResponse response = client.execute(get);
					
					results.addAll(parseSearchResults(EntityUtils.toString(response.getEntity())));
				}
			}
			
			return results;
			
		} catch (Throwable t) {
			throw new ScrapeException(t);
		}
	}
	
	private List<URL> parseSearchResults(String response) {
		Document searchDoc = Jsoup.parse(response);
		
		Elements contentDiv = searchDoc.select("ol#b_results");
		
		List<URL> hits = new LinkedList<URL>();
		Elements anchors = contentDiv.select("h2 a[href]");
		
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
