/**
 * created Oct 30, 2006
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
package de.parsemis.utils;

import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * This class as an iterator over all set/cleared indices of the given bitset
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class BitSetIterator implements IntIterator {

	private final static int DONE = -1;

	private final BitSet bitset;

	private final boolean clear;

	private int next, last = -1;

	/**
	 * new iterator over the set bits in the given bitsets
	 * 
	 * @param bitset
	 */
	public BitSetIterator(final BitSet bitset) {
		this(bitset, true);
	}

	/**
	 * new iterator over the set/unset bits in the given bitsets
	 * 
	 * @param bitset
	 * @param set
	 */
	public BitSetIterator(final BitSet bitset, final boolean set) {
		this.bitset = bitset;
		this.clear = !set;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntIterator#hasNext()
	 */
	public boolean hasNext() {
		if (bitset == null) {
			return false;
		}
		if (next == DONE) {
			next = (clear ? bitset.nextClearBit(last + 1) : bitset
					.nextSetBit(last + 1));
		}
		return next > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntIterator#next()
	 */
	public int next() {
		if (hasNext()) {
			last = next;
			next = DONE;
			return last;
		}
		throw new NoSuchElementException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntIterator#remove()
	 */
	public void remove() {
		bitset.set(last, clear);
	}

}
