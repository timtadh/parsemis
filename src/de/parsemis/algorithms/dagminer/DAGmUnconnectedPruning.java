/**
 * created 31.07.2006
 *
 * @by Tobias Werth (sitowert@i2.informatik.uni-erlangen.de)
 *
 * Copyright 2006 Tobias Werth
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.algorithms.dagminer;

import java.util.Collection;

import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.general.HPEmbedding;

/**
 * @author Tobias Werth (sitowert@i2.informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class DAGmUnconnectedPruning<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	public DAGmUnconnectedPruning(final MiningStep<NodeType, EdgeType> next) {
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
		final DAGmFragment<NodeType, EdgeType> actFragment = (DAGmFragment<NodeType, EdgeType>) node
				.toHPFragment();
		if (!actFragment.isConnected()) {
			if (false && (actFragment.getLastAction() == DAGmFragment.LastAction.INSERTED_NODE)) {
				for (final HPEmbedding<NodeType, EdgeType> embed : actFragment) {
					final DAGmHPEmbedding<NodeType, EdgeType> actEmbed = (DAGmHPEmbedding<NodeType, EdgeType>) embed;
					if (actEmbed.isNeverConnectable(actFragment)) {
						actFragment.remove(embed);
					}
				}
			}
			node.store(false);
		}
		callNext(node, extensions);
	}

}
