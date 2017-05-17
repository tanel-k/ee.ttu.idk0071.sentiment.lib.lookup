package ee.ttu.idk0071.sentiment.lib.browsing.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ui4j.api.browser.BrowserEngine;
import com.ui4j.api.browser.Page;
import com.ui4j.api.dom.Element;
import com.ui4j.webkit.WebKitBrowserProvider;

import ee.ttu.idk0071.sentiment.lib.browsing.api.BrowserSimulator;

public class UI4JBrowser implements BrowserSimulator {
	static WebKitBrowserProvider browserEngineProvider = new WebKitBrowserProvider();

	BrowserEngine browserEngine;
	Page currentPage;

	@Override
	public void scrollToBottom() {
		currentPage.executeScript("document.body.scrollTop = document.body.scrollHeight");
	}

	@Override
	public void open(String URL) {
		this.currentPage = browserEngine.navigate(URL);
	}

	@Override
	public Object executeScript(String script) {
		return currentPage.executeScript(script);
	}

	@Override
	public void click(String querySelector) {
		query(querySelector).ifPresent(el -> el.click());
	}

	@Override
	public void setValue(String querySelector, String value) {
		query(querySelector).ifPresent(el -> el.setValue(value));
	}

	@Override
	public void close() {
		currentPage.close();
		// do not use browserEngine.shutdown() unless you want to restart the jar
	}

	@Override
	public String getHTML() {
		return String.class.cast(currentPage.executeScript("document.documentElement.innerHTML"));
	}

	@Override
	public List<String> getTexts(String querySelector) {
		return queryAll(querySelector).stream()
				.map(el -> el.getText())
				.filter(optTxt -> optTxt.isPresent())
				.map(optTxt -> optTxt.get())
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getAttributeValues(String querySelector, String attribute) {
		return queryAll(querySelector).stream()
				.map(el -> el.getAttribute(attribute))
				.filter(optTxt -> optTxt.isPresent())
				.map(optTxt -> optTxt.get())
				.collect(Collectors.toList());
	}

	@Override
	public int countElements(String querySelector) {
		return queryAll(querySelector).size();
	}

	@Override
	public boolean doesElementExist(String querySelector) {
		Optional<Element> optElement = query(querySelector);
		return optElement != null && optElement.isPresent();
	}

	@Override
	public String getElementText(String querySelector) {
		if (doesElementExist(querySelector)) {
			return query(querySelector).get().getInnerHTML();
		}
	
		return null;
	}

	protected Optional<Element> query(String querySelector) {
		return currentPage.getDocument().query(querySelector);
	}

	protected List<Element> queryAll(String querySelector) {
		return currentPage.getDocument().queryAll(querySelector);
	}

	public UI4JBrowser() {
		browserEngine = browserEngineProvider.create();
	}
}
