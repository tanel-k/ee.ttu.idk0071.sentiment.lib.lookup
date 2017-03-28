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

public class YahooFetcher extends SearchEngineFetcher {
	
	private static final Long COUNT_CONST = (long) 40;
	private static Long RESULT_COUNT = (long) 0;
	private static Long RESULT_PAGE_COUNT = (long) 0;
	private static final int const2 = 9; 
	
	
	private static final String BING_SEARCH_ENDPOINT = "http://search.yahoo.com/search";
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
								
					String queryString = "?p=" + urlEncode(query.getKeyword()) +
										"&b=" + String.valueOf(fiVal) +
										"&pz=" + lstPg;
					
					String endPoint = BING_SEARCH_ENDPOINT + queryString;
					System.out.println(endPoint);
					HttpClient client = HttpClientBuilder.create().build();
					HttpGet get = new HttpGet(endPoint);
					HttpResponse response = client.execute(get);
					
					results.addAll(parseSearchResults(EntityUtils.toString(response.getEntity())));
				}else{	
					
					String queryString = "?q=" + urlEncode(query.getKeyword()) +
										"&b=" + String.valueOf(fiVal) +
										"&pz=" + Long.toString(COUNT_CONST) ;
					
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
		
		List<URL> hits = new LinkedList<URL>();
		
		Document searchDoc = Jsoup.parse(response);
		Elements contentDiv = searchDoc.select("body#shp");
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
	
	
	public static void main(String [ ] args) throws ScrapeException
	{
		Query q = new Query();
		q.setKeyword("test");
	    q.setMaxResults((long)44);
		
		YahooFetcher bf = new YahooFetcher();
		List<URL> li = bf.scrapeURLs(q);
		int len = li.size();
		 
		/*
		for(int i = 0; i<len; i++){
			System.out.println(li.get(i));
		}
		*/
		
		System.out.println(len);
	}
	
}