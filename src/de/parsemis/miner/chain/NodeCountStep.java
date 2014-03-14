/**
 * created May 25, 2006
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
package de.parsemis.miner.chain;

import java.util.Collection;


/**
 * This class implements the general pruning of fragments according to their
 * node count.
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
public class NodeCountStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	private final int min, max;

	/**
	 * creates a new node count pruning
	 * 
	 * @param next
	 * @param min
	 *            the minimal number of nodes a fragment shall have to be
	 *            reported
	 * @param max
	 *            the maximal number of nodes a fragment shall have to be
	 *            reported
	 */
	public NodeCountStep(final MiningStep<NodeType, EdgeType> next,
			final int min, final int max) {
		super(next);
		this.min = min;
		this.max = max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      java.util.Collection)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		final int size = node.toHPFragment().toHPGraph().getNodeCount();
		if (size < min) {
			node.store(false);
		}
		if (size > max) {
			node.store(false);
		} else {
			callNext(node, extensions);
		}
	}
}
