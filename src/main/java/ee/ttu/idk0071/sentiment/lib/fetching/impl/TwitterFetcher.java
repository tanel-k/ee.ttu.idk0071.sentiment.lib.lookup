package ee.ttu.idk0071.sentiment.lib.fetching.impl;

import java.util.ArrayList;
import java.util.List;

import ee.ttu.idk0071.sentiment.lib.fetching.api.Fetcher;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.FetchException;
import ee.ttu.idk0071.sentiment.lib.fetching.objects.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterFetcher implements Fetcher {
	
	public List<String> fetch(Query query) throws FetchException {
		
		int searchResultCount;
		long lowestTweetId = Long.MAX_VALUE;
		int maxresults = longToInt(query.getMaxResults());		
		
		List<String> results = new ArrayList<String>();
		
		try {
			do {
				TwitterFactory tf = twitterAuth();
				Twitter twitter = tf.getInstance();
				twitter4j.Query twitterQuery = new twitter4j.Query(query.getKeyword())
						.count(maxresults)  //100 or maxresults when maxresults < 100
			            .lang("en");
				
				QueryResult result = twitter.search(twitterQuery);
			
			    searchResultCount = result.getTweets().size();
			    maxresults -= searchResultCount;
			    
			    for (Status tweet : result.getTweets()) {
			    	results.add(tweet.getText());
			    
			    	if (tweet.getId() < lowestTweetId) {
			            lowestTweetId = tweet.getId();
			            twitterQuery.setMaxId(lowestTweetId);
			        }
			    }
			 } while (maxresults != 0 && maxresults > 0);
	    
		}catch (Throwable t) {
			throw new FetchException(t);
		}
		return results;
	}
	
	private TwitterFactory twitterAuth(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("VyDoq2JP8ZVNXAvFhA0Al0M3Q")
		  .setOAuthConsumerSecret("KSA9Yec0XMBWtx6Pt9gJXiQpIstDkWbT55XoboM43IelgTjh4H")
		  .setOAuthAccessToken("2818155769-DrOgpOFsTTfFQgQJG00PscdRiRJsYr5RMfVe0mn")
		  .setOAuthAccessTokenSecret("zdd5qdUe75K6lw0NmkSv4hVaYIcf7dOV8CZP3z0Fkvvo4");
		TwitterFactory tf = new TwitterFactory(cb.build());
		//Twitter twitter = tf.getInstance();
		return tf;
	}
	
	private int longToInt(Long l){
		if (l > Integer.MAX_VALUE){ System.out.println("int: "+Integer.MAX_VALUE); return Integer.MAX_VALUE;}
	    if (l < Integer.MIN_VALUE){ System.out.println("int: "+Integer.MIN_VALUE); return Integer.MIN_VALUE;}
	    System.out.println("int: " + (int) (long) l);
	    return (int) (long) l;
	}
	/*
	private int longToInt(Long l){
		if (l > Integer.MAX_VALUE) { 
			return Integer.MAX_VALUE;
	    }else if (l < Integer.MIN_VALUE) 
	    	return Integer.MIN_VALUE;
	    return (int) (long) l;
	}
	 */
	
}
