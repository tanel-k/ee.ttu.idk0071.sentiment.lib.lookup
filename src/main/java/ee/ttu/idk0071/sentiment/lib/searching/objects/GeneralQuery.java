package ee.ttu.idk0071.sentiment.lib.searching.objects;

public class GeneralQuery {
	private String queryString;
	private int maxResults;

	public String getQueryString() {
		return queryString;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public GeneralQuery(String queryString, int maxResults) {
		this.queryString = queryString;
		this.maxResults = maxResults;
	}
}
