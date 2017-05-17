package ee.ttu.idk0071.sentiment.lib.browsing.impl;

import ee.ttu.idk0071.sentiment.lib.browsing.api.BrowserSimulator;

public class BrowserFactory {
	public static BrowserSimulator getBrowser() {
		return new UI4JBrowser();
	}
}
