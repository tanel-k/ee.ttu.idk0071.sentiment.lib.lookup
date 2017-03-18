package ee.ttu.idk0071.sentiment.lib.scraping.objects;

public class SearchEngineResult {
	private String title;
	private String url;

	public SearchEngineResult() {

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