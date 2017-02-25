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

import ee.ttu.idk0071.sentiment.lib.analysis.SentimentAPI;
import ee.ttu.idk0071.sentiment.lib.analysis.objects.PageSentiment;
import ee.ttu.idk0071.sentiment.lib.analysis.objects.SentimentType;

public class ViveknSentimentAPI implements SentimentAPI {
	private static final String API_URL = "http://sentiment.vivekn.com/api/text/";

	public PageSentiment getSentiment(String text) {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(API_URL);
			List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("txt", text));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			String response = EntityUtils.toString(client.execute(post).getEntity());
			JsonObject responseJson = new JsonParser().parse(response).getAsJsonObject();
			JsonObject jsonResult = responseJson.get("result").getAsJsonObject();
			
			PageSentiment sentiment = new PageSentiment();
			sentiment.setTrustLevel(jsonResult.get("confidence").getAsFloat());
			sentiment.setSentimentType(SentimentType.valueOf(jsonResult.get("sentiment").getAsString().toUpperCase()));
			return sentiment;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
