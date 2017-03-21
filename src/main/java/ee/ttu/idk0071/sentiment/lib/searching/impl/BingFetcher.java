package ee.ttu.idk0071.sentiment.lib.searching.impl;

import java.net.URL;
import java.util.List;

import ee.ttu.idk0071.sentiment.lib.searching.api.SearchEngineFetcher;
import ee.ttu.idk0071.sentiment.lib.searching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.searching.objects.FetchException;

public class BingFetcher extends SearchEngineFetcher {

	@Override
	protected List<URL> scrapeURLs(Query query) throws FetchException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
