package ee.ttu.idk0071.sentiment.lib.searching.api;

import java.util.List;

import ee.ttu.idk0071.sentiment.lib.searching.objects.Query;

public interface Searcher {
	List<String> search(Query query);
}
