/**
 * created May 3, 2006
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

/**
 * Declares the functions to add (and remove) nodes and edges from a graph.
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
public interface MutableGraph<NodeType, EdgeType> extends
		Graph<NodeType, EdgeType> {

	/**
	 * adds a new edge with the given label and the given direction between the
	 * both given nodes.
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param label
	 * @param direction
	 * @return the new edge, (or <code>null</code> if the was still an edge
	 *         between these nodes with the same direction)
	 */
	public Edge<NodeType, EdgeType> addEdge(Node<NodeType, EdgeType> nodeA,
			Node<NodeType, EdgeType> nodeB, EdgeType label, int direction);

	/**
	 * adds a new node with the given label
	 * 
	 * @param label
	 * @return the newly created node
	 */
	public Node<NodeType, EdgeType> addNode(NodeType label);

	/**
	 * adds a new node with the given node label and connect it and nodeA with a
	 * new edge according to the given label and direction
	 * 
	 * @param nodeA
	 * @param nodeBLabel
	 * @param edgeLabel
	 * @param direction
	 * @return the new edge
	 */
	public Node<NodeType, EdgeType> addNodeAndEdge(
			Node<NodeType, EdgeType> nodeA, NodeType nodeBLabel,
			EdgeType edgeLabel, int direction);

	/**
	 * removes the given edge
	 * 
	 * @param edge
	 * @return <code>true</code>, if the edge is removed succesfully
	 */
	public boolean removeEdge(Edge<NodeType, EdgeType> edge);

	/**
	 * removes the given node and all its connected edges
	 * 
	 * @param node
	 * @return <code>true</code>, if the node is removed succesfully
	 */
	public boolean removeNode(Node<NodeType, EdgeType> node);
};
