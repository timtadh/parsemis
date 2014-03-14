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

import java.util.Collection;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.Node;

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
public final class HPEmbeddingWrapper<NodeType, EdgeType> implements
		Embedding<NodeType, EdgeType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private final HPEmbedding<NodeType, EdgeType> hp;

	/**
	 * @param embedding
	 */
	public HPEmbeddingWrapper(final HPEmbedding<NodeType, EdgeType> embedding) {
		hp = embedding;
	}

	public Frequency frequency() {
		return hp.frequency();
	}

	public DataBaseGraph<NodeType, EdgeType> getDataBaseGraph() {
		return hp.getDataBaseGraph();
	}

	public Graph<NodeType, EdgeType> getSubGraph() {
		return hp.getSubGraph().toGraph();
	}

	public Edge<NodeType, EdgeType> getSubGraphEdge(
			final Edge<NodeType, EdgeType> superGraphEdge) {
		return getSubGraph().getEdge(
				hp.getSubGraphEdge(superGraphEdge.getIndex()));
	}

	public Node<NodeType, EdgeType> getSubGraphNode(
			final Node<NodeType, EdgeType> superGraphNode) {
		return getSubGraph().getNode(
				hp.getSubGraphNode(superGraphNode.getIndex()));
	}

	public Graph<NodeType, EdgeType> getSuperGraph() {
		return hp.getSuperGraph().toGraph();
	}

	public Edge<NodeType, EdgeType> getSuperGraphEdge(
			final Edge<NodeType, EdgeType> subGraphEdge) {
		return getSuperGraph().getEdge(
				hp.getSubGraphEdge(subGraphEdge.getIndex()));
	}

	public Node<NodeType, EdgeType> getSuperGraphNode(
			final Node<NodeType, EdgeType> subGraphNode) {
		return getSuperGraph().getNode(
				hp.getSubGraphNode(subGraphNode.getIndex()));
	}

	public boolean overlaps(final Embedding<NodeType, EdgeType> other,
			final Collection<NodeType> ignore) {
		return hp.overlaps(other.toHPEmbedding(), ignore);
	}

	public HPEmbedding<NodeType, EdgeType> toHPEmbedding() {
		return hp;
	}
}