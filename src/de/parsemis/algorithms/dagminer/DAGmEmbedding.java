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

import java.util.BitSet;
import java.util.Collection;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.EmbeddingWrapper;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;

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
public class DAGmEmbedding<NodeType, EdgeType> implements
		Embedding<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DAGmGraph<NodeType, EdgeType> dbGraph;

	private HPGraph<NodeType, EdgeType> dbHPGraph;

	private Graph<NodeType, EdgeType> subGraph;

	private HPGraph<NodeType, EdgeType> hpSubGraph;

	// stores to each subgraph node index (index of the array) the corresponding
	// supergraph node index (value)
	private int[] superNodes;

	// used Nodes of the superGraph
	private BitSet usedNodes;

	private transient HPEmbedding<NodeType, EdgeType> hp;

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
		if (dbGraph == null) {
			// FIXME
			return null;
		} else {
			return dbGraph;
		}
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
	public Graph<NodeType, EdgeType> getSubGraph() {
		return subGraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSubGraphEdge(de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> getSubGraphEdge(
			final Edge<NodeType, EdgeType> superEdge) {
		final Node<NodeType, EdgeType> subNodeA = getSubGraphNode(superEdge
				.getNodeA());
		final Node<NodeType, EdgeType> subNodeB = getSubGraphNode(superEdge
				.getNodeB());
		if (subNodeA == null || subNodeB == null) {
			return null;
		} else {
			return getSubGraph().getEdge(subNodeA, subNodeB);
		}
	}

	public Node<NodeType, EdgeType> getSubGraphNode(final int superNodeIndex) {
		for (int i = 0; i < superNodes.length; i++) {
			if (superNodes[i] == superNodeIndex) {
				return getSubGraph().getNode(i);
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSubGraphNode(de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> getSubGraphNode(
			final Node<NodeType, EdgeType> superNode) {
		final int superNodeIndex = superNode.getIndex();

		for (int i = 0; i < superNodes.length - 1; i++) {
			if (superNodes[i] == superNodeIndex) {
				return getSubGraph().getNode(i);
			}
		}

		return null;
	}

	public int getSubGraphNodeIndex(final int superNodeIndex) {
		for (int i = 0; i < superNodes.length; i++) {
			if (superNodes[i] == superNodeIndex) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraph()
	 */
	public Graph<NodeType, EdgeType> getSuperGraph() {
		return dbHPGraph.toGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraphEdge(de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> getSuperGraphEdge(
			final Edge<NodeType, EdgeType> subEdge) {
		return getSuperGraph().getEdge(getSuperGraphNode(subEdge.getNodeA()),
				getSuperGraphNode(subEdge.getNodeB()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraphNode(de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> getSuperGraphNode(final int subNodeIndex) {
		return getSuperGraph().getNode(superNodes[subNodeIndex]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#getSuperGraphNode(de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> getSuperGraphNode(
			final Node<NodeType, EdgeType> subNode) {
		return getSuperGraph().getNode(superNodes[subNode.getIndex()]);
	}

	public int getSuperGraphNodeIndex(final int subNodeIndex) {
		return superNodes[subNodeIndex];
	}

	public int[] getSuperNodes() {
		return superNodes;
	}

	public boolean isUsed(final int superGraphNodeIdx) {
		return usedNodes.get(superGraphNodeIdx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.Embedding#overlaps(de.parsemis.graph.Embedding,
	 *      java.util.Collection)
	 */
	public boolean overlaps(final Embedding<NodeType, EdgeType> other,
			final Collection<NodeType> ignore) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public DAGmEmbedding<NodeType, EdgeType> set(
			final DAGmGraph<NodeType, EdgeType> dbGraph,
			final Graph<NodeType, EdgeType> fragment, final int[] superNodes) {
		this.dbGraph = dbGraph;
		this.dbHPGraph = dbGraph.toHPGraph();
		this.subGraph = fragment;
		this.hpSubGraph = fragment.toHPGraph();
		this.superNodes = superNodes;
		this.usedNodes = new BitSet(dbHPGraph.getNodeCount());
		for (final int actNodeId : superNodes) {
			usedNodes.set(actNodeId);
		}
		return this;
	}

	public DAGmEmbedding<NodeType, EdgeType> set(
			final DAGmGraph<NodeType, EdgeType> dbGraph,
			final HPGraph<NodeType, EdgeType> fragment, final int[] superNodes) {
		this.dbGraph = dbGraph;
		this.dbHPGraph = dbGraph.toHPGraph();
		this.subGraph = fragment.toGraph();
		this.hpSubGraph = fragment;
		this.superNodes = superNodes;
		this.usedNodes = new BitSet(dbHPGraph.getNodeCount());
		for (final int actNodeId : superNodes) {
			usedNodes.set(actNodeId);
		}
		return this;
	}

	public HPEmbedding<NodeType, EdgeType> toHPEmbedding() {
		if (hp == null) {
			hp = new EmbeddingWrapper<NodeType, EdgeType>(this);
		}
		return hp;
	}
}
