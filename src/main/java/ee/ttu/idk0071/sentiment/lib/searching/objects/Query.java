package ee.ttu.idk0071.sentiment.lib.searching.objects;

public class Query {
	private String keyword;
	private Long maxResults;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Long getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(Long maxResults) {
		this.maxResults = maxResults;
	}

	public Query() {
		
	}

	public Query(String keyword, Long maxResults) {
		this.keyword = keyword;
		this.maxResults = maxResults;
	}
}
