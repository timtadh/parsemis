/**
 * Created on Jun 26, 2006
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
package de.parsemis.strategy;

import static de.parsemis.miner.environment.Debug.INFO;
import static de.parsemis.miner.environment.Debug.WARN;
import static de.parsemis.miner.environment.Debug.err;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import de.parsemis.algorithms.Algorithm;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.Statistics;
import de.parsemis.miner.general.Fragment;
import de.parsemis.utils.FileSerializeCollection;

/**
 * This class represents the threaded DFS search
 * 
 * It is also the head-element of the double linked MiningStack (ring) list and
 * therefore responsible for the work distribution
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
public class ThreadedDFSStrategy<NodeType, EdgeType> extends
		ListItem<MiningStack<NodeType, EdgeType>> implements
		Strategy<NodeType, EdgeType>, StackList<NodeType, EdgeType> {

	private final Collection<Fragment<NodeType, EdgeType>>[] answers;

	private final Thread[] threads;

	private final Statistics stats;

	private int size = 0;

	/**
	 * creates a new ThreadedDFSStrategy
	 * 
	 * @param threadcount
	 *            the number of threads that shall be initiated
	 * @param stats
	 *            object to store statistical informations for all threads
	 */
	@SuppressWarnings("unchecked")
	public ThreadedDFSStrategy(final int threadcount, final Statistics stats) {
		next = prev = this;
		this.answers = new Collection[threadcount];
		this.threads = new Thread[threadcount];
		this.stats = stats;
	}

	private boolean _add(final ListItem<MiningStack<NodeType, EdgeType>> item) {
		if (item.next != null || item.prev != null) {
			return false;
		}
		item.prev = this.prev;
		item.next = this;
		this.prev.next = item;
		this.prev = item;
		size++;
		return true;
	}

	boolean _remove(final ListItem<MiningStack<NodeType, EdgeType>> item) {
		if (item.next == null || item.prev == null) {
			return false;
		}
		item.prev.next = item.next;
		item.next.prev = item.prev;
		item.prev = item.next = null;
		size--;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(E)
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean add(final MiningStack<NodeType, EdgeType> arg0) {
		return _add((ListItem<MiningStack<NodeType, EdgeType>>) arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public synchronized boolean addAll(
			final Collection<? extends MiningStack<NodeType, EdgeType>> arg0) {
		for (final MiningStack<NodeType, EdgeType> ack : arg0) {
			if (!add(ack)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public synchronized void clear() {
		while (next != this) {
			_remove(next);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean contains(final Object arg0) {
		final MiningStack<NodeType, EdgeType> cur = (MiningStack<NodeType, EdgeType>) arg0;
		for (ListItem<MiningStack<NodeType, EdgeType>> ack = this.next; ack != this; ack = ack.next) {
			if (ack.elem == cur) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean containsAll(final Collection<?> arg0) {
		final Collection<MiningStack<NodeType, EdgeType>> col = (Collection<MiningStack<NodeType, EdgeType>>) arg0;
		for (final MiningStack<NodeType, EdgeType> ack : col) {
			if (!contains(ack)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	public synchronized boolean isEmpty() {
		return size == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	public synchronized Iterator<MiningStack<NodeType, EdgeType>> iterator() {
		return new Iterator<MiningStack<NodeType, EdgeType>>() {
			ListItem<MiningStack<NodeType, EdgeType>> ack = next;

			public boolean hasNext() {
				return ack.next.elem != null;
			}

			public MiningStack<NodeType, EdgeType> next() {
				final MiningStack<NodeType, EdgeType> ret = ack.elem;
				ack = ack.next;
				return ret;
			}

			public void remove() {
				_remove(ack.prev);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean remove(final Object arg0) {
		return _remove((ListItem<MiningStack<NodeType, EdgeType>>) arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean removeAll(final Collection<?> arg0) {
		final Collection<MiningStack<NodeType, EdgeType>> col = (Collection<MiningStack<NodeType, EdgeType>>) arg0;
		for (final MiningStack<NodeType, EdgeType> ack : col) {
			if (!remove(ack)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public synchronized boolean retainAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException(
				"retainAll is not suported for a StackList");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.Strategy#search(de.parsemis.miner.Algorithm,
	 *      int)
	 */
	public Collection<Fragment<NodeType, EdgeType>> search(
			final Algorithm<NodeType, EdgeType> algo) {
		final String filename = LocalEnvironment.env(this).objectFileName;

		final MiningStack<NodeType, EdgeType> mine = new LocalStack<NodeType, EdgeType>(
				0, this, LocalEnvironment.env(this));
		for (final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo
				.initialNodes(); it.hasNext();) {
			mine.push(it.next());
		}

		// initialize worker for the current thread
		answers[0] = (filename != null ? new FileSerializeCollection<Fragment<NodeType, EdgeType>>(
				filename)
				: new ArrayList<Fragment<NodeType, EdgeType>>());
		final Worker<NodeType, EdgeType> me = new Worker<NodeType, EdgeType>(
				mine, answers[0], algo.getExtender(0), 0);

		// initialize worker for further threads
		for (int i = 1; i < answers.length; ++i) {
			answers[i] = (filename != null ? new FileSerializeCollection<Fragment<NodeType, EdgeType>>(
					filename + "_" + i)
					: new HashSet<Fragment<NodeType, EdgeType>>());
			threads[i] = new SMPThread<NodeType, EdgeType>(i, algo, this,
					answers[i]);
		}

		// start other workers
		if (INFO) {
			stats.distributedTime -= System.currentTimeMillis();
		}
		for (int i = 1; i < answers.length; ++i) {
			threads[i].start();
		}

		// start local worker
		me.run();

		// wait for others
		for (int i = 1; i < answers.length; ++i) {
			try {
				threads[i].join();
			} catch (final InterruptedException ie) {
				if (WARN) {
					err.println(ie);
				}
			}
		}
		if (INFO) {
			stats.distributedTime += System.currentTimeMillis();
		}

		// collect data
		for (int i = 1; i < answers.length; ++i) {
			answers[0].addAll(answers[i]);
		}

		return answers[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	public synchronized int size() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.general.StackList#split(de.parsemis.general.MiningStack)
	 */
	public synchronized boolean split(
			final MiningStack<NodeType, EdgeType> empty) {
		if (INFO) {
			stats.splitTime -= System.currentTimeMillis();
		}
		for (ListItem<MiningStack<NodeType, EdgeType>> ack = this.next; ack != this; ack = ack.next) {
			if (ack.elem.split(empty)) {
				_remove(ack);
				_add(ack);
				if (INFO) {
					stats.splitTime += System.currentTimeMillis();
				}
				return true;
			}
		}
		if (INFO) {
			stats.splitTime += System.currentTimeMillis();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	public synchronized Object[] toArray() {
		return toArray(new MiningStack[size]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T> T[] toArray(final T[] arg0) {
		final MiningStack<NodeType, EdgeType>[] arr = (MiningStack<NodeType, EdgeType>[]) arg0;
		int i = -1;
		for (ListItem<MiningStack<NodeType, EdgeType>> ack = this.next; ack != this; ack = ack.next) {
			arr[++i] = ack.elem;
		}
		return (T[]) arr;
	}

}
