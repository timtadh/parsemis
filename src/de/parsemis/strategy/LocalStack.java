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

import static de.parsemis.miner.environment.Debug.ERROR;
import static de.parsemis.miner.environment.Debug.VVERBOSE;
import static de.parsemis.miner.environment.Debug.err;
import static de.parsemis.miner.environment.Debug.out;
import de.parsemis.jp.RemoteWrapperStack;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.utils.Generic;

/**
 * This class implements the local part of the dynamic work stealing stack
 * system
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
public class LocalStack<NodeType, EdgeType> extends
		ListItem<LocalStack<NodeType, EdgeType>> implements
		MiningStack<NodeType, EdgeType>, Generic<NodeType, EdgeType> {

	private final StackList<NodeType, EdgeType> available;

	private final int SPLITTSIZE;

	private final int MAXDEPTH;

	private final int MAXCOUNT;

	private final int idx;

	private final MiningStack<NodeType, EdgeType> master;

	private SearchLatticeNode<NodeType, EdgeType>[] pool;

	private int pos;

	/**
	 * creates a new local stack
	 * 
	 * @param idx
	 * @param avail
	 * @param env
	 */
	@SuppressWarnings("unchecked")
	public LocalStack(final int idx, final StackList<NodeType, EdgeType> avail,
			final LocalEnvironment<NodeType, EdgeType> env) {
		super.elem = this;
		this.available = avail;
		this.idx = idx;

		this.SPLITTSIZE = env.splitSize;
		this.MAXDEPTH = env.maxSplitDepth;
		this.MAXCOUNT = env.maxSplitCount;

		env.stack[idx] = this;
		this.master = (env.javaparty ? /** @at idx */
		new RemoteWrapperStack(idx) : (MiningStack) this);
		available.add(master);

		pool = new SearchLatticeNode[10];
		pos = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.MiningStack#pop()
	 */
	public SearchLatticeNode<NodeType, EdgeType> pop() {
		if (size() == 0) {
			final long before = System.currentTimeMillis();
			if (VVERBOSE) {
				out.println(this + " needs split. Available stacks "
						+ available);
			}
			available.remove(master);
			while (available.size() > 0) {
				synchronized (available) {
					if (available.split(master)) {
						available.add(master);
						synchronized (this) {
							assert pool[pos - 1].getThreadNumber() == idx : "wrong Node in LocalStack";
							LocalEnvironment.env(this).stats.syncTime[2] += System
									.currentTimeMillis()
									- before;
							return pool[--pos];
						}
					}
				}
				if (VVERBOSE) {
					out.println(this + " split failed. retry");
				}
				try {
					Thread.sleep(500);
				} catch (final InterruptedException ie) {
					if (ERROR) {
						err.print("Error: " + ie);
					}
				}
			}
			synchronized (available) {
				LocalEnvironment.env(this).stats.syncTime[2] += System
						.currentTimeMillis()
						- before;
			}
			return null;
		}
		synchronized (this) {
			assert pool[pos - 1].getThreadNumber() == idx : "wrong Node in LocalStack";
			return pool[--pos];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.MiningStack#push(de.parsemis.miner.SearchLatticeNode)
	 */
	public synchronized SearchLatticeNode<NodeType, EdgeType> push(
			final SearchLatticeNode<NodeType, EdgeType> object) {
		if (pos == pool.length) {
			@SuppressWarnings("unchecked")
			final SearchLatticeNode<NodeType, EdgeType>[] tmp = new SearchLatticeNode[(int) (1.5 * pos)];
			System.arraycopy(pool, 0, tmp, 0, pos);
			pool = tmp;
		}
		object.setThreadNumber(idx);
		pool[pos++] = object;
		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.MiningStack#size()
	 */
	public int size() {
		synchronized (this) {
			return pos;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.MiningStack#split(de.parsemis.strategy.MiningStack)
	 */
	public boolean split(final MiningStack<NodeType, EdgeType> other) {
		synchronized (this) {
			if (size() < SPLITTSIZE
					|| (pool[SPLITTSIZE - 2]).getLevel() > MAXDEPTH) {
				return false;
			}
			int j = 0, i = 0;
			final int max = (MAXCOUNT == Integer.MAX_VALUE || pos - 1 < 2 * MAXCOUNT) ? pos - 1
					: 2 * MAXCOUNT;
			while (i < max) {
				final SearchLatticeNode<NodeType, EdgeType> next = pool[i++];
				if (next == null || next.getLevel() > MAXDEPTH) {
					break;
				}
				other.push(next);
				pool[j++] = pool[i++];
			}
			while (i < pos) {
				pool[j++] = pool[i++];
			}
			while (pos > j) {
				pool[--pos] = null;
			}
			if (VVERBOSE) {
				out.println(other + " gets splitt: size=" + other.size()
						+ " this.size=" + this.size() + " (" + this + ")");
			}
			return true;
		}
	}

}
