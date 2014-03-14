/**
 * created Dec 12, 2007
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2007 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.utils;

/**
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public final class ThreeTuple implements Comparable<ThreeTuple> {

	final int value1;

	final int value2;

	final int value3;

	public ThreeTuple(final int value1, final int value2, final int value3) {
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final ThreeTuple o) {
		if (value1 != o.value1) {
			return o.value1 - value1;
		}
		if (value2 != o.value2) {
			return o.value2 - value2;
		}
		return o.value3 - value3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {
		return ((o instanceof ThreeTuple) && ((ThreeTuple) o).value1 == value1
				&& ((ThreeTuple) o).value2 == value2 && ((ThreeTuple) o).value3 == value3);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return value1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + value1 + "," + value2 + "," + value3 + ")";
	}

}