package ee.ttu.idk0071.sentiment.lib.analysis.impl;

import de.l3s.boilerpipe.extractors.ArticleExtractor;
import ee.ttu.idk0071.sentiment.lib.analysis.SentimentAPI;
import ee.ttu.idk0071.sentiment.lib.analysis.SentimentAnalysisException;
import ee.ttu.idk0071.sentiment.lib.analysis.SentimentAnalyzer;
import ee.ttu.idk0071.sentiment.lib.analysis.objects.PageSentiment;
import ee.ttu.idk0071.sentiment.lib.utils.HttpUtils;

public class BasicSentimentAnalyzer implements SentimentAnalyzer {
	private int timeOutMillis;

	public BasicSentimentAnalyzer() {
		
	}

	public BasicSentimentAnalyzer(int timeOutMillis) {
		this.timeOutMillis = timeOutMillis;
	}

	public int getTimeOutMillis() {
		return timeOutMillis;
	}

	public void setTimeOutMillis(int timeOutMillis) {
		this.timeOutMillis = timeOutMillis;
	}

	public PageSentiment analyzePage(String url) throws SentimentAnalysisException {
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
			throw new SentimentAnalysisException(t);
		}
	}	
}
