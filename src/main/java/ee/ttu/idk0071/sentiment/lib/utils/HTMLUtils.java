package ee.ttu.idk0071.sentiment.lib.utils;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

public class HTMLUtils {
	public static class TextExtractionException extends Exception {
		private static final long serialVersionUID = -4444580746791264076L;
	
		public TextExtractionException(Throwable t) {
			super(t);
		}
	}

	public static String getText(String html) throws TextExtractionException{
		try {
			ArticleExtractor articleExtractor = ArticleExtractor.getInstance();
			return articleExtractor.getText(html);
		} catch (BoilerpipeProcessingException ex) {
			throw new TextExtractionException(ex);
		}
	}
}
