package ee.ttu.idk0071.sentiment.lib.fetching.objects;

public class Query {
	private String keyword;
	private Long maxResults;
	private Credentials credentials;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Long getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(long maxResults) {
		this.maxResults = maxResults;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public Query() {
		
	}

	public Query(String keyword, long maxResults) {
		this.keyword = keyword;
		this.maxResults = maxResults;
	}
}
