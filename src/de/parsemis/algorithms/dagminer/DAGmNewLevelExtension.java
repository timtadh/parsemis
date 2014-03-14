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
import java.util.HashMap;
import java.util.Iterator;

import de.parsemis.algorithms.dagminer.DAGmFragment.LastAction;
import de.parsemis.graph.Edge;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.utils.IntIterator;

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
public class DAGmNewLevelExtension<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	public DAGmNewLevelExtension(final MiningStep<NodeType, EdgeType> next) {
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
		final HashMap<NodeType, DAGmFragment<NodeType, EdgeType>> extendedFragments = new HashMap<NodeType, DAGmFragment<NodeType, EdgeType>>();
		final Node<NodeType, EdgeType> lastNode = actFragment.getLastNode();
		final int lastLevel = actFragment.getLevel(lastNode);

		actFragment.getPartition(0); // assure that partitioning is computed

		// start new level
		// iteriere ueber alle knoten auf der letzten Ebene
		// gibt es eine ausgehende Kante zu einem noch nicht verwendeten Knoten?
		// erzeuge Fragment und Embedding wie vorher

		final HPGraph<NodeType, EdgeType> subGraph = actFragment
				.getHPSubGraph();
		final int nodeCount = subGraph.getNodeCount();
		for (int actNode = nodeCount - 1; actNode >= 0; actNode--) {
			extendedFragments.clear(); // ist clear oder new billiger? ist das
			// notwendig?

			if (actFragment.getLevel(actNode) < lastLevel) {
				// only extend nodes to the last level - topologically!
				break;
			}

			if (actNode + 1 < nodeCount) {
				if (actFragment.samePartition(actNode, actNode + 1)) {
					continue;
				}
			}

			final Iterator<HPEmbedding<NodeType, EdgeType>> embeddingIt = actFragment
					.iterator();
			while (embeddingIt.hasNext()) {
				final DAGmHPEmbedding<NodeType, EdgeType> actEmbedding = (DAGmHPEmbedding<NodeType, EdgeType>) embeddingIt
						.next();
				final HPGraph<NodeType, EdgeType> superGraph = actEmbedding
						.getHPSuperGraph();
				final int superNode = actEmbedding
						.getSuperGraphNodeIndex(actNode);

				final IntIterator edgeIt = superGraph
						.getOutEdgeIndices(superNode);
				while (edgeIt.hasNext()) {
					final int actEdge = edgeIt.next();
					final EdgeType actEdgeLabel = superGraph
							.getEdgeLabel(actEdge);
					final int otherNode = superGraph.getOtherNode(actEdge,
							superNode);
					final NodeType otherNodeLabel = superGraph
							.getNodeLabel(otherNode);

					// prune - this is a valid embedding, but does not ever lead
					// to *enough*
					final int nodeTypeIdx = LocalEnvironment.env(actFragment)
							.getNodeLabelIndex(otherNodeLabel);
					if (nodeTypeIdx < 0) {
						// System.out.println("dummdidumm");
						// //System.exit(nodeTypeIdx);
						continue;
					}

					if (!actEmbedding.isUsed(otherNode)) {
						// we found an useful extension?!
						final DAGmHPEmbedding<NodeType, EdgeType> newEmbedding = new DAGmHPEmbedding<NodeType, EdgeType>();
						DAGmFragment<NodeType, EdgeType> newFragment;

						if (extendedFragments.containsKey(otherNodeLabel)) {
							newFragment = extendedFragments.get(otherNodeLabel);
						} else {
							final int nodeLevels[] = new int[actFragment
									.getNodeLevels().length + 1];
							System.arraycopy(actFragment.getNodeLevels(), 0,
									nodeLevels, 0, nodeLevels.length - 1);
							nodeLevels[nodeLevels.length - 1] = lastLevel + 1; // new
							// node
							// is
							// one
							// level
							// below

							final HPMutableGraph<NodeType, EdgeType> newGraph = (HPMutableGraph<NodeType, EdgeType>) actEmbedding
									.getHPSubGraph().clone();
							final int oldNode = actNode; // is this always
							// true?
							final int newNode = newGraph
									.addNodeIndex(otherNodeLabel);
							newGraph.addEdgeIndex(oldNode, newNode,
									actEdgeLabel, Edge.OUTGOING);

							newFragment = new DAGmFragment<NodeType, EdgeType>(
									newGraph, nodeLevels);
							newFragment.setLastAction(LastAction.STARTED_LEVEL);
							newFragment.setLastCreatingNode(actNode); // extension
							// made
							// with
							// this
							// node?
							newFragment.setLastEdgeCreatingNode(actNode);

							extendedFragments.put(otherNodeLabel, newFragment);
							LocalEnvironment.env(this).stats.newLevel++;
						}

						final int superNodes[] = new int[actFragment
								.getNodeLevels().length + 1];
						System.arraycopy(actEmbedding.getSuperNodes(), 0,
								superNodes, 0, superNodes.length - 1);
						superNodes[superNodes.length - 1] = otherNode;

						newEmbedding.set(
								(DAGmGraph<NodeType, EdgeType>) actEmbedding
										.getDataBaseGraph(), newFragment
										.getHPSubGraph(), superNodes);

						newFragment.add(newEmbedding);
					}
				}

			}

			// add these new found fragments to search lattice - but only
			// frequent one's
			for (final DAGmFragment<NodeType, EdgeType> newFragment : extendedFragments
					.values()) {
				if (LocalEnvironment.env(newFragment).minFreq
						.compareTo(newFragment.frequency()) <= 0) {
					extensions
							.add(new DAGmSearchLatticeNode<NodeType, EdgeType>(
									newFragment));
				}
			}

		}

		callNext(node, extensions);
	}
}
