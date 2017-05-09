package ee.ttu.idk0071.sentiment.lib.analysis.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
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
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;

public class TextProcessingComAnalyzer implements SentimentAnalyzer {
	private static final int MAX_TEXT_LENGTH = 80000;
	private static final String API_URL = "http://text-processing.com/api/sentiment/";
	private static final String API_TEST_TEXT = "great";
	private static final SentimentType API_TEST_TEXT_SENTIMENT = SentimentType.POSITIVE;
	private static final Map<String, SentimentType> SENTIMENT_LABEL_MAP = new ConcurrentHashMap<>();

	static {
		SENTIMENT_LABEL_MAP.put("pos", SentimentType.POSITIVE);
		SENTIMENT_LABEL_MAP.put("neg", SentimentType.NEGATIVE);
		SENTIMENT_LABEL_MAP.put("neutral", SentimentType.NEUTRAL);
	}

	@Override
	public SentimentType getSentiment(String text) throws SentimentRetrievalException {
		try {
			if (text.length() > MAX_TEXT_LENGTH) {
				throw new SentimentRetrievalException("Max text length exceeded");
			}
			
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(API_URL);
			List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("text", text));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = client.execute(post);
			if (!HTTPUtils.checkResponseOK(response)) {
				throw new SentimentRetrievalException("Rejected according to response code: " 
						+ response.getStatusLine().getStatusCode());
			}
			
			String responseString = EntityUtils.toString(response.getEntity());
			JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
			String responseLabel = responseJson.get("label").getAsString();
			
			SentimentType sentiment = SENTIMENT_LABEL_MAP.get(responseLabel);
			if (sentiment == null) {
				throw new SentimentRetrievalException("Unknown label received: " + responseLabel);
			}
			
			return sentiment;
		} catch (Throwable t) {
			throw new SentimentRetrievalException(t);
		}
	}

	@Override
	public boolean isAvailable() {
		try {
			return API_TEST_TEXT_SENTIMENT == this.getSentiment(API_TEST_TEXT);
		} catch (Throwable t) {
			return false;
		}
	}

}
