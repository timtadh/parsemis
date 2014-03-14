/**
 * created Jan 10, 2008
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPGraphComparator;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.Relabler;

/**
 * This class is a HashSet for HPGraphs. It uses explicit expensive graph
 * isomorphism test for comparing graphs with the same hash value.
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
public class GraphSet<NodeType, EdgeType> implements
		Collection<HPGraph<NodeType, EdgeType>>, Generic<NodeType, EdgeType> {

	public final Comparator<HPGraph<NodeType, EdgeType>> comparator;

	private final int averageBinSize;

	ArrayList<HPGraph<NodeType, EdgeType>>[] map;

	int entries;

	final private Relabler<NodeType, EdgeType> rel;

	/** creates a new GraphSet */
	@SuppressWarnings("unchecked")
	public GraphSet() {
		this(LocalEnvironment.environ);
	}

	@SuppressWarnings("unchecked")
	private GraphSet(final int initialSize,
			final Comparator<HPGraph<NodeType, EdgeType>> graphComparator,
			final int averageBinSize, final Relabler<NodeType, EdgeType> rel) {
		final int bins = initialSize / averageBinSize;
		int index = Arrays.binarySearch(Math.PRIMES, bins);
		if (index < 0) {
			index = -index;
		}
		if (index >= Math.PRIMES.length) {
			index = Math.PRIMES.length - 1;
		}

		this.map = new ArrayList[Math.PRIMES[index]];
		for (int i = 0; i < map.length; i++) {
			map[i] = new ArrayList<HPGraph<NodeType, EdgeType>>(
					(int) (averageBinSize * 1.25));
		}

		this.comparator = graphComparator;
		this.averageBinSize = averageBinSize;
		this.rel = rel;
	}

	/**
	 * creates a new GraphSet
	 * 
	 * @param rel
	 *            Relabler to use for hashing
	 */
	public GraphSet(final Relabler<NodeType, EdgeType> rel) {
		this(Math.PRIMES[16], new HPGraphComparator<NodeType, EdgeType>(rel),
				25, rel);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(final HPGraph<NodeType, EdgeType> o) {
		return addFirst(o) == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(
			final Collection<? extends HPGraph<NodeType, EdgeType>> c) {
		boolean ret = false;
		for (final HPGraph<NodeType, EdgeType> g : c) {
			ret |= add(g);
		}
		return ret;
	}

	// map reader
	public HPGraph<NodeType, EdgeType> addFirst(
			final HPGraph<NodeType, EdgeType> o) {
		if (entries > averageBinSize * map.length) {
			resize((int) java.lang.Math.ceil(map.length * 1.23578));
		}
		assert o != null;
		final int hashCode = getHash(o);
		final int bin = getBin(hashCode, map.length);

		final ArrayList<HPGraph<NodeType, EdgeType>> list = map[bin];
		synchronized (list) {
			for (final HPGraph<NodeType, EdgeType> g : list) {
				if (getHash(g) == hashCode && comparator.compare(g, o) == 0) {
					return g;
				}
			}
			list.add(o);
		}
		// entries writer
		entries++;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	// map writer
	public void clear() {
		for (int i = 0; i < map.length; i++) {
			map[i].clear();
		}
		entries = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	// map reader
	public boolean contains(final Object o) {
		if (o instanceof HPGraph) {
			@SuppressWarnings("unchecked")
			final HPGraph<NodeType, EdgeType> graph = (HPGraph<NodeType, EdgeType>) o;

			final int hashCode = getHash(graph);
			final int bin = getBin(hashCode, map.length);
			for (final HPGraph<NodeType, EdgeType> g : map[bin]) {
				if (getHash(g) == hashCode && comparator.compare(g, graph) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(final Collection<?> c) {
		boolean ret = true;
		for (final Object g : c) {
			ret &= contains(g);
		}
		return ret;
	}

	private final int getBin(final int hashCode, final int length) {
		return java.lang.Math.abs((hashCode ^ (hashCode >> 24)
				^ (hashCode >> 15) ^ (hashCode >> 9)))
				% length;
	}

	private final int getHash(final HPGraph<NodeType, EdgeType> g) {
		return GraphUtils.hashValue(g, rel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	// entries writer
	public boolean isEmpty() {
		return entries == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<HPGraph<NodeType, EdgeType>> iterator() {
		return new Iterator<HPGraph<NodeType, EdgeType>>() {
			Iterator<HPGraph<NodeType, EdgeType>> it = map[0].iterator();

			int idx = 1;

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#hasNext()
			 */
			public boolean hasNext() {
				while (idx < map.length && !it.hasNext()) {
					it = map[idx++].iterator();
				}
				return it.hasNext();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#next()
			 */
			public HPGraph<NodeType, EdgeType> next() {
				if (hasNext()) {
					return it.next();
				}
				throw new NoSuchElementException("No more elements");
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#remove()
			 */
			public void remove() {
				it.remove();
				entries--;
			}

		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	// map reader
	public boolean remove(final Object o) {
		boolean ret = false;
		if (o instanceof HPGraph) {
			@SuppressWarnings("unchecked")
			final HPGraph<NodeType, EdgeType> graph = (HPGraph<NodeType, EdgeType>) o;
			final int hashCode = getHash(graph);
			final int bin = getBin(hashCode, map.length);

			final ArrayList<HPGraph<NodeType, EdgeType>> list = map[bin];

			synchronized (list) {
				for (final Iterator<HPGraph<NodeType, EdgeType>> bit = list
						.iterator(); !ret && bit.hasNext();) {
					final HPGraph<NodeType, EdgeType> g = bit.next();
					if (getHash(g) == hashCode
							&& comparator.compare(g, graph) == 0) {
						bit.remove();
						ret = true;
					}
				}
			}
		}
		// entries writer
		if (ret)
			entries--;
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(final Collection<?> c) {
		boolean ret = false;
		for (final Object g : c) {
			ret |= remove(g);
		}
		return ret;
	}

	// map writer
	protected void resize(int newSize) {
		if (newSize <= 0) {
			newSize = 1;
		}
		@SuppressWarnings("unchecked")
		final ArrayList<HPGraph<NodeType, EdgeType>>[] temp = new ArrayList[newSize];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = new ArrayList<HPGraph<NodeType, EdgeType>>(
					(int) (averageBinSize * 1.25));
		}

		for (int i = 0; i < map.length; i++) {
			for (final HPGraph<NodeType, EdgeType> g : map[i]) {
				temp[getBin(getHash(g), newSize)].add(g);
			}
		}
		map = temp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	// map writer
	public boolean retainAll(final Collection<?> c) {
		boolean ret = false;
		for (final Iterator<HPGraph<NodeType, EdgeType>> git = iterator(); git
				.hasNext();) {
			if (!c.contains(git.next())) {
				git.remove();
				ret = true;
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	// entries writer
	public int size() {
		return entries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	// map writer
	public Object[] toArray() {
		return toArray(new Graph[entries]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	// map writer
	public <T> T[] toArray(final T[] a) {
		@SuppressWarnings("unchecked")
		final HPGraph<NodeType, EdgeType> f[] = (HPGraph<NodeType, EdgeType>[]) a;
		int i = 0;
		for (final HPGraph<NodeType, EdgeType> g : this) {
			f[i++] = g;
		}
		return a;
	}

}
