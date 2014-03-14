/**
 * created Aug 17, 2006
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

import java.util.BitSet;
import java.util.HashMap;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.Relabler;

/**
 * 
 * This class contains several static functions that do various computations on
 * graphs
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public final class GraphUtils {
	private static class NodeComparator<NodeType, EdgeType> implements
			IntComparator {

		final HPGraph<NodeType, EdgeType> graph;

		final int[] nodelabels;

		final int[] edgelabels;

		protected NodeComparator(final HPGraph<NodeType, EdgeType> graph,
				final Relabler<NodeType, EdgeType> rel) {
			this.graph = graph;
			nodelabels = new int[graph.getMaxNodeIndex()];
			for (int i = nodelabels.length - 1; i >= 0; i--) {
				if (graph.isValidNode(i)) {
					nodelabels[i] = graph.getNodeLabelIndex(i, rel);
				}
			}
			edgelabels = new int[graph.getMaxEdgeIndex()];
			for (int i = edgelabels.length - 1; i >= 0; i--) {
				if (graph.isValidEdge(i)) {
					edgelabels[i] = graph.getEdgeLabelIndex(i, rel);
				}
			}
		}

		public int compare(final int node1, final int node2) {
			int diff = nodelabels[node1] - nodelabels[node2];
			if (diff != 0) {
				return diff;
			}

			diff = graph.getDegree(node1) - graph.getDegree(node2);
			if (diff != 0) {
				return diff;
			}

			int edgeProd1 = 1, edgeProd2 = 1;
			for (int i = graph.getDegree(node1) - 1; i >= 0; i--) {
				final int edge = graph.getNodeEdge(node1, i);
				edgeProd1 *= Math.PRIMES[edgelabels[edge] % Math.PRIMES.length];

				final int edge2 = graph.getNodeEdge(node2, i);
				edgeProd2 *= Math.PRIMES[edgelabels[edge2] % Math.PRIMES.length];
			}
			diff = edgeProd1 - edgeProd2;
			if (diff != 0) {
				return diff;
			}

			int neighbourProd1 = 1, neighbourProd2 = 1;
			for (int i = graph.getDegree(node1) - 1; i >= 0; i--) {
				final int neighbour = graph.getNodeNeigbour(node1, i);
				neighbourProd1 *= Math.PRIMES[nodelabels[neighbour]
						% Math.PRIMES.length];

				final int neighbour2 = graph.getNodeNeigbour(node2, i);
				neighbourProd2 *= Math.PRIMES[nodelabels[neighbour2]
						% Math.PRIMES.length];
			}

			return neighbourProd1 - neighbourProd2;
		}

	}

	private enum NodeState {
		INACTIVE, ACTIVE, VISITED
	}

	private static class PartitionComparator<NodeType, EdgeType> implements
			IntComparator {
		final HPGraph<NodeType, EdgeType> graph;

		int[] partitionNumbers;

		protected PartitionComparator(final HPGraph<NodeType, EdgeType> graph,
				final int[] partitionNumbers) {
			this.graph = graph;
			this.partitionNumbers = partitionNumbers;
		}

		public int compare(final int node1, final int node2) {
			int prod1 = 1, prod2 = 1;

			for (int i = graph.getDegree(node1) - 1; i >= 0; i--) {
				final int neighbour = graph.getNodeNeigbour(node1, i);
				prod1 *= Math.PRIMES[partitionNumbers[neighbour]
						% Math.PRIMES.length];
			}

			for (int i = graph.getDegree(node2) - 1; i >= 0; i--) {
				final int neighbour = graph.getNodeNeigbour(node2, i);
				prod2 *= Math.PRIMES[partitionNumbers[neighbour]
						% Math.PRIMES.length];
			}

			return prod1 - prod2;
		}

		protected void setPartitionNumbers(final int[] partitionNumbers) {
			this.partitionNumbers = partitionNumbers;
		}

	}

	public final static int NO_PARTITION = -1;

	private final static int UNVISITED = 0, EDGE_VISITED = 1, BRIDGE = 2,
			BACKEDGE = 3;

	/*
	 * for refining partitions
	 * 
	 * create a set containing all edge informations of the given node
	 */
	private final static <NodeType, EdgeType> SortedMultiSet<ThreeTuple> computeEdgeSet(
			final HPGraph<NodeType, EdgeType> graph, final int node,
			final int[] partNumbers, final Relabler<NodeType, EdgeType> env) {
		final SortedMultiSet<ThreeTuple> ret = new SortedMultiSet<ThreeTuple>();

		// read edges of node
		final IntIterator edgeIt = graph.getEdgeIndices(node);
		while (edgeIt.hasNext()) {
			final int actEdge = edgeIt.next();
			final int otherNodeIdx = graph.getOtherNode(actEdge, node);
			ret.add(new ThreeTuple(partNumbers[otherNodeIdx], graph
					.getDirection(actEdge, node), graph.getEdgeLabelIndex(
					actEdge, env)));
		}

		return ret;
	}

	/**
	 * computes the node partitioning of a graph by iterative refinement
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 * @param graph
	 *            the watched graph
	 * @param rel
	 *            Relabeler for ordering the nodes and edge labels
	 * @return an int-array containing the partition number for each node at the
	 *         corresponding index
	 */
	public static <NodeType, EdgeType> int[] computePartitions(
			final HPGraph<NodeType, EdgeType> graph,
			final Relabler<NodeType, EdgeType> rel) {
		final HPGraph<NodeType, EdgeType> hpFragment = graph;
		final int[] parMembers = new int[hpFragment.getNodeCount()];
		final int[] partitionNumbers = new int[hpFragment.getMaxNodeIndex()];
		final int lastPartition = initializePartitionNumbers(hpFragment,
				partitionNumbers, parMembers, rel);
		return GraphUtils.refinePartitions(hpFragment, rel, partitionNumbers,
				parMembers, lastPartition + 1);
	}

	public static <NodeType, EdgeType> int[] computePartitions2(
			final HPGraph<NodeType, EdgeType> g,
			final Relabler<NodeType, EdgeType> rel) {
		/*
		 * if the nodes are in three partitions that look like this 0 1 2 3 4 5
		 * 6 7 8 (the numbers are the indexes in nodes[]) then boundaries[] will
		 * look like this { -1, 3, 6, 8, 0, 0, 0, 0, 0, 0 } and partitions is 3.
		 */
		final int[] nodes = new int[g.getNodeCount()];

		// the end index of each partition is at boundaries[partition]
		int[] boundaries = new int[nodes.length + 1];
		int[] newBoundaries = new int[boundaries.length];

		// stores the number of the partition a node is in
		int[] partitionNumbers = new int[g.getMaxNodeIndex()];
		int[] newPartitionNumbers = new int[partitionNumbers.length];

		int partitions = 1;

		final NodeComparator<NodeType, EdgeType> comp1 = new NodeComparator<NodeType, EdgeType>(
				g, rel);
		final PartitionComparator<NodeType, EdgeType> comp2 = new PartitionComparator<NodeType, EdgeType>(
				g, partitionNumbers);

		// create partitions based on degree, edge labels and neighbor node
		// labels
		for (int i = 0, j = 0; i < partitionNumbers.length; i++) {
			if (g.isValidNode(i)) {
				nodes[j++] = i;
			} else {
				partitionNumbers[i] = newPartitionNumbers[i] = NO_PARTITION;
			}
		}
		Permutations.quickSort(nodes, comp1);

		boundaries[0] = -1;
		partitionNumbers[nodes[0]] = 1;
		for (int i = 1; i < nodes.length; i++) {
			if (comp1.compare(nodes[i - 1], nodes[i]) != 0) {
				boundaries[partitions++] = i - 1;
			}
			partitionNumbers[nodes[i]] = partitions;
		}
		boundaries[partitions] = nodes.length - 1;

		// iteratively create partitions based on the neighbor nodes' partitions
		boolean changed = false;
		int newPartitions;
		do {
			newPartitions = 0;
			newBoundaries[0] = -1;
			for (int i = 1; i <= partitions; i++) {
				Permutations.quickSort(nodes, boundaries[i - 1] + 1,
						boundaries[i], comp2);

				newPartitions++;
				newPartitionNumbers[nodes[boundaries[i - 1] + 1]] = newPartitions;
				for (int k = boundaries[i - 1] + 2; k <= boundaries[i]; k++) {
					if (comp2.compare(nodes[k - 1], nodes[k]) != 0) {
						newBoundaries[newPartitions++] = k - 1;
					}
					newPartitionNumbers[nodes[k]] = newPartitions;
				}
				newBoundaries[newPartitions] = boundaries[i];
			}

			changed = (newPartitions != partitions);

			partitions = newPartitions;
			int[] temp = partitionNumbers;
			partitionNumbers = newPartitionNumbers;
			newPartitionNumbers = temp;
			comp2.setPartitionNumbers(partitionNumbers);
			temp = boundaries;
			boundaries = newBoundaries;
			newBoundaries = temp;
		} while (changed && (partitions < nodes.length));

		return partitionNumbers;

	}

	private static <NodeType, EdgeType> boolean dfs(final int actNode,
			final NodeState nodeStates[], final int startNode,
			final HPGraph<NodeType, EdgeType> originalGraph) {
		nodeStates[actNode] = NodeState.ACTIVE;

		final IntIterator edgeIt = originalGraph.getOutEdgeIndices(actNode);
		while (edgeIt.hasNext()) {
			final int actEdge = edgeIt.next();
			final int nextNode = originalGraph.getOtherNode(actEdge, actNode);

			if (nodeStates[nextNode] == NodeState.ACTIVE) {
				// cycle found
				return false;
			} else if (nodeStates[nextNode] == NodeState.VISITED) {
				// already processed
				continue;
			}

			if (!dfs(nextNode, nodeStates, startNode, originalGraph)) {
				return false;
			}
		}

		nodeStates[actNode] = NodeState.VISITED;
		return true;
	}

	/**
	 * Finds all edges in the given graph that are bridges. A bridge is an edge
	 * whose removal will create a new connected component, i.e. the graph will
	 * break apart.
	 * 
	 * @param graph
	 *            a graph
	 * @return an array of all edges that are bridges
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 */
	public static <NodeType, EdgeType> BitSet getWeakBridges(
			final HPGraph<NodeType, EdgeType> graph) {

		if (graph.getNodeCount() < 2) {
			return new BitSet(1);
		}

		final int[] dfsIndex = new int[graph.getMaxNodeIndex()];
		final int[] lowMark = new int[graph.getMaxNodeIndex()];
		final int[] edgeMarker = new int[graph.getMaxEdgeIndex()];

		int bridgeCount = 0;
		for (int i = graph.getMaxNodeIndex() - 1; i >= 0; i--) {
			if (graph.isValidNode(i) && dfsIndex[i] == UNVISITED) {
				bridgeCount += getWeakBridges(graph, i, dfsIndex, lowMark,
						edgeMarker, new MutableInteger(0));
			}
		}

		final BitSet ret = new BitSet(graph.getNodeCount());

		for (int i = 0; i < edgeMarker.length; i++) {
			ret.set(i, edgeMarker[i] == BRIDGE);
		}

		return ret;
	}

	/*
	 * Recursively determines the bridges in the graph.
	 */
	private static <NodeType, EdgeType> int getWeakBridges(
			final HPGraph<NodeType, EdgeType> graph, final int node,
			final int[] dfsIndex, final int[] lowMark, final int[] edgeMarker,
			final MutableInteger count) {

		dfsIndex[node] = count.inc();
		lowMark[node] = dfsIndex[node];

		int bridgeCount = 0;

		for (int i = graph.getDegree(node) - 1; i >= 0; i--) {
			final int edge = graph.getNodeEdge(node, i);
			final int neighbour = graph.getOtherNode(edge, node);

			if (dfsIndex[neighbour] == UNVISITED) {
				edgeMarker[edge] = EDGE_VISITED;

				bridgeCount += getWeakBridges(graph, neighbour, dfsIndex,
						lowMark, edgeMarker, count);

				if (lowMark[neighbour] < lowMark[node]) {
					lowMark[node] = lowMark[neighbour];
				}

				if (lowMark[neighbour] == dfsIndex[neighbour]) {
					edgeMarker[edge] = BRIDGE;
					bridgeCount++;
				}
			} else if (edgeMarker[edge] == UNVISITED) {
				edgeMarker[edge] = BACKEDGE;

				if (lowMark[neighbour] < lowMark[node]) {
					lowMark[node] = lowMark[neighbour];
				}
			}
		}

		return bridgeCount;
	}

	public static <NodeType, EdgeType> int hashValue(
			final HPGraph<NodeType, EdgeType> g,
			final Relabler<NodeType, EdgeType> r) {

		int code = (g.getNodeCount() << 24) ^ (g.getEdgeCount() << 16);
		for (int i = g.getNodeCount() - 1; i >= 0; i--) {
			code ^= g.getDegree(i) << ((8 + g.getNodeLabelIndex(i, r)) % 29);
		}

		for (int i = g.getEdgeCount() - 1; i >= 0; i--) {
			code ^= Math.PRIMES[g.getEdgeLabelIndex(i, r) % Math.PRIMES.length];
		}

		return code;

	}

	/*
	 * for partition computation
	 * 
	 * groups als nodes with same label, in- and out-degree into one partition
	 */
	private final static <NodeType, EdgeType> int initializePartitionNumbers(
			final HPGraph<NodeType, EdgeType> graph, final int[] partNumbers,
			final int[] partMemberCount, final Relabler<NodeType, EdgeType> env) {
		final HashMap<ThreeTuple, Integer> partitionTable = new HashMap<ThreeTuple, Integer>();
		int ackPart = 0;
		for (int i = 0; i < graph.getMaxNodeIndex(); i++) {
			if (graph.isValidNode(i)) {
				final ThreeTuple tmp = new ThreeTuple(graph.getInDegree(i),
						graph.getOutDegree(i), graph.getNodeLabelIndex(i, env));
				if (partitionTable.containsKey(tmp)) {
					partNumbers[i] = partitionTable.get(tmp);
				} else {
					partitionTable.put(tmp, partNumbers[i] = ackPart++);
				}
				partMemberCount[partNumbers[i]]++;
			} else {
				// invalid node is in no partition
				partNumbers[i] = NO_PARTITION;
			}
		}
		return ackPart - 1;
	}

	/**
	 * @param graph
	 * @return <code>true</code>, if the given graph is connected
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 */
	public static <NodeType, EdgeType> boolean isConnected(
			final Graph<NodeType, EdgeType> graph) {
		return isConnected(graph.toHPGraph());
	}

	/**
	 * @param graph
	 * @return <code>true</code>, if the given graph is connected
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 */
	public static <NodeType, EdgeType> boolean isConnected(
			final HPGraph<NodeType, EdgeType> graph) {
		if (graph.getNodeCount() < 2) {
			return true;
		}
		if (graph.getEdgeCount() == 0) {
			return false;
		}
		final BitSet todo = new BitSet(graph.getMaxNodeIndex());
		final BitSet done = new BitSet(graph.getMaxNodeIndex());
		int next = graph.edgeIndexIterator().next();
		for (todo.set(next); !todo.isEmpty(); next = todo.nextSetBit(0)) {
			todo.clear(next);
			for (int i = graph.getDegree(next) - 1; i >= 0; --i) {
				final int neibour = graph.getOtherNode(graph.getNodeEdge(next,
						i), next);
				todo.set(neibour, !done.get(neibour));
			}
			done.set(next);
		}
		return done.cardinality() == graph.getNodeCount();
	}

	/**
	 * @param graph
	 * @return <code>true</code>, if the given graph is a directed acyclic
	 *         graph
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 */
	public static <NodeType, EdgeType> boolean isDAG(
			final Graph<NodeType, EdgeType> graph) {
		return isDAG(graph.toHPGraph());
	}

	/**
	 * @param graph
	 * @return <code>true</code>, if the given graph is a directed acyclic
	 *         graph
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 */
	public static <NodeType, EdgeType> boolean isDAG(
			final HPGraph<NodeType, EdgeType> graph) {
		final NodeState nodeStates[] = new NodeState[graph.getNodeCount()];
		for (int i = 0; i < graph.getNodeCount(); i++) {
			nodeStates[i] = NodeState.INACTIVE;
		}

		// brute force DAG check
		final IntIterator nodeIt = graph.nodeIndexIterator();
		while (nodeIt.hasNext()) {
			final int actNode = nodeIt.next();

			if (nodeStates[actNode] != NodeState.INACTIVE) {
				continue;
			}

			// start dfs at this node
			if (!dfs(actNode, nodeStates, actNode, graph)) {
				return false;
			}
		}

		return true;
	}

	public static <NodeType, EdgeType> boolean isPath(
			final HPGraph<NodeType, EdgeType> graph) {
		if (graph.getNodeCount() < 2) {
			return true;
		}
		int sn = HPGraph.NO_NODE;
		int en = HPGraph.NO_NODE;
		final IntIterator nodeIt = graph.nodeIndexIterator();
		while (nodeIt.hasNext()) {
			final int actNode = nodeIt.next();
			final int d = graph.getDegree(actNode);
			if (d > 2 || d == 0) {
				return false;
			}
			if (d == 1) {
				if (sn == HPGraph.NO_NODE) {
					sn = actNode;
				} else if (en == HPGraph.NO_NODE) {
					en = actNode;
				} else {
					return false;
				}
			}
		}
		// TODO: check connections not just degree
		return true;
	}

	/**
	 * Iteratively refines the given partitions until the partitioning is stable
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 * @param graph
	 *            the watched graph
	 * @param rel
	 *            Relabeler for ordering the nodes and edge labels
	 * @param partNumbers
	 *            initial partitioning for all nodes
	 * @param partMemberCount
	 *            number of nodes in each partition
	 * @param nextPart
	 *            next free partition number
	 * @return the (original) array of partition numbers for each node
	 */
	private static <NodeType, EdgeType> int[] refinePartitions(
			final HPGraph<NodeType, EdgeType> graph,
			final Relabler<NodeType, EdgeType> rel, final int[] partNumbers,
			final int[] partMemberCount, int nextPart) {

		final int maxNodeIndex = graph.getMaxNodeIndex();
		final int nodeCount = graph.getNodeCount();
		// to store mapping partition -> nodes
		final int[][] partitionMembers = new int[maxNodeIndex][];
		// to store edge tuples for each node
		@SuppressWarnings("unchecked")
		final SortedMultiSet<ThreeTuple>[] edges = new SortedMultiSet[maxNodeIndex];

		int lastNextPart = -1;

		// refinement finished, if each node has its one partition or nothing
		// changed
		while (nextPart < nodeCount && nextPart != lastNextPart) {
			lastNextPart = nextPart;

			// generate edge sets for all nodes in divisible partitions
			for (int node = 0; node < maxNodeIndex; node++) {
				final int part = partNumbers[node];
				if (graph.isValidNode(node)
						&& (partitionMembers[part] != null || partMemberCount[part] > 1)) {
					if (partitionMembers[part] == null) {
						// first node of this partition
						partitionMembers[part] = new int[partMemberCount[part]];
					}
					// store node in reverse map
					final int pos = --partMemberCount[part];
					partitionMembers[part][pos] = node;
					// traverse edges
					edges[node] = computeEdgeSet(graph, node, partNumbers, rel);
				}
			}

			// check and split partitions
			for (int part = 0; part < lastNextPart; part++) {
				int[] mem = partitionMembers[part];
				// reset for next iteration;
				partitionMembers[part] = null;
				if (mem != null) {
					partMemberCount[part] = mem.length;
					int oldPart = part;
					do {
						final int thisPart = nextPart++;
						mem = splitPartition(mem, edges, partNumbers,
								partMemberCount, oldPart, thisPart, graph);
						oldPart = thisPart;
						if (partMemberCount[thisPart] == 0) {
							nextPart--;
						}
					} while (oldPart < nodeCount
							&& partMemberCount[oldPart] > 1);
				}
			}
		}

		return partNumbers;
	}

	/*
	 * for refining partitions
	 * 
	 * keeps the nodes "equal" to the first node of the partition in the
	 * partition an groups all other in the newly created partition
	 */
	private final static <NodeType, EdgeType> int[] splitPartition(
			final int[] members, final SortedMultiSet<ThreeTuple>[] edges,
			final int[] partNumbers, final int[] partMemberCount,
			final int curIdx, final int nextIdx,
			final HPGraph<NodeType, EdgeType> graph) {
		final int[] ret = new int[members.length];
		final int curCount = partMemberCount[curIdx];

		// keep first in current partition
		partMemberCount[curIdx] = 1;
		final SortedMultiSet<ThreeTuple> first = edges[members[0]];

		for (int i = 1; i < curCount; i++) {
			final int mem = members[i];
			if (first.compareTo(edges[mem]) == 0) {
				// keep member in current partition
				members[partMemberCount[curIdx]++] = mem;
			} else {
				// insert into new one
				ret[(partMemberCount[nextIdx]++)] = mem;
				partNumbers[mem] = nextIdx;
			}
		}

		return ret;
	}
}
