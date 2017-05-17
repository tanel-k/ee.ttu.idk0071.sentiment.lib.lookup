package ee.ttu.idk0071.sentiment.lib.utils;

import java.util.concurrent.TimeUnit;

public class Poller {
	public static class PollFailedException extends Exception {
		private static final long serialVersionUID = 8156244188549328667L;
	}

	public static interface Condition {
		public boolean isTrue();
	}

	private Condition condition;
	private int retries;
	private long cooldownMillis;
	private boolean ignoreErrors;

	public Poller setRetries(int retries) {
		this.retries = retries;
		return this;
	}

	public Poller setCooldown(long cooldown, TimeUnit unit) {
		if (cooldown < 0)
			throw new IllegalArgumentException("Cooldown duration must be non-negative");
		
		this.cooldownMillis = unit.toMillis(cooldown);
		return this;
	}

	public Poller setCondition(Condition operation) {
		this.condition = operation;
		return this;
	}

	public Poller setIgnoreErrors(boolean ignoreErrors) {
		this.ignoreErrors = ignoreErrors;
		return this;
	}

	public void poll() throws PollFailedException {
		if (this.condition == null)
			throw new IllegalStateException("No operation to perform");
		
		int attemptsLeft = retries;
		while (--attemptsLeft > 0) {
			try {
				if (condition.isTrue()) {
					return;
				}
			} catch (Throwable t) {
				if (!ignoreErrors) {
					throw new PollFailedException();
				}
			}
			
			try {
				Thread.sleep(cooldownMillis);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		
		throw new PollFailedException();
	}
}
