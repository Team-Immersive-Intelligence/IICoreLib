package net.iiteam.corelib;

import java.util.Arrays;

/**
 * Class with various array related utilities.
 *
 * @author Pabilo8 (pabilo@iiteam.net)
 * @author Gabriel (gabriel@iiteam.net)
 * @since 19/08/2026
 */
public class ArrayUtil
{
	/**
	 * Compare two arrays of same element type.
	 *
	 * @param a First array to compare
	 * @param b Second array to compare
	 * @return Whether arrays are equal (including if the length is equal)
	 */
	public static <T> boolean equals(T[] a, T[] b) {
		if (a.length != b.length) return false;
		for (int i = 0; i < a.length; i++) {
			if (!a[i].equals(b[i])) return false;
		}
		return true;
	}

	/**
	 * Compare if <code>len</code> elements
	 * of both arrays are equal
	 *
	 * @param a First array to compare
	 * @param b Second array to compare
	 * @param len Amount of elements to compare
	 * @return Whether arrays are equal
	 */
	public static <T> boolean equalsForLen(T[] a, T[] b, int len) {
		if (a.length < len || b.length < len) return false;
		for (int i = 0; i < len; i++) {
			if (!a[i].equals(b[i])) return false;
		}
		return true;
	}

	/**
	 * Returns a reversed copy of an array.
	 *
	 * @param array array to reverse
	 * @param <T>   array type
	 * @return reversed array
	 * @implNote Does not modify the input array.
	 */
	public static <T> T[] reverseArray(T[] array)
	{
		T[] reversed = Arrays.copyOf(array, array.length);
		for(int i = 0; i < array.length; i++)
			reversed[i] = array[array.length-1-i];
		return reversed;
	}
}
