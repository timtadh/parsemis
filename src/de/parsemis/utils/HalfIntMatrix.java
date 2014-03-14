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
 * This class represents a lower (or upper) triangle matrix that stores ints.
 * 
 * @author Thorsten Meinl (Thorsten.Meinl@informatik.uni-erlangen.de)
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class HalfIntMatrix implements IntMatrix {
	/** the array holding the complete triangle matrix */
	private final int[] matrix;

	private final int size, initialValue;

	/**
	 * Creates a new HalfIntMatrix that is an exact copy of the given template
	 * 
	 * @param template
	 *            a HalfIntMatrix that should be copied
	 */
	public HalfIntMatrix(final HalfIntMatrix template) {
		this(template, 0);
	}

	/**
	 * Creates a new HalfIntMatrix that is an exact copy of the given template
	 * 
	 * @param template
	 *            a HalfIntMatrix that should be copied
	 * @param reserveNewNodes
	 *            the number of new nodes for which space should be reserved or
	 *            removed
	 */
	public HalfIntMatrix(final HalfIntMatrix template, final int reserveNewNodes) {
		this.size = (template.size + reserveNewNodes);
		this.initialValue = template.initialValue;
		this.matrix = new int[((size * size + size) / 2)];
		System.arraycopy(template.matrix, 0, matrix, 0,
				reserveNewNodes >= 0 ? template.matrix.length : matrix.length);
		for (int i = template.matrix.length; i < matrix.length; i++) {
			matrix[i] = initialValue;
		}
	}

	/**
	 * Creates a new HalfIntMatrix with the given size and initial values
	 * 
	 * @param initialSize
	 *            the size of the matrix in rows (or columns)
	 * @param initialValue
	 *            the initial value of each matrix element
	 */
	public HalfIntMatrix(final int initialSize, final int initialValue) {
		this.size = initialSize;
		this.initialValue = initialValue;
		this.matrix = new int[((size * size + size) / 2)];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = initialValue;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntMatrix#exchangeRows(int, int)
	 */
	public void exchangeRows(int rowA, int rowB) {
		if (rowA == rowB) {
			return;
		}
		if (rowA > rowB) {
			final int t = rowA;
			rowA = rowB;
			rowB = t;
		}
		int i = 0;
		for (; i < rowA; i++) {
			swap(rowA, i, rowB, i);
		}
		for (i++; i < rowB; i++) {
			swap(rowA, i, rowB, i);
		}
		for (i++; i < size; i++) {
			swap(rowA, i, rowB, i);
		}
		swap(rowA, rowA, rowB, rowB);
		swap(rowA, rowB, rowB, rowA);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntMatrix#get(int, int)
	 */
	public int get(final int row, final int col) {
		assert row >= 0 && col >= 0 && row < size && col < size : "row/col out of bounds: "
				+ row + "/" + col + " size: " + size;
		return matrix[idx(row, col)];
	}

	private final int idx(final int row, final int col) {
		if (row < col) {
			return col * (col + 1) / 2 + row;
		} else {
			return row * (row + 1) / 2 + col;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntMatrix#set(int, int, int)
	 */
	public void set(final int row, final int col, final int value) {
		assert row >= 0 && col >= 0 && row < size && col < size : "row/col out of bounds: "
				+ row + "/" + col + " size: " + size;
		matrix[idx(row, col)] = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.IntMatrix#size()
	 */
	public int size() {
		return size;
	}

	private final void swap(final int r1, final int c1, final int r2,
			final int c2) {
		Permutations.swap(matrix, idx(r1, c1), idx(r2, c2));
	}

}
