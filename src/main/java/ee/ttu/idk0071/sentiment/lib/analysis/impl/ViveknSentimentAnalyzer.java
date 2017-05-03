package ee.ttu.idk0071.sentiment.lib.analysis.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ee.ttu.idk0071.sentiment.lib.analysis.api.SentimentAnalyzer;
import ee.ttu.idk0071.sentiment.lib.analysis.api.SentimentRetrievalException;
import ee.ttu.idk0071.sentiment.lib.analysis.objects.SentimentType;
import ee.ttu.idk0071.sentiment.lib.errorHandling.ErrorService;
import ee.ttu.idk0071.sentiment.lib.utils.HTTPUtils;

public class ViveknSentimentAnalyzer implements SentimentAnalyzer {
	private static final String API_URL = "http://sentiment.vivekn.com/api/text/";
	private static final String MAIN_PAGE_URL = "http://sentiment.vivekn.com";
	private static final String CLASS_NAME = ViveknSentimentAnalyzer.class.getName();
	
	@Autowired
	public ErrorService errorService;

	@Override
	public SentimentType getSentiment(String text) throws SentimentRetrievalException {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(API_URL);
			List<NameValuePair> nameValuePairs = new LinkedList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("txt", text));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = client.execute(post);
			if (!HTTPUtils.checkResponseOK(response)) {
				throw new SentimentRetrievalException("Rejected according to response code");
			}
			
			String responseString = EntityUtils.toString(client.execute(post).getEntity());
			JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
			JsonObject jsonResult = responseJson.get("result").getAsJsonObject();
			String sentimentString = jsonResult.get("sentiment").getAsString();
			
			if (sentimentString == null) {
				throw new SentimentRetrievalException("Response did not contain a sentiment estimation");
			}
			
			return SentimentType.valueOf(sentimentString.toUpperCase());
		} catch (Throwable t) {
			errorService.saveError(t, CLASS_NAME);
			throw new SentimentRetrievalException(t);
		}
	}

	@Override
	public boolean isAvailable() {
		return HTTPUtils.checkHeadOK(MAIN_PAGE_URL);
	}
}
