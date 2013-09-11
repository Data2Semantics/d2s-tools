package org.data2semantics.platform.util;

import org.data2semantics.platform.Global;

public class Functions
{
	/** 
	 * toString with null check
	 * @param in
	 * @return
	 */
	public static String toString(Object in)
	{
		if(in == null)
			return "null";
		return in.toString();
	}
	
	/** 
	 * equals with null check
	 * @param in
	 * @return
	 */
	public static boolean equals(Object first, Object second)
	{
		if(first == null)
			return second == null;
		
		return first.equals(second);
	}
	
	/**
	 * Produces a random string of the given length, to serve as a unique identifier
	 * 
	 * @param stringLength
	 * @returns {String}
	 */
	public static String randomString(int length) 
	{
		String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
		StringBuilder randomString = new StringBuilder();
		for (int i : Series.series(length)) 
		{
			int rnum = Global.random().nextInt(chars.length());
			randomString.append(chars.charAt(rnum));
		}
		
		return randomString.toString();
	}
}
