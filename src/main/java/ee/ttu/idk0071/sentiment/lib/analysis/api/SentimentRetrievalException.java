package ee.ttu.idk0071.sentiment.lib.analysis.api;

public class SentimentRetrievalException extends Exception {
	private static final long serialVersionUID = 3229969904392032617L;

	public SentimentRetrievalException(String message) {
		super(message);
	}

	public SentimentRetrievalException(String message, Throwable t) {
		super(message, t);
	}

	public SentimentRetrievalException(Throwable t) {
		super(t);
	}
}
