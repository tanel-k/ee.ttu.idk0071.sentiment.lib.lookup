package ee.ttu.idk0071.sentiment.lib.fetching.api;

import java.util.List;

import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;

public interface Fetcher {
	/**
	 * Retrieve a collection of text Strings that the data source matches to the specified query
	 * 
	 * @throws FetchException when the fetching cannot be completed
	 */
	List<String> fetch(Query query) throws FetchException;
}
