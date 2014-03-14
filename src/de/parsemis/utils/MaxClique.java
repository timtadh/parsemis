/**
 * created Jun 19, 2006
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
package de.parsemis.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.Node;

/**
 * This class calculates a maximal clique according to the algorithm of
 * Kumlander.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class MaxClique {

	/* helper class to store weights, colors and neigbors of the graph nodes */
	private static class CliqueNode {
		final int nodeIndex;

		final int weight;

		final int color;

		BitSet neighbor;

		CliqueNode(final int index, final int weight, final int color) {
			this.nodeIndex = index;
			this.weight = weight;
			this.color = color;
		}

		@SuppressWarnings("unchecked")
		void getNeigbors(final HPGraph graph, final int onodes[]) {
			neighbor = new BitSet(graph.getNodeCount());
			for (int i = graph.getDegree(nodeIndex) - 1; i >= 0; --i) {
				neighbor.set(onodes[graph.getOtherNode(graph.getNodeEdge(
						nodeIndex, i), nodeIndex)]);
			}
		}
	}

	/**
	 * calculates a maximal clique of nodes from the given collision graph
	 * 
	 * @param collisionGraph
	 * @return a Collection containing the nodes of the maximal clique
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 */
	public static <NodeType, EdgeType> Collection<Node<NodeType, EdgeType>> calculate(
			final Graph<NodeType, EdgeType> collisionGraph) {
		final Collection<Node<NodeType, EdgeType>> ret = new ArrayList<Node<NodeType, EdgeType>>();
		final int nodeC = collisionGraph.getNodeCount();
		final int edgeC = collisionGraph.getEdgeCount();

		if (nodeC == 1 || nodeC * (nodeC - 1) == edgeC * 2) {
			for (final Iterator<Node<NodeType, EdgeType>> nit = collisionGraph
					.nodeIterator(); nit.hasNext();) {
				ret.add(nit.next());
			}
			return ret;
		}
		final HPGraph<NodeType, EdgeType> hp = collisionGraph.toHPGraph();
		final CliqueNode nodes[] = colorGraph(hp, null);
		final BitSet cbc = getClique(nodes, null);
		for (int node = cbc.nextSetBit(0); node > 0; node = cbc
				.nextSetBit(node + 1)) {
			ret.add(collisionGraph.getNode(nodes[node].nodeIndex));
		}
		return ret;
	}

	/**
	 * calculates a subset of nodes of the given graph that represent a maximal
	 * clique
	 * 
	 * @param collisionGraph
	 * @return a BitSet where each set bit represents that the corrsponding node
	 *         is part of the clique
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 */
	public static <NodeType, EdgeType> BitSet calculateBitset(
			final Graph<NodeType, EdgeType> collisionGraph) {
		if (collisionGraph.getNodeCount() == 1) {
			final BitSet ret = new BitSet(1);
			ret.set(0);
			return ret;
		}
		final HPGraph<NodeType, EdgeType> hp = collisionGraph.toHPGraph();
		final CliqueNode nodes[] = colorGraph(hp, null);
		final BitSet cbc = getClique(nodes, null);
		return cbc;
	}

	/**
	 * calculates a (weighted) maximal clique of nodes from the given hp
	 * collision graph
	 * 
	 * @param collisionGraph
	 * @param weights
	 *            arrayof weights used for each node of the collision graph (may
	 *            be <code>null</code>)
	 * @return a BitSet where each set bit represents that the corrsponding node
	 *         is part of the clique
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 */
	public static <NodeType, EdgeType> BitSet calculateHP(
			final HPGraph<NodeType, EdgeType> collisionGraph,
			final int[] weights) {
		final int nodeC = collisionGraph.getNodeCount();
		final int edgeC = collisionGraph.getEdgeCount();

		if (nodeC == 1 || nodeC * (nodeC - 1) == edgeC * 2) {
			final BitSet ret = new BitSet(nodeC);
			ret.set(0, nodeC);
			return ret;
		}
		final CliqueNode nodes[] = colorGraph(collisionGraph, weights);
		final BitSet cbc = getClique(nodes, weights);
		final BitSet ret = new BitSet(collisionGraph.getMaxNodeIndex());
		for (int pos = cbc.nextSetBit(0); pos >= 0; pos = cbc
				.nextSetBit(pos + 1)) {
			ret.set(nodes[pos].nodeIndex);
		}
		return ret;
	}

	/*
	 * generates an coloring og the collision graph.
	 * 
	 * The colors of the nodes are used to speed up the clique detection,
	 * because nodes with the same color can never be part of the same clique.
	 * 
	 * The better the coloring, the more effectiv if the search, but it always
	 * will find a maximal clique
	 */
	@SuppressWarnings("unchecked")
	private final static CliqueNode[] colorGraph(final HPGraph graph,
			final int[] weights) {
		final int maxNode = graph.getMaxNodeIndex();
		final int nodeCount = graph.getNodeCount();
		final BitSet usedNeighborColors = new BitSet(nodeCount);

		final CliqueNode nodes[] = new CliqueNode[maxNode];
		// iterativly give each node the smallest possible colornumber
		for (int node = 0; node < nodes.length; ++node) {
			if (!graph.isValidNode(node)) {
				continue;
			}
			usedNeighborColors.clear();
			for (int j = graph.getDegree(node) - 1; j >= 0; --j) {
				final int other = graph.getOtherNode(
						graph.getNodeEdge(node, j), node);
				if (nodes[other] != null) {
					usedNeighborColors.set(nodes[other].color);
				}
			}
			nodes[node] = new CliqueNode(node, (weights == null ? 1
					: weights[node]), usedNeighborColors.nextClearBit(0));
		}
		// sort nodes by color and weight
		Arrays.sort(nodes, new Comparator<CliqueNode>() {
			public int compare(final CliqueNode ca, final CliqueNode cb) {
				if (ca == null) {
					return (cb == null ? 0 : -1);
				}
				if (cb == null) {
					return 1;
				}
				if (ca.color != cb.color) {
					return ca.color - cb.color;
				}
				return (ca.weight - cb.weight);
			}
		});

		// compute mapping bewteen node index and index in the sorted array
		final int onodes[] = new int[maxNode];
		for (int i = nodes.length - 1; i >= 0; --i) {
			if (nodes[i] != null) {
				onodes[nodes[i].nodeIndex] = i;
			}
		}
		final CliqueNode ret[] = new CliqueNode[nodeCount];
		int pos = 0;
		for (int i = 0; i < nodes.length; ++i) {
			if (nodes[i] != null) {
				// translate the neigbors into the correct indices of the sorted
				// node set
				nodes[i].getNeigbors(graph, onodes);
				ret[pos] = nodes[i];
				++pos;
			}
		}
		return ret;
	}

	/*
	 * fast clique compution with backtracking and color pruning
	 */
	private static BitSet getClique(final CliqueNode[] nodes,
			final int[] weights) {
		final BitSet currentBestClique = new BitSet(nodes.length);
		int currentBestCliqueWeight = 0;
		final int backtrack[] = new int[nodes.length];
		for (int n = nodes.length - 1; n >= 0; --n) {
			final BitSet currentClique = new BitSet(nodes.length);
			currentClique.set(n);
			// compute the best clique of the last n nodes
			final BitSet possibleCliqueNodes = new BitSet(nodes.length);
			possibleCliqueNodes.set(n, nodes.length);
			possibleCliqueNodes.and(nodes[n].neighbor);

			if (weights == null) {
				maxC(currentClique, possibleCliqueNodes, currentBestClique,
						backtrack, nodes);
				// store result for backtracking
				backtrack[n] = currentBestClique.cardinality();
			} else {
				currentBestCliqueWeight = maxC(currentClique, nodes[n].weight,
						possibleCliqueNodes, currentBestClique,
						currentBestCliqueWeight, backtrack, nodes);
				// store result for backtracking
				backtrack[n] = currentBestCliqueWeight;
			}
		}
		return currentBestClique;

	}

	/*
	 * computes the sum of maximal weighted nodes of each color in the set
	 */
	private final static int getMaxColorWeight(final BitSet cur,
			final CliqueNode nodes[]) {
		int ret = 0;
		int ln = cur.nextSetBit(0);
		if (ln >= 0) {
			int lc = nodes[ln].color;
			for (int next = cur.nextSetBit(ln + 1); next > 0; next = cur
					.nextSetBit(next + 1)) {
				final int cc = nodes[next].color;
				if (cc > lc) {
					ret += nodes[ln].weight;
					lc = cc;
				}
				ln = next;
			}
			ret += nodes[ln].weight;
		}
		return ret;
	}

	/*
	 * computes the number of different colors in the set
	 */
	private final static int getNumberOfColors(final BitSet cur, int last,
			final CliqueNode nodes[]) {
		int ret = 0;
		for (int next = cur.nextSetBit(0); next > 0; next = cur
				.nextSetBit(next + 1)) {
			final int cc = nodes[next].color;
			if (cc > last) {
				last = cc;
				ret++;
			}
		}
		return ret;
	}

	/*
	 * recursive clique extension
	 */
	private static void maxC(final BitSet currentClique,
			final BitSet possibleCliqueNodes, final BitSet currentBestClique,
			final int[] backtrack, final CliqueNode[] nodes) {
		int next = possibleCliqueNodes.nextSetBit(0);
		if (next < 0) {
			// no additional nodes for the current clique, check if current
			// clique is bigger than current best one
			if (currentClique.cardinality() > currentBestClique.cardinality()) {
				currentBestClique.clear();
				currentBestClique.or(currentClique);
			}
			return;
		}
		for (; next >= 0; next = possibleCliqueNodes.nextSetBit(next + 1)) {
			// check clique extension with possible nodes bigger than next
			final int diff = currentBestClique.cardinality()
					- currentClique.cardinality();
			if (backtrack[next] <= diff
					|| getNumberOfColors(possibleCliqueNodes,
							nodes[next].color, nodes) < diff) {
				// neither the best remaining possible clique nor possible nodes
				// of each color will suffice to overtop the current best
				// clique, so no further extension will result in a better
				// result
				return;
			}
			// extend clique with the next node
			possibleCliqueNodes.clear(next);
			currentClique.set(next);
			final BitSet neu = (BitSet) possibleCliqueNodes.clone();
			neu.and(nodes[next].neighbor);

			// recursive step to try cliques containing the next node
			maxC(currentClique, neu, currentBestClique, backtrack, nodes);

			// remove current next node to try cliques without this node
			currentClique.clear(next);
		}
	}

	/*
	 * recursive clique extension
	 */
	private static int maxC(final BitSet currentClique,
			final int currentWeight, final BitSet possibleCliqueNodes,
			final BitSet currentBestClique, int currentBestCliqueWeight,
			final int[] backtrack, final CliqueNode[] nodes) {
		int next = possibleCliqueNodes.nextSetBit(0);
		if (next < 0) {
			// no additional nodes for the current clique, check if current
			// clique is bigger than current best one
			if (currentWeight > currentBestCliqueWeight) {
				currentBestClique.clear();
				currentBestClique.or(currentClique);
				return currentWeight;
			}
			return currentBestCliqueWeight;
		}
		for (; next > 0; next = possibleCliqueNodes.nextSetBit(next + 1)) {
			// check clique extension with possible nodes bigger than next
			final int diff = currentBestCliqueWeight - currentWeight;
			if (backtrack[next] <= diff
					|| getMaxColorWeight(possibleCliqueNodes, nodes) < diff) {
				// neither the best remaining possible clique nor the heavyest
				// possible nodes
				// of each color will suffice to overtop the current best
				// clique, so no further extension will result in a better
				// result
				return currentBestCliqueWeight;
			}
			// extend clique with the next node
			possibleCliqueNodes.clear(next);
			currentClique.set(next);
			final BitSet neu = (BitSet) possibleCliqueNodes.clone();
			neu.and(nodes[next].neighbor);

			// recursive step to try cliques containing the next node
			currentBestCliqueWeight = maxC(currentClique, currentWeight
					+ nodes[next].weight, neu, currentBestClique,
					currentBestCliqueWeight, backtrack, nodes);

			// remove current next node to try cliques without this node
			currentClique.clear(next);
		}
		return currentBestCliqueWeight;
	}

}
