/**
 * created May 29, 2006
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
package de.parsemis.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Map;

import de.parsemis.miner.environment.Relabler;
import de.parsemis.miner.environment.SimpleRelabler;

/**
 * Compares two graphs for structural and label equality.
 * <p>
 * It determines no total order on graphs, so it do NOT correctly fits the
 * Comparator interface!!
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
public class HPGraphComparator<NodeType, EdgeType> implements
		Comparator<HPGraph<NodeType, EdgeType>>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final Relabler<NodeType, EdgeType> rel;

	public HPGraphComparator() {
		this(new SimpleRelabler<NodeType, EdgeType>());
	}

	public HPGraphComparator(final Relabler<NodeType, EdgeType> rel) {
		this.rel = rel;
	}

	protected boolean canMatch(final HPGraph<NodeType, EdgeType> g1,
			final int nodeA, final HPGraph<NodeType, EdgeType> g2,
			final int nodeB, final int[] parts1, final int[] parts2,
			final int[][] map, final int count) {
		if (!g1.getNodeLabel(nodeA).equals(g2.getNodeLabel(nodeB))) {
			return false;
		}

		if (parts1 != null) {
			if (parts1[nodeA] != parts2[nodeB]) {
				return false;
			}
		}

		for (int i = count; i >= 0; i--) {
			final int edge1 = g1.getEdge(nodeA, map[0][i]);
			final int edge2 = g2.getEdge(nodeB, map[1][i]);

			if ((edge1 == HPGraph.NO_EDGE) ^ (edge2 == HPGraph.NO_EDGE)) {
				return false;
			} else if ((edge1 != HPGraph.NO_EDGE)
					&& (!g1.getEdgeLabel(edge1).equals(g2.getEdgeLabel(edge2)))) {
				return false;
			}

			// if the both graphs are directed also check edges going into the
			// other direction
			final int edge3 = g1.getEdge(map[0][i], nodeA);
			final int edge4 = g2.getEdge(map[1][i], nodeB);

			if ((edge3 == HPGraph.NO_EDGE) ^ (edge4 == HPGraph.NO_EDGE)) {
				return false;
			} else if ((edge3 != HPGraph.NO_EDGE)
					&& (!g1.getEdgeLabel(edge3).equals(g2.getEdgeLabel(edge4)))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * compares two graphs
	 * 
	 * @param graphA
	 * @param graphB
	 * @return <code>0</code>, if the graphs are equal, otherwise
	 *         <code>-1</code>
	 */
	public int compare(final HPGraph<NodeType, EdgeType> graphA,
			final HPGraph<NodeType, EdgeType> graphB) {

		final HPGraph<NodeType, EdgeType> g1 = graphA;
		final HPGraph<NodeType, EdgeType> g2 = graphB;
		final int[] partitions1 = g1.getNodePartions(rel);
		final int[] partitions2 = g2.getNodePartions(rel);

		if (g1.getNodeCount() != g2.getNodeCount()) {
			return -1;
		}
		if (g1.getEdgeCount() != g2.getEdgeCount()) {
			return -1;
		}

		int partCount1 = 0;
		for (int i = 0; i < partitions1.length; i++) {
			partCount1 = (partitions1[i] > partCount1) ? partitions1[i]
					: partCount1;
		}

		int partCount2 = 0;
		for (int i = 0; i < partitions2.length; i++) {
			partCount2 = (partitions2[i] > partCount2) ? partitions2[i]
					: partCount2;
		}

		if (partCount1 != partCount2) {
			return -1;
		}

		final int[][] map = new int[2][g1.getNodeCount()];
		for (int i = 0; i < map[0].length; i++) {
			map[0][i] = -1;
		}
		for (int i = 0; i < map[1].length; i++) {
			map[1][i] = -1;
		}

		final BitSet avail = new BitSet();
		avail.or(g2.getNodes());

		if (getIsomorphism(map, g1, g2, partitions1, partitions2, avail, 0)) {
			return 0;
		}

		return -1;
	}

	protected boolean getIsomorphism(final int[][] map,
			final HPGraph<NodeType, EdgeType> g1,
			final HPGraph<NodeType, EdgeType> g2, final int[] parts1,
			final int[] parts2, final BitSet avail, final int count) {
		if (count >= g1.getMaxNodeIndex()) {
			return true;
		}
		if (!g1.isValidNode(count)) {
			return getIsomorphism(map, g1, g2, parts1, parts2, avail, count + 1);
		}

		for (int i = 0; i < g2.getMaxNodeIndex(); i++) {

			if (avail.get(i)
					&& canMatch(g1, count, g2, i, parts1, parts2, map,
							count - 1)) {
				map[0][count] = count;
				map[1][count] = i;
				avail.clear(i);

				if (getIsomorphism(map, g1, g2, parts1, parts2, avail,
						count + 1)) {
					return true;
				}

				map[0][count] = -1;
				map[1][count] = -1;
				avail.set(i);
			}
		}

		return false;
	}

	/*
	 * recursiv NP-complete (sub)graph-isomorphism-test
	 */
	private boolean recursive(final ArrayList<Node<NodeType, EdgeType>> nodesA,
			final ArrayList<Node<NodeType, EdgeType>> nodesB,
			final Map<Node<NodeType, EdgeType>, Node<NodeType, EdgeType>> map,
			final int posA) {

		if (posA == nodesA.size()) {
			return true;
		}

		final Node<NodeType, EdgeType> ackA = nodesA.get(posA);
		for (final Node<NodeType, EdgeType> ackB : nodesB) {
			// try to map ackB to the current node ackA
			if (!map.containsValue(ackB)
					&& ackA.getLabel().equals(ackB.getLabel())) {
				boolean match = true;
				if (ackA.getDegree() != ackB.getDegree()) {
					match = false;
				}
				// try to map all edges of ackA to edges of ackB
				for (int i = 0; match && i < posA; ++i) {
					final Node<NodeType, EdgeType> oNodeA = nodesA.get(i);
					final Edge<NodeType, EdgeType> edgeA = ackA.getGraph()
							.getEdge(ackA, oNodeA);
					if (edgeA != null) {
						final Edge<NodeType, EdgeType> edgeB = ackB.getGraph()
								.getEdge(ackB, map.get(oNodeA));
						if (edgeB == null
								|| !edgeA.getLabel().equals(edgeB.getLabel())
								|| edgeA.getDirection(ackA) != edgeB
										.getDirection(ackB)) {
							match = false;
						}
					}
					final Edge<NodeType, EdgeType> edgeA2 = ackA.getGraph()
							.getEdge(oNodeA, ackA);
					if (edgeA2 != null) {
						final Edge<NodeType, EdgeType> edgeB = ackB.getGraph()
								.getEdge(map.get(oNodeA), ackB);
						if (edgeB == null
								|| !edgeA2.getLabel().equals(edgeB.getLabel())
								|| edgeA2.getDirection(ackA) != edgeB
										.getDirection(ackB)) {
							match = false;
						}
					}
				}
				// all edges can be mapped, so recursivly try next node in A
				if (match) {
					map.put(ackA, ackB);
					if (recursive(nodesA, nodesB, map, posA + 1)) {
						// complete match found
						return true;
					}
					map.remove(ackA);
				}
			}
		}
		return false;
	}

}
