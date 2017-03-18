package ee.ttu.idk0071.sentiment.lib.analysis;

import ee.ttu.idk0071.sentiment.lib.analysis.objects.SentimentResult;

public interface SentimentAPI {
	public SentimentResult getSentiment(String text) throws SentimentRetrievalException;
}
