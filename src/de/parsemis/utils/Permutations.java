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
 * generate ordered permutations of an integer array
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
public class Permutations {

	/**
	 * @param n
	 * @return an initial ordered permutation of n elements
	 */
	public static int[] initialPermutation(int n) {
		final int mapping[] = new int[n--];
		while (n >= 0) {
			mapping[n] = n--;
		}
		return mapping;
	}

	/**
	 * generate the next order permutation
	 * 
	 * @param mapping
	 * @return <code>true</code>, if a new permutation is generated
	 */
	public static boolean nextPermutation(final int mapping[]) {
		for (int i = mapping.length - 2, j; i >= 0; i--) {
			if (mapping[i + 1] > mapping[i]) {
				for (j = mapping.length - 1; mapping[j] <= mapping[i]; j--) {
					; // skip smaller nodes
				}

				swap(mapping, i, j);
				for (j = 1; j <= (mapping.length - i) / 2; j++) {
					swap(mapping, i + j, mapping.length - j);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Sorts parts of the given array with the quicksort algorithm using the
	 * given comparator.
	 * 
	 * @param field
	 *            the field to be sorted
	 * @param left
	 *            the left start index in the field (inclusive)
	 * @param right
	 *            the right end index in the field (inclusive)
	 * @param comp
	 *            a comparator
	 */
	public static void quickSort(final int[] field, final int left,
			final int right, final IntComparator comp) {
		if (right - left == 2) {
			if (comp.compare(field[left], field[left + 1]) > 0) {
				swap(field, left, left + 1);
			}

			if (comp.compare(field[left + 1], field[right]) > 0) {
				swap(field, right, left + 1);
			}

			if (comp.compare(field[left], field[left + 1]) > 0) {
				swap(field, left, left + 1);
			}
		} else if (right - left == 1) {
			if (comp.compare(field[left], field[right]) > 0) {
				swap(field, left, right);
			}
		} else {
			int l = left, r = right;
			int pivot = (right - left) / 2 + left;

			while (l < r) {
				while ((pivot < r)
						&& (comp.compare(field[pivot], field[r]) <= 0)) {
					r--;
				}
				if (pivot < r) {
					swap(field, pivot, r);
					pivot = r;
				}

				while ((l < pivot)
						&& (comp.compare(field[l], field[pivot]) <= 0)) {
					l++;
				}
				if (l < pivot) {
					swap(field, pivot, l);
					pivot = l;
				}
			}

			if (l - 1 - left > 0) {
				quickSort(field, left, l - 1, comp);
			}
			if (right - (r + 1) > 0) {
				quickSort(field, r + 1, right, comp);
			}
		}
	}

	/**
	 * Sorts the given <code>int[]</code> -field with quicksort using the
	 * given comparator.
	 * 
	 * @param field
	 *            the field to be sorted
	 * @param comp
	 *            a comparator
	 * @return the sorted field (the same as <code>field</code>)
	 */
	public static int[] quickSort(final int[] field, final IntComparator comp) {
		quickSort(field, 0, field.length - 1, comp);
		return field;
	}

	/**
	 * swaps the values of the array at position i and j
	 * 
	 * @param array
	 * @param i
	 * @param j
	 */
	public static void swap(final int array[], final int i, final int j) {
		final int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}
}
