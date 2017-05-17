package ee.ttu.idk0071.sentiment.lib.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Utility class for scraping Google.<br/>
 * NOT detection-proof! Try to avoid spamming queries with search modifiers - they view those with suspicion.
 */
public class GoogleUtils {
	public static class GoogleException extends Exception {
		private static final long serialVersionUID = 242485987315147534L;
	
		public GoogleException(Throwable t) {
			super(t);
		}
	
		public GoogleException(String msg, Throwable t) {
			super(msg, t);
		}
	
		public GoogleException(String msg) {
			super(msg);
		}
	}

	public static class GoogleScrapeDetectedException extends GoogleException {
		private static final long serialVersionUID = 6771652306110778680L;
	
		public GoogleScrapeDetectedException() {
			super("Google has detected scraping");
		}
	}

	public static class GoogleScraper {
		public static final String SEARCH_MODIFIER_SITE = "Site";
	
		private static final long MAX_RESULTS_PER_PAGE = 50;
		private static final Pattern URL_EXTRACTION_PATTERN = Pattern.compile("/url\\?q=(.*)&sa.*");
		private static final String FAKE_CHROME_UA_HEADER = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";
		private static final String QUERY_PLACEHOLDER = "%QUERY%";
		private static final String RESULTS_PER_PAGE_PLACEHOLDER = "%RESULTS_PER_PAGE%";
		private static final String RESULTS_ANCHOR_SELECTOR = "div#search h3 a[href]";
		private static final String OFFSET_PLACEHOLDER = "%OFFSET%";
		private static final String END_OF_RESULTS_PHRASE = "did not match any documents";
		private static final long MIN_THROTTLE_MILLIS = 1000L;
		private static final String SEARCH_PAGE_URL_TEMPLATE = "http://www.google.com/search?"
				+ "q=" + QUERY_PLACEHOLDER 
				+ "&num=" + RESULTS_PER_PAGE_PLACEHOLDER
				+ "&start=" + OFFSET_PLACEHOLDER;
	
		private final Header userAgentHeader = new BasicHeader(HttpHeaders.USER_AGENT, FAKE_CHROME_UA_HEADER);
		private final Map<String, String> searchModifiers = new HashMap<String, String>();
	
		private long resultsPerPage = MAX_RESULTS_PER_PAGE;
		private long throttleMillis = MIN_THROTTLE_MILLIS;
		private long offset = 1L;
		private boolean isOnLastPage = false;
		private String query;
		private String pageBuffer = null;
		private Header cookieHeader = null;
	
		private static boolean isLastPage(String pageContent) {
			return StringUtils.isEmpty(pageContent) || pageContent.contains(END_OF_RESULTS_PHRASE);
		}
	
		protected String getQueryURL() throws GoogleException {
			try {
				StringBuilder queryStringBuf = new StringBuilder();
				queryStringBuf.append(this.query);
				
				for (Entry<String, String> modifierDefinition : this.searchModifiers.entrySet()) {
					queryStringBuf.append(" " + modifierDefinition.getKey() + ":" + modifierDefinition.getValue());
				}
				
				return SEARCH_PAGE_URL_TEMPLATE
					.replace(QUERY_PLACEHOLDER, HTTPUtils.getURLEncodedValue(queryStringBuf.toString()))
					.replace(RESULTS_PER_PAGE_PLACEHOLDER, String.valueOf(this.resultsPerPage))
					.replace(OFFSET_PLACEHOLDER, String.valueOf(this.offset));
			} catch (UnsupportedEncodingException e) {
				throw new GoogleException(e);
			}
		}
	
		public GoogleScraper setQuery(String query) {
			this.query = query;
			return this;
		}
	
		public GoogleScraper setSearchModifier(String modifier, String value) {
			this.searchModifiers.put(modifier, value);
			return this;
		}
	
		public GoogleScraper setResultsPerPage(long resultsPerPage) {
			this.resultsPerPage = Math.min(resultsPerPage, MAX_RESULTS_PER_PAGE);
			return this;
		}
	
		public GoogleScraper setThrottleMillis(long throttleMillis) {
			this.throttleMillis = Math.max(throttleMillis, MIN_THROTTLE_MILLIS);
			return this;
		}
	
		public boolean isOnLastPage() {
			return isOnLastPage;
		}
	
		private void clearPageBuffer() {
			this.pageBuffer = null;
		}
	
		public Set<URL> getURLsFromPage() {
			if (this.pageBuffer == null) {
				throw new IllegalStateException("No cached page. Use nextPage to buffer a page");
			}
			
			if (isOnLastPage()) {
				throw new IllegalStateException("The final page has been reached");
			}
			
			Document searchDoc = Jsoup.parse(pageBuffer);
			Set<URL> pageURLs = new HashSet<URL>();
			Elements anchors = searchDoc.select(RESULTS_ANCHOR_SELECTOR);
			
			if (anchors.size() < 1) {
				this.isOnLastPage = true;
				return pageURLs;
			}
			
			for (Element anchor : anchors) {
				String anchorHref = anchor.attr("href");
				
				Matcher URLMatcher = URL_EXTRACTION_PATTERN.matcher(anchorHref);
				if (URLMatcher.matches()) {
					anchorHref = URLMatcher.group(1);
				}
				
				try {
					pageURLs.add(new URL(anchorHref));
				} catch (MalformedURLException ex) {
					// consume
					continue;
				}
			}
			
			return pageURLs;
		}
	
		public void nextPage() throws GoogleException, GoogleScrapeDetectedException {
			if (isOnLastPage())
				throw new IllegalStateException("No more pages left");
			
			try {
				clearPageBuffer();
				HttpResponse response = this.cookieHeader == null 
						? HTTPUtils.get(getQueryURL(), this.userAgentHeader)
						: HTTPUtils.get(getQueryURL(), this.userAgentHeader, this.cookieHeader);
				
				if (!HTTPUtils.checkResponseOK(response)) {
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
						// they're onto us :)
						throw new GoogleScrapeDetectedException();
					} else {
						throw new GoogleException("Unexpected response status " + response.getStatusLine().getStatusCode());
					}
				}
				
				if (this.cookieHeader == null) {
					this.cookieHeader = HTTPUtils.extractCookieHeader(response);
					System.out.println(this.cookieHeader);
				}
				
				this.pageBuffer = EntityUtils.toString(response.getEntity());
				
				if (isLastPage(this.pageBuffer)) {
					this.isOnLastPage = true;
				}
				
				this.offset += resultsPerPage;
			} catch (GoogleException gex) {
				throw gex;
			} catch (Throwable t) {
				throw new GoogleException(t);
			}
		}
	
		public void nextPageThrottled() throws GoogleException {
			try {
				Thread.sleep(throttleMillis);
			} catch (InterruptedException ex) {
				throw new GoogleException(ex);
			}
			nextPage();
		}
	}
}
