package ee.ttu.idk0071.sentiment.lib.utils;

import java.util.LinkedList;
import java.util.List;

public class LongPartition {
	public static final long MAX_INT_AS_LONG = intToLong(Integer.MAX_VALUE);
	public static final long MIN_INT_AS_LONG = intToLong(Integer.MIN_VALUE);
	public static final int MAX_INT = Integer.MAX_VALUE;
	public static final int MIN_INT = Integer.MIN_VALUE;

	private List<Integer> integerPartitions = new LinkedList<>();
	private boolean fitsInteger = false;
	private long longValue;
	private int intValue;

	public boolean fitsInteger() {
		return fitsInteger;
	}

	public Integer asInteger() throws IllegalStateException {
		if (!fitsInteger)
			throw new IllegalStateException();
		
		return intValue;
	}

	public long asLong() {
		return longValue;
	}

	/**
	 * @return the encapsulated long value partitioned into a series of integers
	 */
	public List<Integer> getIntegerPartitions() {
		return new LinkedList<Integer>(integerPartitions);
	}

	public LongPartition(long longValue) {
		this.longValue = longValue;
		
		if (fitsInInteger(longValue)) {
			this.fitsInteger = true;
			this.intValue = longToInt(longValue);
			this.integerPartitions.add(intValue);
			return;
		}
		
		int integerLimitValue = longValue > 0
				? MAX_INT
				: MIN_INT;
		long longLimitIncrement = longValue > 0
				? MAX_INT_AS_LONG
				: MIN_INT_AS_LONG;
		
		// remove limit values until longValue fits into int
		while (!fitsInInteger(longValue)) {
			longValue -= longLimitIncrement;
			this.integerPartitions.add(integerLimitValue);
		}
		
		// last partition
		this.integerPartitions.add(longToInt(longValue));
	}

	/**
	 * @return the input long COMPRESSED to an int
	 */
	public static int longToInt(long rawLong) {
		return Long.valueOf(rawLong).intValue();
	}

	/**
	 * @return the input int as a long
	 */
	public static long intToLong(int rawInt) {
		return Integer.valueOf(rawInt).longValue();
	}

	public static boolean fitsInInteger(long longValue) {
		return between(MIN_INT_AS_LONG, longValue, MAX_INT_AS_LONG);
	}

	private static boolean between(long first, long value, long last) {
		return first <= value && value <= last;
	}
}
