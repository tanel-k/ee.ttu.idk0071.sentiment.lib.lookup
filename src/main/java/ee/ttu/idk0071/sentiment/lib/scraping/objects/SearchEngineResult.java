package ee.ttu.idk0071.sentiment.lib.scraping.objects;

public class SearchEngineResult {
	private long rank;
	private String title;
	private String url;

	public SearchEngineResult() {

	}

	public long getRank() {
		return rank;
	}

	public void setRank(long rank) {
		this.rank = rank;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}