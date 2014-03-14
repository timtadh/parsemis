/**
 * created 13.06.2006
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
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.chain.MaxCliqueStep;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.miner.general.HPFragment;
import de.parsemis.miner.general.HPFragmentWrapper;
import de.parsemis.utils.FrequentedArrayList;
import de.parsemis.utils.FrequentedCollection;
import de.parsemis.utils.GraphUtils;
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
public class DAGmFragment<NodeType, EdgeType> extends
		FrequentedArrayList<HPEmbedding<NodeType, EdgeType>> implements
		HPFragment<NodeType, EdgeType> {

	public enum LastAction {
		INSERTED_NODE, INSERTED_EDGE, INSERTED_BOTH, STARTED_LEVEL, UNKNOWN
	}

	private class StartPartition {
		int indegree, outdegree;

		NodeType label;

		public StartPartition(final int in, final int out, final NodeType label) {
			this.indegree = in;
			this.outdegree = out;
			this.label = label;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof DAGmFragment.StartPartition)) {
				return false;
			}
			final StartPartition other = (StartPartition) o;
			return (this.indegree == other.indegree
					&& this.outdegree == other.outdegree && this.label
					.equals(other.label));
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static int UNKNOWN_PARTITION = -1;

	Graph<NodeType, EdgeType> fragment;

	private HPGraph<NodeType, EdgeType> hpFragment;

	private final Frequency freq;

	final BitSet graphSet;;

	private final Collection<HPEmbedding<NodeType, EdgeType>> embeddings;

	public final DAGmFragment<NodeType, EdgeType> me;

	// stores to each subgraph node index (index of the array) the corresponding
	// topological level in the subgraph (value)
	private int[] nodeLevel;

	public int[] partitionNumber;

	private LastAction lastAction;

	private int lastCreatingNodeIndex;

	private int lastEdgeCreatingNodeIndex;

	private transient FrequentedCollection<HPEmbedding<NodeType, EdgeType>> mc;

	transient Fragment<NodeType, EdgeType> frag = null;

	public DAGmFragment(final Graph<NodeType, EdgeType> graph,
			final int[] nodeLevel) {
		super(LocalEnvironment.env(graph).newFrequency());
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);

		this.fragment = graph;
		this.hpFragment = graph.toHPGraph();
		this.graphSet = new BitSet(env.graphCount());
		this.freq = env.newFrequency();
		this.nodeLevel = nodeLevel;

		this.embeddings = new ArrayList<HPEmbedding<NodeType, EdgeType>>();

		lastAction = LastAction.UNKNOWN;

		partitionNumber = new int[nodeLevel.length];
		for (int i = 0; i < partitionNumber.length; i++) {
			partitionNumber[i] = UNKNOWN_PARTITION;
		}

		this.me = this;
	}

	public DAGmFragment(final HPGraph<NodeType, EdgeType> graph,
			final int[] nodeLevel) {
		super(LocalEnvironment.env(graph).newFrequency());
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);

		this.hpFragment = graph;
		this.fragment = graph.toGraph();
		this.graphSet = new BitSet(env.graphCount());
		this.freq = env.newFrequency();
		this.nodeLevel = nodeLevel;

		this.embeddings = new ArrayList<HPEmbedding<NodeType, EdgeType>>();

		lastAction = LastAction.UNKNOWN;

		partitionNumber = new int[nodeLevel.length];
		for (int i = 0; i < partitionNumber.length; i++) {
			partitionNumber[i] = UNKNOWN_PARTITION;
		}

		this.me = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#add(de.parsemis.miner.DataBaseGraph)
	 */
	public void add(final DataBaseGraph<NodeType, EdgeType> graph)
			throws UnsupportedOperationException {
		if (LocalEnvironment.env(this).embeddingBased) {
			throw new UnsupportedOperationException(
					"add only Database graph is not allowed with embeddingBased mining");
		} else {
			final int idx = graph.getIndex();
			if (graphSet.get(idx)) {
				return;
			} else {
				graphSet.set(idx);
				freq.add(graph.frequency()); // FIXME embeddings vs.
				// fragment?
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(E)
	 */
	@Override
	public boolean add(final HPEmbedding<NodeType, EdgeType> arg0) {
		if (LocalEnvironment.env(this).embeddingBased) {
			if (fragment == null) {
				hpFragment = arg0.getSubGraph();
				fragment = hpFragment.toGraph();
			}

			if (super.add(arg0)) {
				mc = null;
				graphSet.set(arg0.getDataBaseGraph().getIndex());
				return true;
			}
			System.err.println("ups - adding failed");

			return false;
		} else {
			if (fragment == null) {
				fragment = arg0.getSubGraph().toGraph();
				hpFragment = arg0.getSubGraph();
			}
			embeddings.add(arg0);

			this.add(arg0.getDataBaseGraph());

			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(
			final Collection<? extends HPEmbedding<NodeType, EdgeType>> arg0) {
		return embeddings.addAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		graphSet.clear();
		embeddings.clear();
		freq.sub(freq); // FIXME freq.clear()?
		fragment = null;
	}

	private void computePartitions() {
		int partitionCount = 0;
		final boolean debug = false;
		final int nodeCount = hpFragment.getNodeCount();

		final HashMap<StartPartition, Integer> partitionTable = new HashMap<StartPartition, Integer>();
		for (int i = 0; i < nodeCount; i++) {
			final StartPartition tmp = new StartPartition(hpFragment
					.getInDegree(i), hpFragment.getOutDegree(i), hpFragment
					.getNodeLabel(i));
			if (partitionTable.containsKey(tmp)) {
				partitionNumber[i] = partitionTable.get(tmp);
			} else {
				partitionNumber[i] = partitionCount;
				partitionCount++;
				partitionTable.put(tmp, partitionNumber[i]);
			}
		}

		boolean changed = true;
		do {

			changed = false;
			for (int i = 0; (!changed) && i < nodeCount; i++) {
				for (int j = i + 1; (!changed) && j < nodeCount; j++) {
					if (partitionNumber[i] != partitionNumber[j]) {
						continue;
					}

					ArrayList<Integer> otherPartitions = new ArrayList<Integer>();
					IntIterator edgeIt = hpFragment.getInEdgeIndices(i);
					while (edgeIt.hasNext()) {
						final int actEdge = edgeIt.next();
						final int otherNodeIdx = hpFragment.getOtherNode(
								actEdge, i);
						otherPartitions.add(getPartition(otherNodeIdx));
					}

					boolean samePart = true;
					edgeIt = hpFragment.getInEdgeIndices(j);
					while (edgeIt.hasNext()) {
						final int actEdge = edgeIt.next();
						final int otherNodeIdx = hpFragment.getOtherNode(
								actEdge, j);
						if (!otherPartitions
								.contains(getPartition(otherNodeIdx))) {
							samePart = false;
							break;
						}
						otherPartitions
								.remove((Integer) getPartition(otherNodeIdx));
					}

					if (samePart) {
						otherPartitions = new ArrayList<Integer>();
						edgeIt = hpFragment.getOutEdgeIndices(i);
						while (edgeIt.hasNext()) {
							final int actEdge = edgeIt.next();
							final int otherNodeIdx = hpFragment.getOtherNode(
									actEdge, i);
							otherPartitions.add(getPartition(otherNodeIdx));
						}

						samePart = true;
						edgeIt = hpFragment.getOutEdgeIndices(j);
						while (edgeIt.hasNext()) {
							final int actEdge = edgeIt.next();
							final int otherNodeIdx = hpFragment.getOtherNode(
									actEdge, j);
							if (!otherPartitions
									.contains(getPartition(otherNodeIdx))) {
								samePart = false;
								break;
							}
							otherPartitions
									.remove((Integer) getPartition(otherNodeIdx));
						}
						if (samePart) {
							continue;
						}
					}

					final ArrayList<Integer> newPartition = new ArrayList<Integer>();
					newPartition.add(i);
					changed = true;

					for (int k = i + 1; k < fragment.getNodeCount(); k++) {
						if (partitionNumber[j] != partitionNumber[k]) {
							continue;
						}
						if (j == k) {
							continue;
						}

						otherPartitions = new ArrayList<Integer>();
						edgeIt = hpFragment.getInEdgeIndices(i);
						while (edgeIt.hasNext()) {
							final int actEdge = edgeIt.next();
							final int otherNodeIdx = hpFragment.getOtherNode(
									actEdge, i);
							otherPartitions.add(getPartition(otherNodeIdx));
						}

						samePart = true;
						edgeIt = hpFragment.getInEdgeIndices(k);
						while (edgeIt.hasNext()) {
							final int actEdge = edgeIt.next();
							final int otherNodeIdx = hpFragment.getOtherNode(
									actEdge, k);
							if (!otherPartitions
									.contains(getPartition(otherNodeIdx))) {
								samePart = false;
								break;
							}
							otherPartitions
									.remove((Integer) getPartition(otherNodeIdx));
						}

						if (!samePart) {
							continue;
						}

						otherPartitions = new ArrayList<Integer>();
						edgeIt = hpFragment.getOutEdgeIndices(i);
						while (edgeIt.hasNext()) {
							final int actEdge = edgeIt.next();
							final int otherNodeIdx = hpFragment.getOtherNode(
									actEdge, i);
							otherPartitions.add(getPartition(otherNodeIdx));
						}

						samePart = true;
						edgeIt = hpFragment.getOutEdgeIndices(k);
						while (edgeIt.hasNext()) {
							final int actEdge = edgeIt.next();
							final int otherNodeIdx = hpFragment.getOtherNode(
									actEdge, k);
							if (!otherPartitions
									.contains(getPartition(otherNodeIdx))) {
								samePart = false;
								break;
							}
							otherPartitions
									.remove((Integer) getPartition(otherNodeIdx));
						}

						if (!samePart) {
							continue;
						}

						newPartition.add(k);
					}

					final Iterator<Integer> newIt = newPartition.iterator();
					while (newIt.hasNext()) {
						final int newPart = newIt.next();
						partitionNumber[newPart] = partitionCount;
					}
					partitionCount++;
				}
			}

		} while (changed);

		if (debug) {
			System.err.println("================================");
			System.err.println(LocalEnvironment.env(this).serializer
					.serialize(fragment));

			for (int i = 0; i < fragment.getNodeCount(); i++) {
				System.err.println("Node " + fragment.getNode(i).getLabel()
						+ " is in partition " + partitionNumber[i]);
			}

			System.err.println("================================");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object arg0) {
		return embeddings.contains(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(final Collection<?> arg0) {
		return embeddings.containsAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#copy()
	 */
	public HPFragment<NodeType, EdgeType> copy() {
		// FIXME
		return new DAGmFragment<NodeType, EdgeType>(hpFragment.clone(),
				nodeLevel.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#embeddingToFragmentEdge(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> embeddingToFragmentEdge(
			final Embedding<NodeType, EdgeType> emb,
			final Edge<NodeType, EdgeType> embeddingEdge) {
		assert (contains(emb));
		return embeddingEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#embeddingToFragmentNode(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> embeddingToFragmentNode(
			final Embedding<NodeType, EdgeType> emb,
			final Node<NodeType, EdgeType> embeddingNode) {
		assert (contains(emb));
		return embeddingNode;
	}

	public void finalizeIt() {
		for (final HPEmbedding<NodeType, EdgeType> emb : this) {
			emb.freeTransient();
		}
		mc = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#fragmentToEmbeddingEdge(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> fragmentToEmbeddingEdge(
			final Embedding<NodeType, EdgeType> emb,
			final Edge<NodeType, EdgeType> fragmentEdge) {
		assert (contains(emb));
		return fragmentEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#fragmentToEmbeddingNode(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> fragmentToEmbeddingNode(
			final Embedding<NodeType, EdgeType> emb,
			final Node<NodeType, EdgeType> fragmentNode) {
		assert (contains(emb));
		return fragmentNode;
	}

	public void freeUnusedInfo() {
		mc = null;
		partitionNumber = null;
		nodeLevel = null;
		for (final HPEmbedding<NodeType, EdgeType> actEmbed : embeddings) {
			final DAGmHPEmbedding<NodeType, EdgeType> embed = (DAGmHPEmbedding<NodeType, EdgeType>) actEmbed;
			embed.freeUnusedInfo();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	@Override
	public Frequency frequency() {
		if (LocalEnvironment.env(this).embeddingBased) {
			return mc().frequency();
		} else {
			return freq;
		}
	}

	public Node<NodeType, EdgeType> getEdgeLastCreatingNode() {
		return fragment.getNode(lastEdgeCreatingNodeIndex);
	}

	public int getEdgeLastCreatingNodeIndex() {
		return lastEdgeCreatingNodeIndex;
	}

	public Collection<HPEmbedding<NodeType, EdgeType>> getEmbeddings() {
		return embeddings;
	}

	public HPGraph<NodeType, EdgeType> getHPSubGraph() {
		return hpFragment;
	}

	public LastAction getLastAction() {
		return lastAction;
	}

	public Node<NodeType, EdgeType> getLastCreatingNode() {
		return fragment.getNode(lastCreatingNodeIndex);
	}

	public int getLastCreatingNodeIndex() {
		return lastCreatingNodeIndex;
	}

	public Node<NodeType, EdgeType> getLastNode() {
		// FIXME so einfach?
		return fragment.getNode(fragment.getNodeCount() - 1);
	}

	/**
	 * @param subNodeIndex
	 * @return the topological level of the given *sub*Node
	 */
	public int getLevel(final int subNodeIndex) {
		return nodeLevel[subNodeIndex];
	}

	/**
	 * @param subNode
	 * @return the topological level of the given *sub*Node
	 */
	public int getLevel(final Node<NodeType, EdgeType> subNode) {
		return nodeLevel[subNode.getIndex()];
	}

	public Collection<HPEmbedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet() {
		return mc();
	}

	public int[] getNodeLevels() {
		return nodeLevel;
	}

	/**
	 * @param level
	 * @return every node on the given topological level
	 */
	public Collection<Node<NodeType, EdgeType>> getNodesAtLevel(final int level) {
		final ArrayList<Node<NodeType, EdgeType>> ret = new ArrayList<Node<NodeType, EdgeType>>();
		for (int i = 0; i < nodeLevel.length; i++) {
			if (nodeLevel[i] == level) {
				ret.add(fragment.getNode(i));
			}
		}

		return ret;
	}

	public int getPartition(final int nodeIndex) {
		if (partitionNumber[nodeIndex] != UNKNOWN_PARTITION) {
			return partitionNumber[nodeIndex];
		}

		computePartitions();

		return partitionNumber[nodeIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#toGraph()
	 */
	public Graph<NodeType, EdgeType> getSubGraph() {
		return fragment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#graphIterator()
	 */
	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator() {
		return new Iterator<DataBaseGraph<NodeType, EdgeType>>() {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(fragment);

			int next = graphSet.nextSetBit(0);

			public boolean hasNext() {
				if (next >= 0) {
					return true;
				} else {
					/*
					 * System.out.println("this fragment has " +
					 * embeddings.size() + " embeddings:"); for (Embedding<NodeType,EdgeType>
					 * actEmb : embeddings) {
					 * System.out.println(actEmb.getDataBaseGraph().toGraph().getName()); }
					 */
					return false;
				}
			}

			public DataBaseGraph<NodeType, EdgeType> next() {
				final DataBaseGraph<NodeType, EdgeType> ret = env
						.getGraph(next);
				next = graphSet.nextSetBit(next + 1);
				return ret;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractList#hashCode()
	 */
	@Override
	public int hashCode() {
		return fragment == null ? 0 : fragment.hashCode();
	}

	public boolean hasSingleDuplicate(final ArrayList<Integer> arrayA,
			final ArrayList<Integer> arrayB) {
		final Iterator<Integer> indexIt = arrayA.iterator();
		while (indexIt.hasNext()) {
			final Integer actIndex = indexIt.next();
			if (arrayB.contains(actIndex)) {
				return true;
			}
		}
		return false;
	}

	public boolean isConnected() {
		return GraphUtils.isConnected(fragment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return (size() == 0);
	}

	public boolean isSameComponent(final ArrayList<Integer> arrayA,
			final ArrayList<Integer> arrayB) {
		if (hasSingleDuplicate(arrayA, arrayB)) {
			return true;
		}
		if (isSameComponent(arrayA, arrayB, true)) {
			return true;
		}
		if (isSameComponent(arrayA, arrayB, false)) {
			return true;
		}

		return false;
	}

	public boolean isSameComponent(final ArrayList<Integer> arrayA,
			final ArrayList<Integer> arrayB, final boolean up) {
		if (arrayA.size() == 0 || arrayB.size() == 0) {
			return false;
		}
		if (hasSingleDuplicate(arrayA, arrayB)) {
			return true;
		}

		final ArrayList<Integer> nextNodesA = new ArrayList<Integer>();
		final ArrayList<Integer> nextNodesB = new ArrayList<Integer>();

		Iterator<Integer> indexIt = arrayA.iterator();
		while (indexIt.hasNext()) {
			final Integer actIndex = indexIt.next();

			Iterator<Edge<NodeType, EdgeType>> edgeIt;
			if (up) {
				edgeIt = fragment.getNode(actIndex).incommingEdgeIterator();
			} else {
				edgeIt = fragment.getNode(actIndex).outgoingEdgeIterator();
			}

			while (edgeIt.hasNext()) {
				final Edge<NodeType, EdgeType> actEdge = edgeIt.next();

				nextNodesA.add(actEdge.getOtherNode(fragment.getNode(actIndex))
						.getIndex());
			}
		}

		indexIt = arrayB.iterator();
		while (indexIt.hasNext()) {
			final Integer actIndex = indexIt.next();

			Iterator<Edge<NodeType, EdgeType>> edgeIt;
			if (up) {
				edgeIt = fragment.getNode(actIndex).incommingEdgeIterator();
			} else {
				edgeIt = fragment.getNode(actIndex).outgoingEdgeIterator();
			}

			while (edgeIt.hasNext()) {
				final Edge<NodeType, EdgeType> actEdge = edgeIt.next();

				nextNodesB.add(actEdge.getOtherNode(fragment.getNode(actIndex))
						.getIndex());
			}
		}

		if (isSameComponent(nextNodesA, nextNodesB, up)) {
			return true;
		}

		return false;
	}

	public boolean isSuccessor(final int child, final int parent) {
		if (parent == child) {
			return true;
		}

		final Iterator<Edge<NodeType, EdgeType>> edgeIt = fragment.getNode(
				parent).outgoingEdgeIterator();
		while (edgeIt.hasNext()) {
			final Edge<NodeType, EdgeType> actEdge = edgeIt.next();
			final Node<NodeType, EdgeType> actNode = actEdge
					.getOtherNode(fragment.getNode(parent));
			if (isSuccessor(child, actNode.getIndex())) {
				return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<HPEmbedding<NodeType, EdgeType>> iterator() {
		if (LocalEnvironment.env(this).embeddingBased) {
			return super.iterator();
		} else {
			return embeddings.iterator();
		}
	}

	private final FrequentedCollection<HPEmbedding<NodeType, EdgeType>> mc() {
		if (mc == null) {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(this);
			mc = new FrequentedArrayList<HPEmbedding<NodeType, EdgeType>>(env
					.newFrequency());

			MaxCliqueStep.findHPMaxClique(this, env.ignoreNodes, mc);
		}
		return mc;
	}

	public void release(final ThreadEnvironment<NodeType, EdgeType> target) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object arg0) {
		return super.remove(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException(
				"removeAll is not yet supported for DAGmFragment");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException(
				"retainAll is not yet supported for DAGmFragment");
	}

	public boolean samePartition(final int nodeIndexA, final int nodeIndexB) {
		if (nodeIndexA < 0 || nodeIndexB < 0) {
			return false;
		}
		if (!hpFragment.getNodeLabel(nodeIndexA).equals(
				hpFragment.getNodeLabel(nodeIndexB))) {
			return false;
		}

		return getPartition(nodeIndexA) == getPartition(nodeIndexB);
	}

	/*
	 * public Collection<Embedding<NodeType, EdgeType>>
	 * getMaximalNonOverlappingSubSet() { throw new
	 * UnsupportedOperationException("getMaximalNonOverlappingSubSet not yet
	 * implemented"); }
	 */

	public void setLastAction(final LastAction lastAction) {
		this.lastAction = lastAction;
	}

	public void setLastCreatingNode(final int lastCreatingNodeIndex) {
		this.lastCreatingNodeIndex = lastCreatingNodeIndex;
	}

	public void setLastEdgeCreatingNode(final int lastEdgeCreatingNodeIndex) {
		this.lastEdgeCreatingNodeIndex = lastEdgeCreatingNodeIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		if (LocalEnvironment.env(this).embeddingBased) {
			return super.size();
		} else {
			return embeddings.size();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		return toArray(new Object[size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(final T[] arg0) {
		if (LocalEnvironment.env(this).embeddingBased) {
			return super.toArray(arg0);
		} else {
			int i = 0;

			for (final T emb : (Collection<T>) embeddings) {
				arg0[i] = emb;
				i++;
			}

			return arg0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#toFragment()
	 */
	public Fragment<NodeType, EdgeType> toFragment() {
		if (frag == null) {
			frag = new HPFragmentWrapper<NodeType, EdgeType>(this);
		}
		return frag;
	}

	public HPGraph<NodeType, EdgeType> toHPGraph() {
		return hpFragment;
	}

}