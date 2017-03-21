package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.net.URL;
import java.util.List;

import ee.ttu.idk0071.sentiment.lib.fetching.api.SearchEngineFetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;

public class YahooFetcher extends SearchEngineFetcher {

	@Override
	protected List<URL> scrapeURLs(Query query) throws FetchException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
