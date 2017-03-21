package ee.ttu.idk0071.sentiment.lib.analysis.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ee.ttu.idk0071.sentiment.lib.analysis.api.SentimentAnalyzer;
import ee.ttu.idk0071.sentiment.lib.analysis.api.SentimentRetrievalException;
import ee.ttu.idk0071.sentiment.lib.analysis.objects.SentimentType;

public class ViveknSentimentAnalyzer implements SentimentAnalyzer {
	private static final String API_URL = "http://sentiment.vivekn.com/api/text/";

	public SentimentType getSentiment(String text) throws SentimentRetrievalException {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(API_URL);
			List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("txt", text));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			String response = EntityUtils.toString(client.execute(post).getEntity());
			JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
			JsonObject jsonResult = responseJson.get("result").getAsJsonObject();
			
			return SentimentType.valueOf(jsonResult.get("sentiment").getAsString().toUpperCase());
		} catch (Throwable t) {
			throw new SentimentRetrievalException(t);
		}
	}
}
