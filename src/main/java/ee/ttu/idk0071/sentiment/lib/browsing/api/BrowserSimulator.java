package ee.ttu.idk0071.sentiment.lib.browsing.api;

import java.util.List;

/**
 * Some web pages are constructed after the page is loaded (generally SPAs)<br/>
 * We cannot interact with these pages by merely downloading raw HTML.<br/>
 * We must simulate browser logic so that we could get content from such pages.<br/>
 * This interface provides a set of methods for browser simulators.
 */
public interface BrowserSimulator {
	void open(String URL);
	void scrollToBottom();
	void click(String querySelector);
	void setValue(String querySelector, String value);
	void close();

	List<String> getAttributeValues(String querySelector, String attribute);
	List<String> getTexts(String querySelector);
	boolean doesElementExist(String querySelector);
	int countElements(String querySelector);
	Object executeScript(String script);
	String getElementText(String querySelector);
	String getHTML();
}
