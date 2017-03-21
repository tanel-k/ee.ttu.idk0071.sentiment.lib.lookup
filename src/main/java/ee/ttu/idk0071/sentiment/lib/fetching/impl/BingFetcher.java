package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.net.URL;
import java.util.List;

import ee.ttu.idk0071.sentiment.lib.fetching.api.SearchEngineFetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.ScrapeException;

public class BingFetcher extends SearchEngineFetcher {

	@Override
	protected List<URL> scrapeURLs(Query query) throws ScrapeException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
