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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.Edge;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.DefaultFlatHPEmbedding;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.utils.IntIterator;

/**
 * This class contains the whole chain for possible children generation
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 * @param <DB>
 *            the corresponding DataBaseGraph type
 */
public class EmbeddingSearchGenerationStep<NodeType, EdgeType, DB extends DataBaseGraph<NodeType, EdgeType>>
		extends GenerationStep<NodeType, EdgeType> {
	private final Collection<HPEmbedding<NodeType, EdgeType>> embeddings;

	private final LocalEnvironment<NodeType, EdgeType> env;

	private final ThreadEnvironment<NodeType, EdgeType> tenv;

	/**
	 * creates a new embeddings searching GSpanGeneration
	 * 
	 * @param next
	 * @param tenv
	 */
	public EmbeddingSearchGenerationStep(
			final MiningStep<NodeType, EdgeType> next,
			final ThreadEnvironment<NodeType, EdgeType> tenv) {
		super(next);
		this.env = LocalEnvironment.env(this);
		this.tenv = tenv;
		this.embeddings = new ArrayList<HPEmbedding<NodeType, EdgeType>>();
		// ArrayList for deterministic order
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		reset();
		final int[] ackNodes = tenv.getIntArray(node.toHPFragment().toHPGraph()
				.getNodeCount(), HPGraph.NO_NODE);
		// search for children in all required database graphs, therefore search
		// all embeddings
		for (final Iterator<DataBaseGraph<NodeType, EdgeType>> ggit = node
				.toHPFragment().graphIterator(); ggit.hasNext();) {
			searchEmbeddings((DB) ggit.next(), node, ackNodes);
		}
		tenv.push(ackNodes);
		super.call(node, extensions);
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
		embeddings.add(embedding);
		super.call(node, embedding);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
	 */
	@Override
	public final void reset() {
		// delete old Embeddings;
		// if (!env.storeEmbeddings)
		// for (HPEmbedding<NodeType, EdgeType> emb : embeddings)
		// ((GSpanHPEmbedding<NodeType, EdgeType>) emb).release(tenv);
		embeddings.clear();
		super.reset();
	}

	private final void searchEmbeddings(final DB dbgraph,
			final SearchLatticeNode<NodeType, EdgeType> node,
			final HPGraph<NodeType, EdgeType> superGraph,
			final HPGraph<NodeType, EdgeType> subGraph, final int[] superNodes,
			final int[] subNodes, int ackNode) {
		if (ackNode >= subGraph.getMaxNodeIndex()) {
			final DefaultFlatHPEmbedding<NodeType, EdgeType, DB> emb = new DefaultFlatHPEmbedding<NodeType, EdgeType, DB>();
			call(node, emb.set(dbgraph, subGraph, superNodes));
			return;
		}
		// skip invalid nodes
		while (!subGraph.isValidNode(ackNode)) {
			ackNode++;
		}
		final int nodeLabel = subGraph.getNodeLabelIndex(ackNode, env);
		for (int superNode = 0; superNode < subNodes.length; superNode++) {
			// search unused right labeled super node
			if (subNodes[superNode] == superNodes.length
					&& superGraph.getNodeLabelIndex(superNode, env) == nodeLabel) {
				// check connections to previous nodes
				boolean connected = true;
				for (final IntIterator eit = subGraph.getEdgeIndices(ackNode); connected
						&& eit.hasNext();) {
					final int subE = eit.next();
					final int oSubNode = subGraph.getOtherNode(subE, ackNode);
					if (oSubNode < ackNode) {
						final int dir = subGraph.getDirection(subE, ackNode);
						final int supE = dir == Edge.INCOMING ? superGraph
								.getEdge(superNodes[oSubNode], superNode)
								: superGraph.getEdge(superNode,
										superNodes[oSubNode]);
						connected &= supE != HPGraph.NO_EDGE
								&& superGraph.getEdgeLabelIndex(supE, env) == subGraph
										.getEdgeLabelIndex(subE, env);
					}
				}
				if (connected) {
					superNodes[ackNode] = superNode;
					subNodes[superNode] = ackNode;

					searchEmbeddings(dbgraph, node, superGraph, subGraph,
							superNodes, subNodes, ackNode + 1);

					subNodes[superNode] = superNodes.length;
					superNodes[ackNode] = HPGraph.NO_NODE;
				}
			}
		}
	}

	/* initializes the recursive search for embeddings */
	private final void searchEmbeddings(final DB dbgraph,
			final SearchLatticeNode<NodeType, EdgeType> node,
			final int[] superNodes) {
		// Initialize embedding arrays

		final HPGraph<NodeType, EdgeType> superGraph = dbgraph.toHPGraph();
		final HPGraph<NodeType, EdgeType> subGraph = node.toHPFragment()
				.toHPGraph();
		final int[] subNodes = tenv.getIntArray(superGraph.getMaxNodeIndex());
		for (int i = 0; i < subNodes.length; i++) {
			subNodes[i] = superGraph.isValidNode(i) ? superNodes.length
					: HPGraph.NO_NODE;
		}
		searchEmbeddings(dbgraph, node, superGraph, subGraph, superNodes,
				subNodes, 0);
	}
}
