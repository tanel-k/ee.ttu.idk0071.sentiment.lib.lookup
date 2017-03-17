package ee.ttu.idk0071.sentiment.lib.analysis.objects;

public class PageSentiment {
	private SentimentType sentimentType;
	private double trustLevel;

	public SentimentType getSentimentType() {
		return sentimentType;
	}

	public void setSentimentType(SentimentType sentimentType) {
		this.sentimentType = sentimentType;
	}

	public double getTrustLevel() {
		return trustLevel;
	}

	public void setTrustLevel(double trustLevel) {
		this.trustLevel = trustLevel;
	}
}
