package ee.ttu.idk0071.sentiment.lib.scraping.objects;

import ee.ttu.idk0071.sentiment.lib.searching.objects.GeneralQuery;

public class SearchEngineQuery extends GeneralQuery {
	public SearchEngineQuery(String queryString, int maxResults) {
		super(queryString, maxResults);
	}
}