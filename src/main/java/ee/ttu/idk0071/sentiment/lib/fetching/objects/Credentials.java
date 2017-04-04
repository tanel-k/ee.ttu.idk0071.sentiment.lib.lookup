package ee.ttu.idk0071.sentiment.lib.fetching.objects;

import java.util.HashMap;
import java.util.Map;

public class Credentials {
	private Map<String, String> credentialMap = new HashMap<String, String>();

	public void set(String key, String credential) {
		this.credentialMap.put(key, credential);
	}

	public String get(String key) {
		return credentialMap.get(key);
	}

	public static Credentials from(Map<String, String> credentialMap) {
		Credentials container = new Credentials();
		container.credentialMap.putAll(credentialMap);
		return container;
	}
}
