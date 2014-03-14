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

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.Node;
import de.parsemis.utils.Frequented;
import de.parsemis.utils.Generic;

/**
 * This interface declares the functionality of an embedding between a subgraph
 * and its supergraph An embedding maps each node and each edge of a subgraph to
 * exactly one edge of the supergraph
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
public interface Embedding<NodeType, EdgeType> extends
		Generic<NodeType, EdgeType>, Frequented, Serializable {

	/**
	 * @return the database graph of this embedding
	 */
	public DataBaseGraph<NodeType, EdgeType> getDataBaseGraph();

	/**
	 * @return the subgraph of this embedding
	 */
	public Graph<NodeType, EdgeType> getSubGraph();

	/**
	 * @param superGraphEdge
	 * @return the corresponding edge of the subgraph
	 */
	public Edge<NodeType, EdgeType> getSubGraphEdge(
			Edge<NodeType, EdgeType> superGraphEdge);

	/**
	 * @param superGraphNode
	 * @return the corresponding node of the subgraph
	 */
	public Node<NodeType, EdgeType> getSubGraphNode(
			Node<NodeType, EdgeType> superGraphNode);

	/**
	 * @return the supergraph of this embedding
	 */
	public Graph<NodeType, EdgeType> getSuperGraph();

	/**
	 * @param subGraphEdge
	 * @return the corresponding edge of the supergraph
	 */
	public Edge<NodeType, EdgeType> getSuperGraphEdge(
			Edge<NodeType, EdgeType> subGraphEdge);

	/**
	 * @param subGraphNode
	 * @return the corresponding node of the supergraph
	 */
	public Node<NodeType, EdgeType> getSuperGraphNode(
			Node<NodeType, EdgeType> subGraphNode);

	/**
	 * checks if the current and the given embedding share common nodes
	 * 
	 * @param other
	 * @param ignore
	 *            node with labels inside ignore will be ignored (may be
	 *            <code>null</code>)
	 * @return <code>true</code>, if there are common nodes
	 */
	public boolean overlaps(Embedding<NodeType, EdgeType> other,
			Collection<NodeType> ignore);

	/**
	 * @return a high performance embedding representing the same as the current
	 *         one
	 */
	public HPEmbedding<NodeType, EdgeType> toHPEmbedding();

}
