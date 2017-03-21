package ee.ttu.idk0071.sentiment.lib.searching.api;

import java.util.List;

import ee.ttu.idk0071.sentiment.lib.searching.objects.Query;
import ee.ttu.idk0071.sentiment.lib.searching.objects.FetchException;

public interface Fetcher {
	/**
	 * Retrieve a collection of Strings that match the specified query
	 * 
	 * @throws FetchException when the fetch process cannot be completed
	 */
	List<String> fetch(Query query) throws FetchException;
}
