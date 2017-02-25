package ee.ttu.idk0071.sentiment.lib.scraping.objects;

public class SearchEngineQuery {
	private String queryString;
	private int maxResults;

	public SearchEngineQuery(String queryString, int maxResults) {
		this.queryString = queryString;
		this.maxResults = maxResults;
	}

	public String getQueryString() {
		return queryString;
	}

	public int getMaxResults() {
		return maxResults;
	}
}