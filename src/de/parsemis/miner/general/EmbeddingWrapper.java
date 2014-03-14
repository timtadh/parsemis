/**
 * Created on 12.01.2008
 *
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * Copyright 2008 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.miner.general;

import java.util.BitSet;
import java.util.Collection;

import de.parsemis.graph.Edge;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.environment.ThreadEnvironment;

/**
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
public final class EmbeddingWrapper<NodeType, EdgeType> implements
		HPEmbedding<NodeType, EdgeType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private final Embedding<NodeType, EdgeType> embedding;

	transient BitSet freeEdges = null;

	transient BitSet freeNodes = null;

	/**
	 * @param embedding
	 */
	public EmbeddingWrapper(final Embedding<NodeType, EdgeType> embedding) {
		this.embedding = embedding;
	}

	public boolean freeSuperEdge(final int superGraphEdge) {
		if (freeEdges == null) {
			final HPGraph<NodeType, EdgeType> sup = getDataBaseGraph()
					.toHPGraph();
			freeEdges = new BitSet(sup.getMaxEdgeIndex());
			for (int i = sup.getMaxEdgeIndex() - 1; i >= 0; --i) {
				freeEdges.set(i, sup.isValidEdge(i)
						&& getSubGraphEdge(i) == HPGraph.NO_EDGE);
			}
		}
		return freeEdges.get(superGraphEdge);
	}

	public boolean freeSuperNode(final int superGraphNode) {
		if (freeNodes == null) {
			final HPGraph<NodeType, EdgeType> sup = getDataBaseGraph()
					.toHPGraph();
			freeNodes = new BitSet(sup.getMaxNodeIndex());
			for (int i = sup.getMaxNodeIndex() - 1; i >= 0; --i) {
				freeNodes.set(i, sup.isValidNode(i)
						&& getSubGraphNode(i) == HPGraph.NO_NODE);
			}
		}
		return freeNodes.get(superGraphNode);
	}

	public void freeTransient() {
		freeEdges = null;
		freeNodes = null;
	}

	public Frequency frequency() {
		return this.embedding.frequency();
	}

	public DataBaseGraph<NodeType, EdgeType> getDataBaseGraph() {
		return this.embedding.getDataBaseGraph();
	}

	public HPGraph<NodeType, EdgeType> getSubGraph() {
		return this.embedding.getSubGraph().toHPGraph();
	}

	public int getSubGraphEdge(final int superGraphEdge) {
		final Edge<NodeType, EdgeType> e = this.embedding
				.getSubGraphEdge(this.embedding.getSuperGraph().getEdge(
						superGraphEdge));
		return e == null ? HPGraph.NO_EDGE : e.getIndex();
	}

	public int getSubGraphNode(final int superGraphNode) {
		final Node<NodeType, EdgeType> n = this.embedding
				.getSubGraphNode(this.embedding.getSuperGraph().getNode(
						superGraphNode));
		return n == null ? HPGraph.NO_NODE : n.getIndex();
	}

	public HPGraph<NodeType, EdgeType> getSuperGraph() {
		return this.embedding.getSuperGraph().toHPGraph();
	}

	public int getSuperGraphEdge(final int subGraphEdge) {
		return this.embedding.getSuperGraphEdge(
				this.embedding.getSubGraph().getEdge(subGraphEdge)).getIndex();
	}

	public int getSuperGraphNode(final int subGraphNode) {
		return this.embedding.getSuperGraphNode(
				this.embedding.getSubGraph().getNode(subGraphNode)).getIndex();
	}

	private final boolean overlaps(final HPEmbedding<NodeType, EdgeType> other) {
		for (int i = 0; i < getSubGraph().getNodeCount(); ++i) {
			if (other.getSubGraphNode(getSuperGraphNode(i)) != HPGraph.NO_NODE) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPEmbedding#overlaps(de.parsemis.graph.HPEmbedding,
	 *      java.util.Collection)
	 */
	public boolean overlaps(final HPEmbedding<NodeType, EdgeType> other,
			final Collection<NodeType> ignore) {
		if (other.getDataBaseGraph() != this.getDataBaseGraph()) {
			return false;
		}
		if (ignore == null) {
			return overlaps(other);
		}
		for (int i = 0; i < getSubGraph().getNodeCount(); ++i) {
			if (other.getSubGraphNode(getSuperGraphNode(i)) != HPGraph.NO_NODE
					&& !ignore.contains(getSuperGraph().getNodeLabel(
							getSuperGraphNode(i)))) {
				return true;
			}
		}
		return false;
	}

	public void release(final ThreadEnvironment<NodeType, EdgeType> tenv) {
		// do nothing

	}

	public Embedding<NodeType, EdgeType> toEmbedding() {
		return this.embedding;
	}
}