package ee.ttu.idk0071.sentiment.lib.searching;

import java.util.List;

import ee.ttu.idk0071.sentiment.lib.searching.objects.SocialMediaQuery;
import ee.ttu.idk0071.sentiment.lib.searching.objects.SocialMediaResult;

public interface SocialMediaSearcher {
	public List<SocialMediaResult> search(SocialMediaQuery query);

}
