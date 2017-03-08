package ee.ttu.idk0071.sentiment.lib.analysis;

import ee.ttu.idk0071.sentiment.lib.analysis.objects.PageSentiment;

public interface SentimentAnalyzer {
	PageSentiment analyzePage(String url) throws SentimentAnalysisException;
}
