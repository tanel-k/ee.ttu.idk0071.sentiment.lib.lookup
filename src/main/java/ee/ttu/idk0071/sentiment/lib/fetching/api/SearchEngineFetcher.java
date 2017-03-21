package ee.ttu.idk0071.sentiment.lib.fetching.api;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.ScrapeException;
import ee.ttu.idk0071.sentiment.lib.utils.HTMLUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HTMLUtils.TextExtractionException;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils.HtmlRetrievalException;

public abstract class SearchEngineFetcher implements Fetcher {
	/**
	 * Perform a search against the engine and retrieve (scrape) a list of URLs
	 * 
	 * @throws ScrapeException when the search fails
	 */
	protected abstract List<URL> scrapeURLs(Query query) throws ScrapeException;

	public List<String> fetch(Query query) throws FetchException {
		List<URL> URLs;
		
		try {
			URLs = scrapeURLs(query);
		} catch (ScrapeException e) {
			throw new FetchException(e);
		}
		
		List<String> results = new LinkedList<String>();
		
		for (URL url : URLs) {
			try {
				String html = getHtml(url);
				String text = getText(html);
				results.add(text);
			} catch (HtmlRetrievalException e) {
				// no recovery
			} catch (TextExtractionException e) {
				// no recovery
			}
		}
		
		return results;
	}

	private String getHtml(URL fromURL) throws HtmlRetrievalException {
		return HTTPUtils.getHtml(fromURL);
	}

	private String getText(String html) throws TextExtractionException {
		return HTMLUtils.getText(html);
	}
}
