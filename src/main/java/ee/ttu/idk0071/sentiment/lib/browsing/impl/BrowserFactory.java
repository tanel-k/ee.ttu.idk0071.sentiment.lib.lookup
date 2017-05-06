package ee.ttu.idk0071.sentiment.lib.browsing.impl;

import ee.ttu.idk0071.sentiment.lib.browsing.api.Browser;

public class BrowserFactory {
	public static Browser getBrowser() {
		return new UI4JBrowser();
	}
}
