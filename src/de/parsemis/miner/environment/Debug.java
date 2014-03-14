/**
 * created Aug 22, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.miner.environment;

import java.io.PrintStream;

/**
 * This class collects/stores information about the configured debug level
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public final class Debug {
	public static final boolean QUIET = (System.getProperty("quiet") != null)
			|| (System.getProperty("debug") != null && System.getProperty(
					"debug").equals("0"));

	public static final boolean VVVERBOSE = (System.getProperty("vvverbose") != null)
			|| (System.getProperty("debug") != null && System.getProperty(
					"debug").equals("5"));

	public static final boolean VVERBOSE = VVVERBOSE
			|| (System.getProperty("vverbose") != null)
			|| (System.getProperty("debug") != null && System.getProperty(
					"debug").equals("4"));

	public static final boolean VERBOSE = VVERBOSE
			|| (System.getProperty("verbose") != null)
			|| (System.getProperty("debug") != null && System.getProperty(
					"debug").equals("3"));

	public static final boolean INFO = VERBOSE
			|| (System.getProperty("info") != null)
			|| (System.getProperty("debug") != null && System.getProperty(
					"debug").equals("2"));

	public static final boolean WARN = (System.getProperty("warn") != null)
			|| (System.getProperty("warnings") != null && System.getProperty(
					"warnings").equals("1"));

	public static final boolean ERROR = WARN
			|| (System.getProperty("error") != null)
			|| (System.getProperty("warnings") != null && System.getProperty(
					"warnings").equals("2"));

	public static final boolean FATAL = ERROR
			|| (System.getProperty("fatal") != null)
			|| (System.getProperty("warnings") != null && System.getProperty(
					"warnings").equals("3"));

	public static final boolean DAGCANCHK = (System.getProperty("dagcanchk") != null);

	public static PrintStream out = System.out;// TODO: configurable

	public static PrintStream err = System.err;// TODO: configurable

	// static{ if (QUIET) out.close(); }

	public final static StringBuilder dumpArray(final int[] arr,
			final int size, final StringBuilder out) {
		if (arr == null) {
			return out.append("null");
		}
		if (arr.length == 0) {
			return out.append("0:[]");
		}
		out.append(size).append(":[").append(arr[0]);
		for (int i = 1; i < size; ++i) {
			out.append(',').append(arr[i]);
		}
		return out.append(']');
	}

	public final static StringBuilder dumpArray(final int[] arr,
			final StringBuilder out) {
		if (arr == null) {
			return out.append("null");
		}
		return dumpArray(arr, arr.length, out);
	}

	public final static StringBuilder dumpArray(final Object[] arr,
			final int size, final StringBuilder out) {
		if (arr == null) {
			return out.append("null");
		}
		if (arr.length == 0) {
			return out.append("0:[]");
		}
		dumpObject(arr[0], out.append(size).append(":["));
		for (int i = 1; i < size; ++i) {
			dumpObject(arr[i], out.append(','));
		}

		return out.append("]");
	}

	public final static StringBuilder dumpArray(final Object[] arr,
			final StringBuilder out) {
		return dumpArray(arr, arr.length, out);
	}

	public final static StringBuilder dumpObject(final Object ack,
			final StringBuilder out) {
		if (ack instanceof Object[]) {
			return dumpArray((Object[]) ack, out);
		}
		if (ack instanceof int[]) {
			return dumpArray((int[]) ack, out);
		}
		return out.append(ack);
	}

	public final static String toString(final int[] array) {
		return dumpArray(array, array.length, new StringBuilder()).toString();
	}

	public final static String toString(final int[] array, final int size) {
		return dumpArray(array, size, new StringBuilder()).toString();
	}

	public final static <Type> String toString(final Type[] array) {
		return dumpArray(array, array.length, new StringBuilder()).toString();
	}

	public final static <Type> String toString(final Type[] array,
			final int size) {
		return dumpArray(array, size, new StringBuilder()).toString();
	}

}
