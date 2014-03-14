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
package de.parsemis.graph;

import java.io.Serializable;
import java.util.Iterator;

import de.parsemis.utils.Cloneable;
import de.parsemis.utils.Generic;

/**
 * Declares the whole functionality of a single typed graph.
 * <p>
 * Its edges can be directed or undirected.
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
public interface Graph<NodeType, EdgeType> extends
		Cloneable<Graph<NodeType, EdgeType>>, Generic<NodeType, EdgeType>,
		Serializable {

	/**
	 * @return an iterator over all edges of this graph
	 */
	public Iterator<Edge<NodeType, EdgeType>> edgeIterator();

	/**
	 * @param idx
	 * @return the edge of the graph with the given index (or <code>null</code>
	 *         if not available)
	 */
	public Edge<NodeType, EdgeType> getEdge(int idx);

	/**
	 * @param nodeA
	 * @param nodeB
	 * @return the (directed) edge from nodeA to nodeB (or <code>null</code>
	 *         if not available)
	 */
	public Edge<NodeType, EdgeType> getEdge(Node<NodeType, EdgeType> nodeA,
			Node<NodeType, EdgeType> nodeB);

	/**
	 * @return the count of edges of this graph
	 */
	public int getEdgeCount();

	/**
	 * @return the uniq id of the graph
	 */
	public int getID();

	/**
	 * @return the maximal used index for edges of this graph
	 */
	public int getMaxEdgeIndex();

	/**
	 * @return the maximal used index for nodes of this graph
	 */
	public int getMaxNodeIndex();

	/**
	 * @return the name of the graph
	 */
	public String getName();

	/**
	 * @param idx
	 * @return the node of the graph with the given index (or <code>null</code>
	 *         if not available)
	 */
	public Node<NodeType, EdgeType> getNode(int idx);

	/**
	 * @param edge
	 * @return the first node of the given edge
	 */
	public Node<NodeType, EdgeType> getNodeA(Edge<NodeType, EdgeType> edge);

	/**
	 * @param edge
	 * @return the second node of the given edge
	 */
	public Node<NodeType, EdgeType> getNodeB(Edge<NodeType, EdgeType> edge);

	/**
	 * @return the count of nodes of this graph
	 */
	public int getNodeCount();

	/**
	 * @return an iterator over all nodes of this graph
	 */
	public Iterator<Node<NodeType, EdgeType>> nodeIterator();

	/**
	 * @return a HPGraph representing this graph
	 */
	public HPGraph<NodeType, EdgeType> toHPGraph();

}
