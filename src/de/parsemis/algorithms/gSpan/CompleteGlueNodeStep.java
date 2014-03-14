/**
 * created Jun 21, 2006
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
package de.parsemis.algorithms.gSpan;

import java.util.Collection;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.SearchLatticeNode;

/**
 * Filters all duplicates resulting from different gluenode
 * connections during unconnected search.
 * <p>
 * Just graphs with a fully connected gluenode will be stored.
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
public class CompleteGlueNodeStep<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	/**
	 * creates the filtering of glue duplicates
	 * 
	 * @param next
	 */
	public CompleteGlueNodeStep(final MiningStep<NodeType, EdgeType> next) {
		super(next);
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

		final DFSCode<NodeType, EdgeType> code = (DFSCode<NodeType, EdgeType>) node;
		final HPGraph<NodeType, EdgeType> hp = code.toHPFragment().toHPGraph();

		// gluenode (idx 0) has to have connected to every other node in the
		// graph to be unique
		if (hp.getDegree(0) < hp.getNodeCount() - 1) {
			node.store(false);
		}

		// TODO: remove debug
		// emulate strucma with inactivated newEdgeExtension
		// for (int i=1;i<hp.getMaxNodeIndex();i++){
		// if (hp.isValidNode(i) && hp.getInDegree(i)>2) node.store(false);
		// }

		callNext(node, extensions);
	}

}
