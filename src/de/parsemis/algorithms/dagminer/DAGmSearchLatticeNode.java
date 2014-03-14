/**
 * created 19.06.2006
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.MutableGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.miner.general.HPFragment;
import de.parsemis.utils.Canonizable;
import de.parsemis.utils.Frequented;
import de.parsemis.utils.Generic;
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
public class DAGmSearchLatticeNode<NodeType, EdgeType> extends
		SearchLatticeNode<NodeType, EdgeType> implements
		// Comparable<DAGmSearchLatticeNode<NodeType,EdgeType>>,
		Generic<NodeType, EdgeType>, Extension<NodeType, EdgeType>,
		Canonizable, Frequented {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2176861853434568197L;

	public static <NodeType, EdgeType> String canonicalCode(
			final Graph<NodeType, EdgeType> origSubGraph) {
		final HPGraph<NodeType, EdgeType> subGraph = origSubGraph.toHPGraph();
		String ret = "";

		// FIXME ugly! hack
		int i = 0;
		try {
			for (i = 0;; i++) {
				LocalEnvironment.env(subGraph).getNodeLabel(i);
			}
		} catch (final IndexOutOfBoundsException e) {
			int stellen = 0;
			while (i > 0) {
				stellen++;
				i /= 10;
			}

			// assume that this graph was built up topologically
			final IntIterator nodeIt = subGraph.nodeIndexIterator();

			while (nodeIt.hasNext()) {
				final int actNode = nodeIt.next();
				// Node<NodeType,EdgeType> actNode = nodeIt.next();

				// LocalEnvironment.env(subGraph).getNodeLabelIndex()
				String label = ""
						+ LocalEnvironment.env(subGraph).getNodeLabelIndex(
								subGraph.getNodeLabel(actNode));
				while (label.length() < stellen) {
					label = "0" + label;
				}
				ret += "[" + label + "]" + actNode;
				ret += "(";

				// Iterator<Edge<NodeType,EdgeType>> edgeIt =
				// actNode.incommingEdgeIterator();
				final IntIterator edgeIt = subGraph.getInEdgeIndices(actNode);

				// FIXME assume the edge label is 1
				final TreeSet<Integer> linkingNodes = new TreeSet<Integer>();
				while (edgeIt.hasNext()) {
					// Edge<NodeType,EdgeType> actEdge = edgeIt.next();
					final int actEdge = edgeIt.next();

					// FIXME same for the edge label
					// ret += "[" +
					// LocalEnvironment.env(subGraph).getEdgeLabelIndex(actEdge.getLabel())
					// + "]";
					// ret += actEdge.getOtherNode(actNode).getIndex();
					linkingNodes.add(subGraph.getOtherNode(actEdge, actNode));
				}
				if (linkingNodes.size() > 0) {
					final int[] linkNodes = new int[linkingNodes.size()];
					int j = 0;
					final Iterator<Integer> linkIt = linkingNodes.iterator();
					while (linkIt.hasNext()) {
						linkNodes[j] = linkIt.next();
						j++;
					}

					for (j = linkNodes.length - 1; j >= 0; j--) {
						// FIXME assume the edge label is 1
						ret += "[" + 1 + "]";
						ret += linkNodes[j];
					}
				}

				ret += ")";
			}

		}
		return ret;
	}

	/*
	 * public DAGmSearchLatticeNode(Fragment<NodeType,EdgeType> fragment) {
	 * this.fragment = (DAGmFragment<NodeType,EdgeType>) fragment; // FIXME }
	 */

	// generate ordered permutations of an integer array
	public static boolean next_permutation(final int a[]) {
		for (int i = a.length - 2, j; i >= 0; i--) {
			if (a[i + 1] > a[i]) {
				for (j = a.length - 1; a[j] <= a[i]; j--) {
					;
				}

				swap(a, i, j);
				for (j = 1; j <= (a.length - i) / 2; j++) {
					swap(a, i + j, a.length - j);
				}
				return true;
			}
		}
		return false;
	}

	public static void swap(final int a[], final int i, final int j) {
		final int temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}

	private final DAGmFragment<NodeType, EdgeType> fragment;

	public DAGmSearchLatticeNode(final HPFragment<NodeType, EdgeType> fragment) {
		this.fragment = (DAGmFragment<NodeType, EdgeType>) fragment; // FIXME
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#allEmbeddings()
	 */
	@Override
	public Collection<HPEmbedding<NodeType, EdgeType>> allEmbeddings() {
		// TODO Auto-generated method stub
		return null;
	}

	public String canonicalCode() {
		return canonicalCode(fragment.getSubGraph());
	}

	private boolean canRecursion(final int actIndex, final int mapping[]) {
		final HPGraph<NodeType, EdgeType> subGraph = fragment.getHPSubGraph();
		final ArrayList<Integer> sameLabelAndLevel = new ArrayList<Integer>();

		if (actIndex >= mapping.length) {
			return true;
		}

		final NodeType actLabel = subGraph.getNodeLabel(actIndex);
		int lastPartition = fragment.partitionNumber[actIndex];
		sameLabelAndLevel.add(actIndex);

		// compute partition frontiers
		int i = actIndex;
		for (i = actIndex; i < mapping.length; i++) {
			if (fragment.getLevel(i) != fragment.getLevel(actIndex)) {
				break;
			}
			if (!subGraph.getNodeLabel(i).equals(actLabel)) {
				break;
			}
			if (fragment.partitionNumber[i] != lastPartition) {
				lastPartition = fragment.partitionNumber[i];
				sameLabelAndLevel.add(i);
			}
		}

		// prepare permutation of partitions
		final int nextRecursionStep = i;

		final int partitionStart[] = new int[sameLabelAndLevel.size()];
		for (int j = 0; j < partitionStart.length; j++) {
			partitionStart[j] = j;
		}

		sameLabelAndLevel.add(nextRecursionStep);

		// permutate
		do {
			// create mapping in both directions
			int index = actIndex;
			for (int j = 0; j < partitionStart.length; j++) {
				for (int k = sameLabelAndLevel.get(partitionStart[j]); k < sameLabelAndLevel
						.get(partitionStart[j] + 1); k++) {
					mapping[index] = k;
					index++;
				}
			}
			boolean valid = true;
			final int mapOldToNew[] = new int[mapping.length];
			for (int j = 0; j < mapping.length; j++) {
				mapOldToNew[mapping[j]] = j;
			}

			boolean recur = true;
			for (int k = actIndex; k < nextRecursionStep; k++) {
				final SortedSet<Integer> lastParents = new TreeSet<Integer>();
				final SortedSet<Integer> parentsB = new TreeSet<Integer>();

				// new parents
				IntIterator edgeIt = subGraph.getInEdgeIndices(mapping[k]);
				while (edgeIt.hasNext()) {
					final int actEdge = edgeIt.next();
					final int otherNode = subGraph.getOtherNode(actEdge,
							mapping[k]);
					parentsB.add(mapOldToNew[otherNode]);
				}

				// old parents - could these be stored anywhere?
				edgeIt = subGraph.getInEdgeIndices(k);
				while (edgeIt.hasNext()) {
					final int actEdge = edgeIt.next();
					final int otherNode = subGraph.getOtherNode(actEdge, k);
					lastParents.add(otherNode);
				}

				// compare parents
				boolean sameParents = true;
				while (parentsB.size() > 0 && lastParents.size() > 0) {
					final int parB = parentsB.last();
					final int parA = lastParents.last();
					if (parA != parB) {
						if (parA < parB) {
							valid = false;
						}
						sameParents = false;
						break;
					}

					lastParents.remove(parA);
					parentsB.remove(parB);
				}

				// new one is better
				if (!valid) {
					break;
				}
				if (sameParents && parentsB.size() > 0) {
					valid = false;
					break;
				}

				// old one is better
				if (!sameParents || lastParents.size() > 0) {
					recur = false;
					break;
				}
			}

			if (!valid) {
				return false;
			}

			if (recur) {
				// only recur, if old one is not better until now
				if (!canRecursion(nextRecursionStep, mapping)) {
					return false;
				}
			}
		} while (next_permutation(partitionStart));

		// restore old mapping
		for (int j = actIndex; j < nextRecursionStep; j++) {
			mapping[j] = j;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(final Extension<NodeType, EdgeType> arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(
				"compareTo is not supported yet");
		// return -1; // FIXME
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final Object obj) {
		// reference comparision as long as no real compareTo implementation
		if (true) {
			return obj == this;
		}
		return obj instanceof Extension
				&& compareTo((Extension<NodeType, EdgeType>) obj) == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#extend(de.parsemis.miner.Extension)
	 */
	@Override
	public SearchLatticeNode<NodeType, EdgeType> extend(
			final Extension<NodeType, EdgeType> extension) {
		// TODO Auto-generated method stub
		System.err.println("extend node");
		return null;
	}

	@Override
	public void finalizeIt() {
		// TODO Auto-generated method stub
		fragment.freeUnusedInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	public Frequency frequency() {
		return fragment.frequency();
	}

	@Override
	public int getThreadNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return fragment.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Canonizable#isCanonical()
	 */
	public boolean isCanonical() {
		if (fragment.getLastAction() == DAGmFragment.LastAction.INSERTED_NODE
				|| fragment.getLastAction() == DAGmFragment.LastAction.STARTED_LEVEL
				|| fragment.getLastAction() == DAGmFragment.LastAction.INSERTED_BOTH
		// || fragment.getLastAction() == DAGmFragment.LastAction.INSERTED_EDGE
		) {
			return true;
		}

		if (true) {
			return isCanonical2();
		}

		final String actCode = canonicalCode();

		boolean debug = false;
		final Graph<NodeType, EdgeType> subGraph = fragment.getSubGraph();
		// assume that this graph was built up topologically!

		if (subGraph.getEdgeCount() == -1 && subGraph.getNodeCount() == -1) {
			debug = true;
		} else {
		}

		if (debug) {
			System.err.println("-------------------------");
			System.err.println(actCode);
			System.err.println("---");
		}

		// store mapping from old to new node id
		final int mapping[] = new int[subGraph.getNodeCount()];
		for (int i = 0; i < mapping.length; i++) {
			mapping[i] = i;
		}

		// compute partitioning
		fragment.getPartition(0);

		do {
			boolean valid = true;

			// ignore invalid mappings
			for (int i = 0; i < mapping.length; i++) {
				if (fragment.getLevel(i) != fragment.getLevel(mapping[i])) {
					// do not shift node to another level! // FIXME use this
					// limitation to speed up permutation construction
					valid = false;
					break;
				}
			}

			if (!valid) {
				continue;
			}

			// mapping the other way around
			final int mapOldToNew[] = new int[mapping.length];
			for (int i = 0; i < mapping.length; i++) {
				if (debug) {
					System.err.print(mapping[i] + " ");
				}
				mapOldToNew[mapping[i]] = i;
			}
			if (debug) {
				System.err.println();
			}

			// puh, don't try this at home:
			final MutableGraph<NodeType, EdgeType> newGraph = LocalEnvironment
					.env(this).newGraph();
			for (int i = 0; i < mapping.length; i++) {
				newGraph.addNode(subGraph.getNode(mapping[i]).getLabel());
			}
			for (int i = 0; i < mapping.length; i++) {
				final Iterator<Edge<NodeType, EdgeType>> edgeIt = subGraph
						.getNode(mapping[i]).incommingEdgeIterator();
				while (edgeIt.hasNext()) {
					final Edge<NodeType, EdgeType> actEdge = edgeIt.next();
					newGraph.addEdge(newGraph.getNode(i), newGraph
							.getNode(mapOldToNew[actEdge.getOtherNode(
									subGraph.getNode(mapping[i])).getIndex()]),
							actEdge.getLabel(), Edge.INCOMING);
				}
			}
			final String tmpCode = canonicalCode(newGraph);
			if (debug) {
				System.err.println("new canonical code: " + tmpCode);
			}
			if (tmpCode.compareTo(actCode) > 0) {
				if (debug) {
					System.err.println("XXX <- was offending code");
				}
				System.err.println("not canonical");
				return false;
			}
		} while (next_permutation(mapping));

		// System.err.println("testing with can2");
		isCanonical2();

		return true; // FIXME
	}

	public boolean isCanonical2() {
		fragment.getPartition(0); // should be already computed?

		// create initial mapping
		final Graph<NodeType, EdgeType> subGraph = fragment.getSubGraph();
		final int mapping[] = new int[subGraph.getNodeCount()];
		for (int i = 0; i < mapping.length; i++) {
			mapping[i] = i;
		}

		return canRecursion(0, mapping);
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		// fragment.freeUnusedInfo();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#setFinalEmbeddings(java.util.Collection)
	 */
	@Override
	public void setFinalEmbeddings(
			final Collection<HPEmbedding<NodeType, EdgeType>> embs) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#setThreadNumber(int)
	 */
	@Override
	public void setThreadNumber(final int idx) {
		// FIXME
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.SearchLatticeNode#toHPFragment()
	 */
	@Override
	public HPFragment<NodeType, EdgeType> toHPFragment() {
		return fragment;
	}

	@Override
	public String toString() {
		// System.err.println("canonical code is: " + canonicalCode());
		return LocalEnvironment.env(this).serializer.serialize(fragment
				.getSubGraph());
	}

}