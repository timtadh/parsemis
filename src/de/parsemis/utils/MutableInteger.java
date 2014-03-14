/**
 * created Aug 13, 2004
 * 
 * @by Thorsten Meinl (Thorsten.Meinl@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Thorsten Meinl
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
 * This class is the same as Integer but the stored value can be changed.
 * 
 * @author Thorsten Meinl (Thorsten.Meinl@informatik.uni-erlangen.de)
 * 
 */
public class MutableInteger extends Number implements
		Comparable<MutableInteger>, SynchronizedCounter {
	private static final long serialVersionUID = 532556164094282571L;

	private int m_value;

	/**
	 * Creates a new MutableInteger with the given initial value.
	 * 
	 * @param value
	 *            the value
	 */
	public MutableInteger(final int value) {
		m_value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final MutableInteger o) {
		return m_value - o.m_value;
	}

	/**
	 * Decrements the value by one.
	 * 
	 * @return the new value
	 */
	public int dec() {
		return --m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#doubleValue()
	 */
	@Override
	public double doubleValue() {
		return m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof MutableInteger)) {
			return false;
		}
		return m_value == ((MutableInteger) obj).m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#floatValue()
	 */
	@Override
	public float floatValue() {
		return m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return m_value;
	}

	/**
	 * Increments the value by one.
	 * 
	 * @return the new value
	 */
	public int inc() {
		return ++m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#intValue()
	 */
	@Override
	public int intValue() {
		return m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Number#longValue()
	 */
	@Override
	public long longValue() {
		return m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Counter#next()
	 */
	public synchronized int next() {
		return m_value++;
	}

	/**
	 * Sets the value to a new value
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(final int value) {
		m_value = value;
	}
}
