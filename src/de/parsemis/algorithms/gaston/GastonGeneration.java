/**
 * Created Jan 05, 2008
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
package de.parsemis.algorithms.gaston;

import java.util.Collection;

import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.GenerationStep;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.SearchLatticeNode;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class GastonGeneration<NodeType, EdgeType> extends
		GenerationStep<NodeType, EdgeType> {

	/**
	 * creates a new GenerationStep
	 * 
	 * @param next
	 *            the next step of the mining chain
	 */
	public GastonGeneration(final MiningStep<NodeType, EdgeType> next) {
		super(next);
		setFirst(getLast());
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
		final GastonNode<NodeType, EdgeType> n = (GastonNode<NodeType, EdgeType>) node;
		// use extension of the Gaston node
		final Collection<Extension<NodeType, EdgeType>> col = n.getExtensions();
		super.call(node, col);
	}

}
