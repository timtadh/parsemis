/**
 * created 23.01.2008
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2008 Marc Woerlein
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
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public interface IntMatrix {

	/**
	 * Exchanges the two rows (and the corresponding columns) in the matrix.
	 * 
	 * @param rowA
	 *            the first row
	 * @param rowB
	 *            the second row
	 */
	public void exchangeRows(int rowA, int rowB);

	/**
	 * Returns the value at the given position.
	 * 
	 * @param row
	 *            the row of the entry
	 * @param col
	 *            the column of the entry
	 * @return the value at the given position in the matrix.
	 */
	public int get(int row, int col);

	/**
	 * Sets the value at the given position.
	 * 
	 * @param row
	 *            the row on the entry
	 * @param col
	 *            the column of the entry
	 * @param value
	 *            the new value
	 */
	public void set(int row, int col, int value);

	/**
	 * @return the size of the matrix in rows (or columns)
	 */
	public int size();

}
