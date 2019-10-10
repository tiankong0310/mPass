package com.ibyte.common.util;

public class RandomUtil {

	public static long getInteger(long minValue, long maxValue) {
		return (long) (Math.random() * (maxValue - minValue + 1)) + minValue;
	}

	public static String getDouble(int exponent, int precision) {
		StringBuilder sb = new StringBuilder();
		sb.append((Math.random() < 0.5 ? "+" : "-"));
		sb.append((int) (Math.random() * 10) + ".");
		precision = (int) (Math.random() * precision);
		for (int i = 0; i < precision; i++)
			sb.append((int) (Math.random() * 10));
		sb.append("e" + (Math.random() < 0.5 ? "+" : "-") + ((int) (Math.random() * (exponent + 1))));
		return sb.toString();
	}

	// a.b; .b; a.
	public static String getDecimal(int p, int s) {
		StringBuilder sb = new StringBuilder(Math.random() < 0.5 ? "+" : "-");
		while (true) {
			int size1 = (int) (Math.random() * (p - s + 1));
			int size2 = (int) (Math.random() * (s + 1));
			for (int i = 0; i < size1; i++)
				sb.append((int) (Math.random() * 10));
			sb.append(".");
			for (int i = 0; i < size2; i++)
				sb.append((int) (Math.random() * 10));
			if (!sb.toString().matches("[+-.]+"))
				break;
			sb.delete(0, sb.length());
			sb.append(Math.random() < 0.5 ? "+" : "-");
		}
		return sb.toString();
	}

	public static long getDate(long beginDateMillisecond, long endDateMillisecond) {
		return (long) (Math.random() * (endDateMillisecond - beginDateMillisecond + 1) + beginDateMillisecond);
	}

	private static char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	public static String getString(int length) {
		int randomLength = (int) (Math.random() * (length + 1));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < randomLength; i++)
			sb.append(chars[(int) (Math.random() * 62)]);
		return sb.toString();
	}

	public static char getChar() {
		return chars[(int) (Math.random() * 62)];
	}

	public static boolean getBoolean() {
		return Math.random() < 0.5 ? true : false;
	}

	public static double getNumeric(double minValue, double maxValue) {
		return Math.random() * (maxValue - minValue + 1) + minValue;
	}

}
