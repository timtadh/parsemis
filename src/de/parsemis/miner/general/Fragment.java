/**
 * created May 2, 2006
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
package de.parsemis.miner.general;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.Node;
import de.parsemis.utils.Frequented;
import de.parsemis.utils.Generic;

/**
 * This interface describe a special collection for embeddings.
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
public interface Fragment<NodeType, EdgeType> extends
		Collection<Embedding<NodeType, EdgeType>>, Generic<NodeType, EdgeType>,
		Frequented, Serializable {
	/**
	 * Adds just the given graph to the graph collection but no embeddings
	 * 
	 * @param graph
	 * @throws UnsupportedOperationException
	 *             if not available
	 */
	public void add(DataBaseGraph<NodeType, EdgeType> graph)
			throws UnsupportedOperationException;

	/**
	 * @return a copy of this fragment
	 */
	public Fragment<NodeType, EdgeType> copy();

	/**
	 * @param emb
	 * @param embeddingEdge
	 *            a node in the global subgraph returned by Fragment.getSubGraph
	 * @return the corresponding node of the subgraph of the given embedding
	 */
	public Edge<NodeType, EdgeType> embeddingToFragmentEdge(
			Embedding<NodeType, EdgeType> emb,
			Edge<NodeType, EdgeType> embeddingEdge);

	/**
	 * @param emb
	 * @param embeddingNode
	 *            a node in the global subgraph returned by Fragment.getSubGraph
	 * @return the corresponding node of the subgraph of the given embedding
	 */
	public Node<NodeType, EdgeType> embeddingToFragmentNode(
			Embedding<NodeType, EdgeType> emb,
			Node<NodeType, EdgeType> embeddingNode);

	/**
	 * @param emb
	 * @param fragmentEdge
	 *            a node in the global subgraph returned by Fragment.getSubGraph
	 * @return the corresponding node of the subgraph of the fragment
	 */
	public Edge<NodeType, EdgeType> fragmentToEmbeddingEdge(
			Embedding<NodeType, EdgeType> emb,
			Edge<NodeType, EdgeType> fragmentEdge);

	/**
	 * @param emb
	 * @param fragmentNode
	 *            a node in the global subgraph returned by Fragment.getSubGraph
	 * @return the corresponding node of the subgraph of the fragment
	 */
	public Node<NodeType, EdgeType> fragmentToEmbeddingNode(
			Embedding<NodeType, EdgeType> emb,
			Node<NodeType, EdgeType> fragmentNode);

	/**
	 * @return all embeddings, if stored
	 */
	public Collection<Embedding<NodeType, EdgeType>> getEmbeddings();

	/**
	 * @return a maximal subset of the embeddings so that no embeddings overlap
	 */
	public Collection<Embedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet();

	/**
	 * @return a graph representing this fragment
	 */
	public Graph<NodeType, EdgeType> toGraph();

	/**
	 * @return an iterator over the graphs this fragment is part of
	 */
	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator();

	public HPFragment<NodeType, EdgeType> toHPFragment();
}
