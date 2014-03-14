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

import de.parsemis.miner.chain.SearchLatticeNode;

/**
 * This interface encapsulate the required abilities of stacks to store search
 * nodes for the mining process.
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
public interface MiningStack<NodeType, EdgeType> {

	/**
	 * @return and removes the top of the stack
	 */
	public SearchLatticeNode<NodeType, EdgeType> pop();

	/**
	 * insert the given <code>object</code> on top of the stack
	 * 
	 * @param object
	 * @return the given object
	 */
	public SearchLatticeNode<NodeType, EdgeType> push(
			SearchLatticeNode<NodeType, EdgeType> object);

	/**
	 * @return the number of objects in the stack
	 */
	public int size();

	/**
	 * try to split the current stack to refill the given one
	 * 
	 * @param other
	 * @return <code>true</code>, if the stack is refilled
	 */
	public boolean split(MiningStack<NodeType, EdgeType> other);

}
