package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ee.ttu.idk0071.sentiment.lib.errorHandling.ErrorService;
import ee.ttu.idk0071.sentiment.lib.fetching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Credentials;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterFetcher implements Fetcher {
	public static final String CRED_KEY_CONSUMER_KEY = "consumer-key";
	public static final String CRED_KEY_CONSUMER_SECRET = "consumer-secret";
	public static final String CRED_KEY_ACCESS_TOKEN = "access-token";
	public static final String CRED_KEY_ACCESS_TOKEN_SECRET = "access-token-secret";
	private static final String CLASS_NAME = TwitterFetcher.class.getName();

	private static final String TWEET_LANG = "en";
	/** Tweets per twitter4j.Query max = 100 */
	private static final int TWEETS_PER_QUERY = 100;
	
	@Autowired
	public ErrorService errorService;
	
	public List<String> fetch(Query query) throws FetchException {
		try {
			
			TwitterFactory tf = getTwitterFactoryForCreds(query.getCredentials());
			Twitter twitter = tf.getInstance();
			
			long maxTweets = query.getMaxResults();
			long cntTweetsRetrieved = 0L;
			
			twitter4j.Query twitterQuery = new twitter4j.Query(query.getKeyword())
				.count(TWEETS_PER_QUERY)
				.lang(TWEET_LANG);
			
			List<String> results = new LinkedList<String>();
			PAGELOOP: do {
				QueryResult twitterResult = twitter.search(twitterQuery);
				
				for (Status tweet : twitterResult.getTweets()) {
					if (maxTweets > cntTweetsRetrieved) {
						results.add(tweet.getText());
						cntTweetsRetrieved++;
					} else {
						break PAGELOOP;
					}
				}
				
				if (!twitterResult.hasNext())
					break;
				
				twitterQuery = twitterResult.nextQuery();
			} while (maxTweets > cntTweetsRetrieved);
			
			return results;
			
		} catch (Throwable t) {
			errorService.saveError(t, CLASS_NAME);
			throw new FetchException(t);
		}
	}

	private TwitterFactory getTwitterFactoryForCreds(Credentials credentials) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setDebugEnabled(false)
			.setOAuthConsumerKey(credentials.get(CRED_KEY_CONSUMER_KEY))
			.setOAuthConsumerSecret(credentials.get(CRED_KEY_CONSUMER_SECRET))
			.setOAuthAccessToken(credentials.get(CRED_KEY_ACCESS_TOKEN))
			.setOAuthAccessTokenSecret(credentials.get(CRED_KEY_ACCESS_TOKEN_SECRET));
		return new TwitterFactory(builder.build());
	}
}
