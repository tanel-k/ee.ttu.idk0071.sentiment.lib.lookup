package ee.ttu.idk0071.sentiment.lib.analysis.objects;

public class PageSentiment {
	private SentimentType sentimentType;
	private float trustLevel;

	public SentimentType getSentimentType() {
		return sentimentType;
	}

	public void setSentimentType(SentimentType sentimentType) {
		this.sentimentType = sentimentType;
	}

	public float getTrustLevel() {
		return trustLevel;
	}

	public void setTrustLevel(float trustLevel) {
		this.trustLevel = trustLevel;
	}
}
