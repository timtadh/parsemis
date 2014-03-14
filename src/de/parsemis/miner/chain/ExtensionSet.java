/**
 * Created Jan 06, 2008
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
package de.parsemis.miner.chain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.parsemis.miner.general.Frequency;
import de.parsemis.utils.FrequentedComparable;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 * @param <StoreType>
 *            the type that should be stored as extension and siblings
 */
public class ExtensionSet<NodeType, EdgeType, StoreType extends FrequentedComparable<StoreType>>
		implements Collection<Extension<NodeType, EdgeType>> {

	public static class Ext<NodeType, EdgeType, StoreType extends FrequentedComparable<StoreType>>
			implements Extension<NodeType, EdgeType> {

		final int pos;

		final ExtensionSet<NodeType, EdgeType, StoreType> set;

		Ext(final int pos, final ExtensionSet<NodeType, EdgeType, StoreType> set) {
			this.pos = pos;
			this.set = set;
		}

		public int compareTo(final Extension<NodeType, EdgeType> o) {
			@SuppressWarnings("unchecked")
			final Ext<NodeType, EdgeType, StoreType> other = (Ext<NodeType, EdgeType, StoreType>) o;
			return getVal().compareTo(other.getVal());
		}

		public Frequency frequency() {
			return getVal().frequency();
		}

		public Collection<StoreType> getSiblings() {
			return set.siblings;
		}

		public StoreType getVal() {
			return set.siblings.get(pos);
		}

	}

	ArrayList<StoreType> siblings = new ArrayList<StoreType>();

	public boolean add(final Extension<NodeType, EdgeType> o) {
		throw new UnsupportedOperationException(
				"not available for ExtensionSets");
	}

	public boolean add(final StoreType l) {
		return siblings.add(l);
	}

	public boolean addAll(
			final Collection<? extends Extension<NodeType, EdgeType>> c) {
		throw new UnsupportedOperationException(
				"not available for ExtensionSets");
	}

	public void clear() {
		siblings.clear();
	}

	public boolean contains(final Object o) {
		throw new UnsupportedOperationException(
				"not available for ExtensionSets");
	}

	public boolean containsAll(final Collection<?> c) {
		throw new UnsupportedOperationException(
				"not available for ExtensionSets");
	}

	public boolean isEmpty() {
		return siblings.isEmpty();
	}

	public Iterator<Extension<NodeType, EdgeType>> iterator() {
		return new Iterator<Extension<NodeType, EdgeType>>() {
			int i = 0;

			public boolean hasNext() {
				return i < siblings.size();
			}

			public Extension<NodeType, EdgeType> next() {
				if (hasNext()) {
					return new Ext<NodeType, EdgeType, StoreType>(i++,
							ExtensionSet.this);
				}
				throw new NoSuchElementException();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	public boolean remove(final Object o) {
		throw new UnsupportedOperationException(
				"not available for ExtensionSets");
	}

	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException(
				"not available for ExtensionSets");
	}

	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException(
				"not available for ExtensionSets");
	}

	public int size() {
		return siblings.size();
	}

	public void sort(final Comparator<StoreType> comp) {
		@SuppressWarnings("unchecked")
		final StoreType field[] = siblings
				.toArray((StoreType[]) new FrequentedComparable[siblings.size()]);
		Arrays.sort(field, comp);
		siblings.clear();
		for (final StoreType l : field) {
			siblings.add(l);
		}
	}

	public Object[] toArray() {
		return toArray(new Extension[size()]);
	}

	public <T> T[] toArray(final T[] a) {
		@SuppressWarnings("unchecked")
		final Extension<NodeType, EdgeType>[] arr = (Extension<NodeType, EdgeType>[]) a;
		for (int pos = 0; pos < size(); pos++) {
			arr[pos] = new Ext<NodeType, EdgeType, StoreType>(pos,
					ExtensionSet.this);
		}
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return siblings.toString();
	}
}
