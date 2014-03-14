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
import static de.parsemis.miner.environment.Debug.VERBOSE;
import static de.parsemis.miner.environment.Debug.VVERBOSE;
import static de.parsemis.miner.environment.Debug.err;
import static de.parsemis.miner.environment.Debug.out;

import java.util.Collection;
import java.util.Iterator;

import de.parsemis.miner.chain.Extender;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.Fragment;

/**
 * This class implements the depth-first working for all threads.
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
public class Worker<NodeType, EdgeType> implements Runnable {

	private final MiningStack<NodeType, EdgeType> stack;

	private final Collection<Fragment<NodeType, EdgeType>> found;

	private final Extender<NodeType, EdgeType> searcher;

	private final int idx;

	/**
	 * creates a new Worker
	 * 
	 * @param stack
	 *            the stack for storing and getting unextended fragments
	 * @param found
	 *            the set to store frequent fragments
	 * @param searcher
	 *            the Searcher which should be used to extend fragments
	 * @param idx
	 *            the index of the given worker
	 */
	public Worker(final MiningStack<NodeType, EdgeType> stack,
			final Collection<Fragment<NodeType, EdgeType>> found,
			final Extender<NodeType, EdgeType> searcher, final int idx) {
		this.stack = stack;
		this.idx = idx;
		this.found = found;
		this.searcher = searcher;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (VERBOSE) {
			out.println(this + " is up and running ("+Thread.currentThread()+")");
			err.println(this + " is up and running (stderr)("+Thread.currentThread()+")");
		}
		int count = 0;
		SearchLatticeNode<NodeType, EdgeType> node = stack.pop();

		// while work is available, extend it
		while (node != null) {
			if ((VERBOSE && node.getLevel() == 0) || VVERBOSE) {
				out.println(this + " Stack size " + stack.size() + " works "
						+ node);
			}
			// extends current node
			final Iterator<SearchLatticeNode<NodeType, EdgeType>> children = searcher
					.getChildren(node).iterator();

			// get next node
			final SearchLatticeNode<NodeType, EdgeType> next = (children
					.hasNext() ? children.next() : stack.pop());

			// push other found ones
			while (children.hasNext()) {
				stack.push(children.next());
			}

			if (node.store()) {
				found.add(node.toFragment());
			} else {
				node.release();
			}
			node.finalizeIt();
			node = next;
			++count;

		}
		if (INFO) {
			out.println(this + " finished and has worked " + count
					+ " fragments.");
		}
		if (VERBOSE) {
			out.println(this + " "
					+ LocalEnvironment.env(searcher).getThreadEnv(idx));
		}
	}

	@Override
	public String toString() {
		return "Worker-" + this.idx;
	}

}
