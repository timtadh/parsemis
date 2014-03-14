/**
 * created Nov 13, 2007
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
package de.parsemis.jp;

import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.strategy.MiningStack;

/**
 * This class wrapps the local stack into the JavaParty environment
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 * 
 * @resident
 */
public class RemoteWrapperStack<NodeType, EdgeType> extends
		RemoteStack<NodeType, EdgeType> {
	private final MiningStack<NodeType, EdgeType> stack;

	/**
	 * create new wrapper for the stack of the thread with the given index
	 * 
	 * @param idx
	 */
	public RemoteWrapperStack(final int idx) {
		this.stack = LocalEnvironment.env(this).stack[idx];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.MiningStack#pop()
	 */
	public synchronized SearchLatticeNode<NodeType, EdgeType> pop() {
		return stack.pop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.MiningStack#push(de.parsemis.miner.SearchLatticeNode)
	 */
	public synchronized SearchLatticeNode<NodeType, EdgeType> push(
			final SearchLatticeNode<NodeType, EdgeType> object) {
		return stack.push(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.MiningStack#size()
	 */
	public synchronized int size() {
		return stack.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.MiningStack#split(de.parsemis.strategy.MiningStack)
	 */
	public synchronized boolean split(
			final MiningStack<NodeType, EdgeType> other) {
		return stack.split(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return stack.toString();
	}

}
