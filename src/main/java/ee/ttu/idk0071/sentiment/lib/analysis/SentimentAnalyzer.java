package ee.ttu.idk0071.sentiment.lib.analysis;

import java.net.MalformedURLException;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import ee.ttu.idk0071.sentiment.lib.analysis.impl.ViveknSentimentAPI;
import ee.ttu.idk0071.sentiment.lib.analysis.objects.PageSentiment;
import ee.ttu.idk0071.sentiment.lib.utils.HttpUtils;

public class SentimentAnalyzer {
	public static PageSentiment analyze(String url, int timeOutMillis) throws BoilerpipeProcessingException, MalformedURLException {
		try {
			ArticleExtractor articleExtractor = ArticleExtractor.getInstance();
			SentimentAPI viveknAPI = new ViveknSentimentAPI();
			
			// get raw HTML
			String html = HttpUtils.getHtml(url, timeOutMillis);
			
			// get human-readable text from HTML
			String text = articleExtractor.getText(html);
			
			// analyze text
			return viveknAPI.getSentiment(text);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}	
}
