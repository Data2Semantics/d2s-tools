package org.data2semantics.platform.util;

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
}
