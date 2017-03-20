package ee.ttu.idk0071.sentiment.lib.analysis.api;

import ee.ttu.idk0071.sentiment.lib.analysis.objects.SentimentType;

public interface SentimentAnalyzer {
	public SentimentType getSentiment(String text) throws SentimentRetrievalException;
}
