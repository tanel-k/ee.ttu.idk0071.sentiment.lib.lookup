package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.util.List;

import ee.ttu.idk0071.sentiment.lib.fetching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;

public class TumblrFetcher implements Fetcher {
	@Override
	public List<String> fetch(Query query) throws FetchException {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
}
