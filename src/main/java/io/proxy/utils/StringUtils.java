package io.proxy.utils;

public abstract class StringUtils {

	public static boolean isEmpty(final CharSequence c) {
		return c == null || c.length() == 0;
	}
}
