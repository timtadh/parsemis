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

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.miner.general.HPEmbeddingWrapper;
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

// FIXME don't use me :-/
public class DAGmHPEmbedding<NodeType, EdgeType> implements
		HPEmbedding<NodeType, EdgeType>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DAGmGraph<NodeType, EdgeType> dbGraph;

	private HPGraph<NodeType, EdgeType> dbHPGraph;

	// private Graph<NodeType,EdgeType> subGraph;
	private HPGraph<NodeType, EdgeType> hpSubGraph;

	// stores to each subgraph node index (index of the array) the corresponding
	// supergraph node index (value)
	private int[] superNodes;

	// used Nodes of the superGraph
	private BitSet usedNodes;

	transient Embedding<NodeType, EdgeType> emb = null;

	public boolean freeSuperEdge(final int superEdge) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public boolean freeSuperNode(final int superNode) {
		return !isUsed(superNode);
	}

	public void freeTransient() {
		// TODO Auto-generated method stub

	}

	public void freeUnusedInfo() {
		usedNodes = null;
	}

	public Frequency frequency() {
		return this.getDataBaseGraph().frequency();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getDataBaseGraph()
	 */
	public DataBaseGraph<NodeType, EdgeType> getDataBaseGraph() {
		return dbGraph;
	}

	public HPGraph<NodeType, EdgeType> getHPSubGraph() {
		return hpSubGraph;
	}

	public HPGraph<NodeType, EdgeType> getHPSuperGraph() {
		return dbHPGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSubGraph()
	 */
	public HPGraph<NodeType, EdgeType> getSubGraph() {
		return hpSubGraph;
	}

	public int getSubGraphEdge(final int superEdge) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public int getSubGraphNode(final int superNodeIndex) {
		for (int i = 0; i < superNodes.length; i++) {
			if (superNodes[i] == superNodeIndex) {
				return i;
			}
		}

		return HPGraph.NO_NODE;
	}

	public int getSubGraphNodeIndex(final int superNodeIndex) {
		for (int i = 0; i < superNodes.length; i++) {
			if (superNodes[i] == superNodeIndex) {
				return i;
			}
		}
		return HPGraph.NO_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraph()
	 */
	public HPGraph<NodeType, EdgeType> getSuperGraph() {
		return dbHPGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraphEdge(de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> getSuperGraphEdge(
			final Edge<NodeType, EdgeType> subEdge) {
		return getSuperGraph().toGraph().getEdge(
				getSuperGraphNode(subEdge.getNodeA()),
				getSuperGraphNode(subEdge.getNodeB()));
	}

	public int getSuperGraphEdge(final int subEdge) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraphNode(de.parsemis.graph.Node)
	 */
	public int getSuperGraphNode(final int subNodeIndex) {
		return superNodes[subNodeIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraphNode(de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> getSuperGraphNode(
			final Node<NodeType, EdgeType> subNode) {
		return getSuperGraph().toGraph()
				.getNode(superNodes[subNode.getIndex()]);
	}

	public int getSuperGraphNodeIndex(final int subNodeIndex) {
		return superNodes[subNodeIndex];
	}

	public int[] getSuperNodes() {
		return superNodes;
	}

	public boolean isNeverConnectable(
			final DAGmFragment<NodeType, EdgeType> fragment) {
		final HashSet<Integer> rootNodes = new HashSet<Integer>();
		final HashMap<Integer, HashSet<Integer>> reachable = new HashMap<Integer, HashSet<Integer>>();

		for (int i = 0; i < hpSubGraph.getNodeCount(); i++) {
			if (fragment.getLevel(i) > 1) {
				break;
			}
			// root node
			rootNodes.add(getSuperGraphNode(i));
			final HashSet<Integer> tmp = new HashSet<Integer>();
			reachable.put(getSuperGraphNode(i), tmp);
		}

		for (final Integer actNode : rootNodes) {
			reachNeighbours(actNode, reachable.get(actNode), rootNodes);
		}

		boolean changed = true;
		while (changed) {
			changed = false;
			if (rootNodes.size() == 1) {
				return false;
			}
			final Iterator<Integer> it = rootNodes.iterator();
			final int firstRoot = it.next();
			final HashSet<Integer> firstSet = reachable.get(firstRoot);

			while (it.hasNext()) {
				final int otherRoot = it.next();
				for (final Integer actNode : reachable.get(otherRoot)) {
					if (firstSet.contains(actNode)) {
						firstSet.addAll(reachable.get(otherRoot));
						rootNodes.remove(otherRoot);
						changed = true;
						break;
					}
				}
				if (changed) {
					break;
				}
			}
		}

		return true;
	}

	public boolean isUsed(final int superGraphNodeIdx) {
		return usedNodes.get(superGraphNodeIdx);
	}

	public boolean overlaps(final HPEmbedding<NodeType, EdgeType> other,
			final Collection<NodeType> ignore) {
		if (other.getDataBaseGraph() != this.getDataBaseGraph()) {
			return false;
		}

		for (int i = 0; i < superNodes.length; ++i) {
			if (other.getSubGraphNode(superNodes[i]) != HPGraph.NO_NODE) {
				if (ignore == null
						|| !ignore.contains(getSuperGraph().getNodeLabel(
								getSuperGraphNode(i)))) {
					return true;
				}
			}
		}

		return false;
	}

	private void reachNeighbours(final int from,
			final HashSet<Integer> reached, final HashSet<Integer> ignore) {
		final IntIterator outgoing = dbHPGraph.getOutEdgeIndices(from);
		while (outgoing.hasNext()) {
			final int actEdge = outgoing.next();
			final int otherNode = dbHPGraph.getOtherNode(actEdge, from);

			if (ignore.contains(otherNode)) {
				continue;
			}

			reached.add(otherNode);
			reachNeighbours(otherNode, reached, ignore);
		}
	}

	public void release(final ThreadEnvironment<NodeType, EdgeType> tenv) {
		// TODO Auto-generated method stub

	}

	public DAGmHPEmbedding<NodeType, EdgeType> set(
			final DAGmGraph<NodeType, EdgeType> dbGraph,
			final Graph<NodeType, EdgeType> fragment, final int[] superNodes) {
		this.dbGraph = dbGraph;
		this.dbHPGraph = dbGraph.toHPGraph();
		// this.subGraph = fragment;
		this.hpSubGraph = fragment.toHPGraph();
		this.superNodes = superNodes;
		this.usedNodes = new BitSet(dbHPGraph.getNodeCount());
		for (final int actNodeId : superNodes) {
			usedNodes.set(actNodeId);
		}
		return this;
	}

	public DAGmHPEmbedding<NodeType, EdgeType> set(
			final DAGmGraph<NodeType, EdgeType> dbGraph,
			final HPGraph<NodeType, EdgeType> fragment, final int[] superNodes) {
		this.dbGraph = dbGraph;
		this.dbHPGraph = dbGraph.toHPGraph();
		// this.subGraph = fragment.toGraph();
		this.hpSubGraph = fragment;
		this.superNodes = superNodes;
		this.usedNodes = new BitSet(dbHPGraph.getNodeCount());
		for (final int actNodeId : superNodes) {
			usedNodes.set(actNodeId);
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#toEmbedding()
	 */
	public Embedding<NodeType, EdgeType> toEmbedding() {
		if (emb == null) {
			emb = new HPEmbeddingWrapper<NodeType, EdgeType>(this);
		}
		return emb;
	}
}
