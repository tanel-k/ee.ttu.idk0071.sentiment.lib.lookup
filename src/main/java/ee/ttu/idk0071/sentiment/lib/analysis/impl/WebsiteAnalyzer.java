package ee.ttu.idk0071.sentiment.lib.analysis.impl;

import ee.ttu.idk0071.sentiment.lib.analysis.SentimentAPI;
import ee.ttu.idk0071.sentiment.lib.analysis.SentimentRetrievalException;
import ee.ttu.idk0071.sentiment.lib.analysis.objects.SentimentResult;
import ee.ttu.idk0071.sentiment.lib.utils.HtmlUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HtmlUtils.TextExtractionException;
import ee.ttu.idk0071.sentiment.lib.utils.HttpUtils;
import ee.ttu.idk0071.sentiment.lib.utils.HttpUtils.HtmlRetrievalException;

public class WebsiteAnalyzer {
	private int timeOutMillis;

	public WebsiteAnalyzer() {
		
	}

	public WebsiteAnalyzer(int timeOutMillis) {
		this.timeOutMillis = timeOutMillis;
	}

	public int getTimeOutMillis() {
		return timeOutMillis;
	}

	public void setTimeOutMillis(int timeOutMillis) {
		this.timeOutMillis = timeOutMillis;
	}

	public SentimentResult analyze(String url) 
			throws HtmlRetrievalException, TextExtractionException, SentimentRetrievalException {
		SentimentAPI viveknAPI = new ViveknSentimentAPI();
		
		// get raw HTML
		String html = HttpUtils.getHtml(url, timeOutMillis);
		
		// get human-readable text from HTML
		String text = HtmlUtils.getText(html);
		
		// analyze text
		return viveknAPI.getSentiment(text);
	}	
}
