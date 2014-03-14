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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
public abstract class AbstractGraphComparator<NodeType, EdgeType> implements
		Comparator<Graph<NodeType, EdgeType>>, Serializable {

	/**
	 * compares two graphs
	 * 
	 * @param graphA
	 * @param graphB
	 * @return <code>0</code>, if the graphs are equal, otherwise
	 *         <code>-1</code>
	 */
	public int compare(final Graph<NodeType, EdgeType> graphA,
			final Graph<NodeType, EdgeType> graphB) {
		if (!precondition(graphA, graphB)) {
			return -1;
		}

		final ArrayList<Node<NodeType, EdgeType>> nodesA = new ArrayList<Node<NodeType, EdgeType>>();
		final ArrayList<Node<NodeType, EdgeType>> nodesB = new ArrayList<Node<NodeType, EdgeType>>();
		for (final Iterator<Node<NodeType, EdgeType>> nit = graphA
				.nodeIterator(); nit.hasNext();) {
			nodesA.add(nit.next());
		}
		for (final Iterator<Node<NodeType, EdgeType>> nit = graphB
				.nodeIterator(); nit.hasNext();) {
			nodesB.add(nit.next());
		}
		final Map<Node<NodeType, EdgeType>, Node<NodeType, EdgeType>> map = new HashMap<Node<NodeType, EdgeType>, Node<NodeType, EdgeType>>();
		final Node<NodeType, EdgeType> firstA = nodesA.get(0);
		for (final Node<NodeType, EdgeType> firstB : nodesB) {
			if (firstA.getLabel().equals(firstB.getLabel())) {
				map.put(firstA, firstB);
				// System.out.println("map "+firstA.getIndex()+" ->
				// "+firstB.getIndex());
				if (recursive(nodesA, nodesB, map, 1)) {
					return 0;
				}
				map.remove(firstA);
			}
		}
		return -1;
	}

	/**
	 * fast test to check the possibility of being equal to void NP-complete
	 * (sub)graph-isomorphism-test
	 * 
	 * @param graphA
	 * @param graphB
	 * @return <code>false</code>, if the ist no chance to be equal,
	 *         otherwise <code>true</code>
	 */
	abstract protected boolean precondition(Graph<NodeType, EdgeType> graphA,
			Graph<NodeType, EdgeType> graphB);

	/*
	 * recursive NP-complete (sub)graph-isomorphism-test
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
				if (ackA.getDegree() > ackB.getDegree()) {
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
