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

import de.parsemis.graph.Edge;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.GenerationPartialStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.general.HPEmbedding;

/**
 * Extends the normal right most extension to be applied to unconnected fragments.
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
public class UnconnectedExtension<NodeType, EdgeType> extends
		RightMostExtension<NodeType, EdgeType> {

	/**
	 * creates a new extension that is specialized for the search of unconnected
	 * fragments
	 * 
	 * @param next
	 * @param tenv
	 */
	public UnconnectedExtension(
			final GenerationPartialStep<NodeType, EdgeType> next,
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		super(next, tenv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      de.parsemis.graph.Embedding)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final HPEmbedding<NodeType, EdgeType> embedding) {
		final DFSCode<NodeType, EdgeType> code = (DFSCode<NodeType, EdgeType>) node;
		final GSpanHPEmbedding<NodeType, EdgeType> emb = (GSpanHPEmbedding<NodeType, EdgeType>) embedding;
		if (code.getLast().isForward() && code.getLast().getNodeA() != 0) {
			// each node require a connection to the pseudo node (0) so after
			// inserting a forward edge, just this backedge has to be added
			final HPGraph<NodeType, EdgeType> sup = emb.getSuperGraph();
			final int lastNode = emb.getSubGraph().getNodeCount() - 1;
			final int nodeA = emb.getSuperGraphNode(lastNode);
			final int nodeB = emb.getSuperGraphNode(0);
			final int edge = sup.getEdge(nodeB, nodeA);
			final GSpanEdge<NodeType, EdgeType> gEdge = tenv.getEdge(lastNode,
					0, code.getLast().getLabelB(), 0, 0, Edge.INCOMING);
			if (code.getLast().compareTo(gEdge) < 0) {
				this.add(gEdge, emb, code, edge, nodeB);
			}
		} else {
			// elsewhere normal extension
			extend(code, emb);
		}
		callNext(node, embedding);
	}

}
