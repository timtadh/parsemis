/**
 * created May 22, 2006
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

import de.parsemis.utils.Generic;

/**
 * This class represents a thread local array pool
 * 
 * (pooling not yet implemented)
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class ThreadEnvironment<NodeType, EdgeType> implements
		Generic<NodeType, EdgeType> {

	/**
	 * This functions returns an exact copy of the given array.
	 * 
	 * @param array
	 * @return a copy of the given array
	 */
	public int[] copy(final int[] array) {
		final int length = array.length;
		final int[] ret = getIntArray(length);
		for (int i = 0; i < length; ++i) {
			ret[i] = array[i];
		}
		return ret;
	}

	/**
	 * This function returns an uninitialized int array. The fields may contain
	 * anny possible integer value.
	 * 
	 * @param length
	 * @return an int array with the given length
	 */
	public int[] getIntArray(final int length) {
		return new int[length];
	}

	/**
	 * This function returns an int array each field is filled with
	 * <code>def</code>.
	 * 
	 * @param length
	 * @param def
	 * @return an int array with the given length
	 */
	public int[] getIntArray(final int length, final int def) {
		final int[] ret = getIntArray(length);
		for (int i = 0; i < length; ++i) {
			ret[i] = def;
		}
		return ret;
	}

	/**
	 * gives the array under control of the pool
	 * 
	 * @param array
	 */
	public void push(final int[] array) {
	}

}
