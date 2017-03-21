package ee.ttu.idk0071.sentiment.lib.fetching.objects;

public class ScrapeException extends Exception {
	private static final long serialVersionUID = 1225812888407759876L;

	public ScrapeException(Throwable t) {
		super(t);
	}

	public ScrapeException(String message, Throwable t) {
		super(message, t);
	}

	public ScrapeException(String message) {
		super(message);
	}
}
