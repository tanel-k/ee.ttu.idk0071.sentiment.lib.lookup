package ee.ttu.idk0071.sentiment.lib.browsing.api;

import java.util.List;

public interface Browser {
	public void open(String URL);
	public void scrollToBottom();
	public void click(String querySelector);
	public void setValue(String querySelector, String value);
	public void close();

	public List<String> getAttributeValues(String querySelector, String attribute);
	public List<String> getTexts(String querySelector);
	public int countElements(String querySelector);
	public Object executeScript(String script);
	public String getHTML();
}
