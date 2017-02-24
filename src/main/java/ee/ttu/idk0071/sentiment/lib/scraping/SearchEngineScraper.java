package ee.ttu.idk0071.sentiment.lib.scraping;

import java.util.List;

import ee.ttu.idk0071.sentiment.lib.scraping.objects.SearchEngineQuery;
import ee.ttu.idk0071.sentiment.lib.scraping.objects.SearchEngineResult;

public interface SearchEngineScraper {
	public List<SearchEngineResult> search(SearchEngineQuery query);
}
