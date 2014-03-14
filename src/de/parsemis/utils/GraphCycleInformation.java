/**
 * created Oct 26, 2006
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
import java.util.BitSet;

import de.parsemis.graph.HPGraph;

/**
 * This class contains information about cycles in a graph.
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
public class GraphCycleInformation<NodeType, EdgeType> {

	/**
	 * computes information about all cycles in the given graph
	 * 
	 * @param graph
	 * @return a GraphCycleInformation object containing the calculated
	 *         information
	 * 
	 * @param <NodeType>
	 *            the type of the node labels
	 * @param <EdgeType>
	 *            the type of the edge labels
	 */
	public static <NodeType, EdgeType> GraphCycleInformation<NodeType, EdgeType> getCycles(
			final HPGraph<NodeType, EdgeType> graph) {
		return getCycles(graph, 1, graph.getNodeCount());
	}

	/**
	 * computes information about the cycles in the given graph
	 * 
	 * @param graph
	 * @param minSize
	 *            the minimal size of a cycle to be mentioned
	 * @param maxSize
	 *            the maximal size of a cycle to be mentioned
	 * @return a GraphCycleInformation object containing the calculated
	 *         information
	 * 
	 * @param <NodeType>
	 *            the type of the node labels
	 * @param <EdgeType>
	 *            the type of the edge labels
	 */
	public static <NodeType, EdgeType> GraphCycleInformation<NodeType, EdgeType> getCycles(
			final HPGraph<NodeType, EdgeType> graph, final int minSize,
			final int maxSize) {
		final BitSet[] nodeRingMembership = new BitSet[graph.getMaxNodeIndex()];
		final BitSet[] edgeRingMembership = new BitSet[graph.getMaxEdgeIndex()];
		int ringCount = 0;

		final BitSet visited = new BitSet(graph.getMaxNodeIndex());
		final int[] parent = new int[graph.getMaxNodeIndex()];
		final int[] distance = new int[graph.getMaxNodeIndex()];

		// iteratively remove nodes with degree <= 1
		// nodes which have a degree < 2 afterwards do not need to be checked by
		// Dijkstra
		final int[] degree = new int[graph.getMaxNodeIndex()];
		for (int i = degree.length - 1; i >= 0; i--) {
			if (graph.isValidNode(i)) {
				degree[i] = graph.getDegree(i);
			}
		}
		boolean change;
		do {
			change = false;
			for (int node = 0; node < degree.length; node++) {
				if (degree[node] == 1) {
					change = true;
					degree[node] = 0;
					degree[graph.getOtherNode(graph.getNodeEdge(node, 0), node)]--;
				}
			}
		} while (change);

		for (int edge = graph.getEdgeCount() - 1; edge >= 0; edge--) {
			if (graph.isValidEdge(edge)) {
				final int nodeA = graph.getNodeA(edge);
				if (degree[nodeA] < 2) {
					continue;
				}

				final int nodeB = graph.getNodeB(edge);
				if (degree[nodeB] < 2) {
					continue;
				}

				shortestPath(graph, nodeA, visited, parent, distance, edge);

				if ((distance[nodeB] >= (minSize - 1))
						&& (distance[nodeB] <= (maxSize - 1))) {
					if (markCycle(graph, nodeB, edge, parent,
							edgeRingMembership, ringCount)) {
						ringCount++;
					}
				}
			}
		}

		for (int node = graph.getNodeCount() - 1; node >= 0; node--) {
			if (graph.isValidNode(node)) {
				final BitSet mask = new BitSet();
				for (int k = graph.getDegree(node) - 1; k >= 0; k--) {
					final BitSet b = edgeRingMembership[graph.getNodeEdge(node,
							k)];
					if (b != null) {
						mask.or(b);
					}
				}
				nodeRingMembership[node] = mask;
			}
		}

		return new GraphCycleInformation<NodeType, EdgeType>(ringCount,
				nodeRingMembership, edgeRingMembership, graph);
	}

	/*
	 * for cycle detection
	 */
	private static <NodeType, EdgeType> boolean markCycle(
			final HPGraph<NodeType, EdgeType> graph, final int endNode,
			final int forbiddenEdge, final int[] parent,
			final BitSet[] edgeRingMembership, final int ringCount) {
		if (edgeRingMembership[forbiddenEdge] == null) {
			edgeRingMembership[forbiddenEdge] = new BitSet();
		}
		final BitSet ringMask = (BitSet) edgeRingMembership[forbiddenEdge]
				.clone();

		int currentNode = endNode;
		// It may happen, that two rings have only a node in common but no edge.
		// So the free ring id
		// calculation must look at
		// all edges adjacent to all ring nodes.
		/*
		 * for (int i = graph.getDegree(currentNode) - 1; i >= 0; i--) { int e =
		 * graph.getNodeEdge(currentNode, i);
		 * 
		 * ringID.or(edgeRingMembership[e]); }
		 */

		while (parent[currentNode] != -1) {
			int edge = graph.getEdge(currentNode, parent[currentNode]);
			if (edge == HPGraph.NO_EDGE) {
				// in a directed graph the edge may go into the other direction
				edge = graph.getEdge(parent[currentNode], currentNode);
			}
			if (edgeRingMembership[edge] == null) {
				edgeRingMembership[edge] = new BitSet();
			}
			ringMask.and(edgeRingMembership[edge]);

			currentNode = parent[currentNode];

			// It may happen, that two rings have only a node in common but no
			// edge. So the free ring id
			// calculation must look at
			// all edges adjacent to all ring nodes.
			/*
			 * for (int i = graph.getDegree(currentNode) - 1; i >= 0; i--) { int
			 * e = graph.getNodeEdge(currentNode, i);
			 * 
			 * ringID |= edgeRingMembership[graph.getEdgeIndex(e)]; }
			 */
		}

		if (ringMask.isEmpty()) {
			edgeRingMembership[forbiddenEdge].set(ringCount);
			currentNode = endNode;
			while (parent[currentNode] != -1) {
				int edge = graph.getEdge(currentNode, parent[currentNode]);
				if (edge == HPGraph.NO_EDGE) {
					// in a directed graph the edge may go into the other
					// direction
					edge = graph.getEdge(parent[currentNode], currentNode);
				}
				edgeRingMembership[edge].set(ringCount);
				currentNode = parent[currentNode];
			}
			return true;
		}
		return false;
	}

	/*
	 * for cycle detection
	 */
	private static <NodeType, EdgeType> void shortestPath(
			final HPGraph<NodeType, EdgeType> graph, final int startNode,
			final BitSet nodeVisited, final int[] parent, final int[] distance,
			final int forbiddenEdge) {
		nodeVisited.clear();
		for (int k = 0; k < distance.length; k++) {
			distance[k] = Integer.MAX_VALUE;
			parent[k] = -1;
		}

		int currentNode = startNode;
		distance[startNode] = 0;

		while (!nodeVisited.get(currentNode)) {
			// System.out.print(currentNode);
			// printlnArr(System.out,parent);
			nodeVisited.set(currentNode);

			for (int i = graph.getDegree(currentNode) - 1; i >= 0; i--) {
				final int edge = graph.getNodeEdge(currentNode, i);
				if (edge == forbiddenEdge) {
					continue;
				}

				final int neighbourNode = graph.getOtherNode(edge, currentNode);

				if (distance[neighbourNode] > (distance[currentNode] + 1)) {
					distance[neighbourNode] = distance[currentNode] + 1;
					parent[neighbourNode] = currentNode;
				}
			}

			// get the next node
			currentNode = 0;
			int dist = Integer.MAX_VALUE;
			for (int i = nodeVisited.nextClearBit(0); i < distance.length; i = nodeVisited
					.nextClearBit(i + 1)) {
				if (dist > distance[i]) {
					dist = distance[i];
					currentNode = i;
				}
			}
		}
	}

	private final int cycleCount;

	private final BitSet[] nodeRingMembership;

	private final BitSet[] edgeRingMembership;

	private final HPGraph<NodeType, EdgeType> graph;

	public GraphCycleInformation(final int cycleCount,
			final BitSet[] nodeRingMembership,
			final BitSet[] edgeRingMembership,
			final HPGraph<NodeType, EdgeType> graph) {
		this.cycleCount = cycleCount;
		this.nodeRingMembership = nodeRingMembership;
		this.edgeRingMembership = edgeRingMembership;
		this.graph = graph;
	}

	public int cycleCount() {
		return cycleCount;
	}

	public IntIterator edgeCycleIterator(final int edgeindex) {
		return new BitSetIterator(this.edgeRingMembership[edgeindex]);
	}

	public BitSet[] getEdgeRingMembershipBitSet() {
		return edgeRingMembership;
	}

	public HPGraph<NodeType, EdgeType> getGraph() {
		return graph;
	}

	public BitSet getNodeCycleMembership(final int nodeIndex) {
		return this.nodeRingMembership[nodeIndex];
	}

	public BitSet[] getNodeRingMembershipBitSet() {
		return nodeRingMembership;
	}

	public ArrayList<Integer> getNodesNotInRings() {
		final ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < nodeRingMembership.length; i++) {

			if (nodeRingMembership[i].isEmpty()) {
				result.add(i);
			}
		}
		return result;
	}

	public boolean isEdgeInCycle(final int edgeindex, final int cycleIndex) {
		return (this.edgeRingMembership[edgeindex] != null)
				&& edgeRingMembership[edgeindex].get(cycleIndex);
	}

	public boolean isNodeInCycle(final int nodeindex, final int cycleIndex) {
		return (!this.nodeRingMembership[nodeindex].isEmpty() || this.nodeRingMembership[nodeindex] != null)
				&& nodeRingMembership[nodeindex].get(cycleIndex);
	}

	public boolean isNodeInCycles(final int nodeIndex) {
		return !this.nodeRingMembership[nodeIndex].isEmpty();
	}

	public IntIterator nodeCycleIterator(final int nodeindex) {
		return new BitSetIterator(this.nodeRingMembership[nodeindex]);
	}

}
