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
import java.util.SortedSet;
import java.util.TreeSet;

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
public class DAGmNewEdgeExtension<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> {

	public DAGmNewEdgeExtension(final MiningStep<NodeType, EdgeType> next) {
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
		final Node<NodeType, EdgeType> lastNode = actFragment.getLastNode();
		final HPGraph<NodeType, EdgeType> subGraph = actFragment
				.getHPSubGraph();
		final int lastLevel = actFragment.getLevel(lastNode);
		final int lastNodeIndex = lastNode.getIndex();
		final int prevNodeIndex = lastNodeIndex - 1;
		final NodeType lastNodeLabel = subGraph.getNodeLabel(lastNodeIndex);

		if (lastLevel == 1) {
			// only extend edges at higher levels
			callNext(node, extensions);
			return;
		}

		// if (lastNode.getIndex() > 0... -> kann man sich ueberall sparen, weil
		// mind. 2 Knoten existieren

		if ((actFragment.getLevel(lastNodeIndex - 1) == lastLevel)
				&& actFragment.samePartition(lastNodeIndex, lastNodeIndex - 1)) {
			// same partition at the moment -> do this extension earlier
			callNext(node, extensions);
			return;
		}

		final int lastCreatingNodeIndex = actFragment
				.getLastCreatingNodeIndex();
		final int lastEdgeCreatingNodeIndex = actFragment
				.getEdgeLastCreatingNodeIndex();

		// insert single edges to last inserted node here
		// gehe ueber alle Embeddings
		// hole passenden Knoten aus dem superGraph, hole incomingedges
		// teste ob die Gegenseite schon verwendet ist
		// falls nein, continue
		// Knotenindex im Subgraph kleiner als der letzte?
		// falls nein, continue
		// erzeuge neues Fragment

		final HashMap<Integer, DAGmFragment<NodeType, EdgeType>> edgeExtendedFragments = new HashMap<Integer, DAGmFragment<NodeType, EdgeType>>();
		final Iterator<HPEmbedding<NodeType, EdgeType>> embeddingIt = actFragment
				.iterator();
		while (embeddingIt.hasNext()) {
			final DAGmHPEmbedding<NodeType, EdgeType> actEmbedding = (DAGmHPEmbedding<NodeType, EdgeType>) embeddingIt
					.next();
			final int superNodeIndex = actEmbedding
					.getSuperGraphNodeIndex(lastNodeIndex);
			final HPGraph<NodeType, EdgeType> superGraph = actEmbedding
					.getHPSuperGraph();

			final IntIterator edgeIt = superGraph
					.getInEdgeIndices(superNodeIndex);
			while (edgeIt.hasNext()) {
				final int actEdgeIndex = edgeIt.next();
				final int otherNodeIndex = superGraph.getOtherNode(
						actEdgeIndex, superNodeIndex);
				final EdgeType actEdgeLabel = superGraph
						.getEdgeLabel(actEdgeIndex);

				if (!actEmbedding.isUsed(otherNodeIndex)) { // node must be used
					continue;
				}

				final int otherNodeInSubgraphIndex = actEmbedding
						.getSubGraphNodeIndex(otherNodeIndex);
				if (otherNodeInSubgraphIndex >= lastEdgeCreatingNodeIndex) { // extend
					// canonical
					continue;
				}

				// existiert ein Knoten mit der gleichen Partition (zu dem ich
				// dann logischerweise auch noch nicht verbunden bin?)
				// wird das nicht auch von den anderen Faellen erschlagen???
				// FIXME
				if (actFragment.samePartition(otherNodeInSubgraphIndex,
						otherNodeInSubgraphIndex + 1)) {
					continue;
				}

				// check for index
				// gleiches Label
				// mehr Eltern beim vor mir eingefuegten Knoten vorhanden
				// alle Eltern von mir sind auch Eltern von dem vor mir
				// eingefuegten Knoten
				// otherNodeInSubGraphIndex > max( nur vom Knoten vor mir
				// benutzte Eltern )
				if (pruneSameParent(actFragment, lastNode, subGraph, lastLevel,
						lastNodeIndex, prevNodeIndex, lastNodeLabel,
						otherNodeInSubgraphIndex)) {
					continue;
				}

				final DAGmHPEmbedding<NodeType, EdgeType> newEmbedding = new DAGmHPEmbedding<NodeType, EdgeType>();
				DAGmFragment<NodeType, EdgeType> newFragment;

				if (edgeExtendedFragments.containsKey(otherNodeInSubgraphIndex)) {
					newFragment = edgeExtendedFragments
							.get(otherNodeInSubgraphIndex);
				} else {
					final int nodeLevels[] = actFragment.getNodeLevels()
							.clone(); // copy
					// old
					// level
					// except
					// for
					// the
					// last
					// one

					final HPMutableGraph<NodeType, EdgeType> newGraph = (HPMutableGraph<NodeType, EdgeType>) actEmbedding
							.getHPSubGraph().clone();
					final int oldNodeIndex = otherNodeInSubgraphIndex;
					final int newNodeIndex = lastNodeIndex;
					newGraph.addEdgeIndex(oldNodeIndex, newNodeIndex,
							actEdgeLabel, Edge.OUTGOING); // FIXME
					// pay
					// attention
					// to
					// the
					// label!!!
					/*
					 * if (0 == gc.compare(DAGmExtender.g, newGraph.toGraph())) {
					 * System.err.println("ancestor of the duplicate is: ");
					 * System.err.println(LocalEnvironment.env(this).serializer.serialize(subGraph.toGraph())); }
					 */

					newFragment = new DAGmFragment<NodeType, EdgeType>(
							newGraph, nodeLevels);
					newFragment.setLastAction(LastAction.INSERTED_EDGE);
					newFragment.setLastCreatingNode(lastCreatingNodeIndex);
					newFragment
							.setLastEdgeCreatingNode(otherNodeInSubgraphIndex);

					edgeExtendedFragments.put(otherNodeInSubgraphIndex,
							newFragment);
					LocalEnvironment.env(this).stats.newEdge++;
				}

				final int superNodes[] = new int[actFragment.getNodeLevels().length + 1];
				System.arraycopy(actEmbedding.getSuperNodes(), 0, superNodes,
						0, superNodes.length - 1);
				superNodes[superNodes.length - 1] = otherNodeIndex;
				newEmbedding.set((DAGmGraph<NodeType, EdgeType>) actEmbedding
						.getDataBaseGraph(), newFragment.getHPSubGraph(),
						superNodes);

				newFragment.add(newEmbedding);
			}
		}

		// add these new found fragments to search lattice - but only frequent
		// one's
		for (final DAGmFragment<NodeType, EdgeType> newFragment : edgeExtendedFragments
				.values()) {
			if (LocalEnvironment.env(newFragment).minFreq.compareTo(newFragment
					.frequency()) <= 0) {
				extensions.add(new DAGmSearchLatticeNode<NodeType, EdgeType>(
						newFragment));
			}
		}
		callNext(node, extensions);
	}

	private boolean pruneSameParent(
			final DAGmFragment<NodeType, EdgeType> actFragment,
			final Node<NodeType, EdgeType> lastNode,
			final HPGraph<NodeType, EdgeType> subGraph, final int lastLevel,
			final int lastNodeIndex, final int prevNodeIndex,
			final NodeType lastNodeLabel, final int otherNodeInSubgraphIndex) {
		// check for index
		// gleiches Label
		// mehr Eltern beim vor mir eingefuegten Knoten vorhanden
		// alle Eltern von mir sind auch Eltern von dem vor mir eingefuegten
		// Knoten
		// otherNodeInSubGraphIndex > max( nur vom Knoten vor mir benutzte
		// Eltern )
		if ((actFragment.getLevel(lastNodeIndex - 1) == lastLevel)
				&& subGraph.getNodeLabel(lastNodeIndex - 1).equals(
						lastNodeLabel)
				&& (lastNode.getInDegree() < subGraph
						.getInDegree(lastNodeIndex - 1))) {
			boolean sameParents = true;
			final SortedSet<Integer> parentsA = new TreeSet<Integer>();
			final SortedSet<Integer> parentsB = new TreeSet<Integer>();
			IntIterator parentAIt = subGraph.getInEdgeIndices(lastNodeIndex);
			while (parentAIt.hasNext()) {
				final int actParent = parentAIt.next();
				parentsA.add(subGraph.getOtherNode(actParent, lastNodeIndex));
			}

			parentAIt = subGraph.getInEdgeIndices(prevNodeIndex);
			while (parentAIt.hasNext()) {
				final int actParent = parentAIt.next();
				parentsB.add(subGraph.getOtherNode(actParent, prevNodeIndex));
			}

			while (parentsA.size() > 0) {
				final int a = parentsA.last();
				final int b = parentsB.last();

				if (a != b) {
					sameParents = false;
					break;
				}

				parentsA.remove(parentsA.last());
				parentsB.remove(parentsB.last());
			}

			if (sameParents) {
				final int b = parentsB.last();
				if (otherNodeInSubgraphIndex > b) {
					return true;
				}
			}
		}
		return false;
	}

}
