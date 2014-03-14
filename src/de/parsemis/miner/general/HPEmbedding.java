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

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.ThreadEnvironment;
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
public interface HPEmbedding<NodeType, EdgeType> extends
		Generic<NodeType, EdgeType>, Frequented, Serializable {

	/**
	 * @param superGraphEdge
	 * @return <code>true</code>, if for the given edge no subgraph edge is
	 *         available
	 */
	public boolean freeSuperEdge(final int superGraphEdge);

	/**
	 * @param superGraphNode
	 * @return <code>true</code>, if for the given node no subgraph node is
	 *         available
	 */
	public boolean freeSuperNode(final int superGraphNode);

	/**
	 * release all internal buffers
	 */
	void freeTransient();

	/**
	 * @return the database graph of this embedding
	 */
	public DataBaseGraph<NodeType, EdgeType> getDataBaseGraph();

	/**
	 * @return the subgraph of this embedding
	 */
	public HPGraph<NodeType, EdgeType> getSubGraph();

	/**
	 * @param superGraphEdge
	 * @return the corresponding edge of the subgraph
	 */
	public int getSubGraphEdge(final int superGraphEdge);

	/**
	 * @param superGraphNode
	 * @return the corresponding node of the subgraph
	 */
	public int getSubGraphNode(final int superGraphNode);

	/**
	 * @return the supergraph of this embedding
	 */
	public HPGraph<NodeType, EdgeType> getSuperGraph();

	/**
	 * @param subGraphEdge
	 * @return the corresponding edge of the supergraph
	 */
	public int getSuperGraphEdge(final int subGraphEdge);

	/**
	 * @param subGraphNode
	 * @return the corresponding node of the supergraph
	 */
	public int getSuperGraphNode(final int subGraphNode);

	/**
	 * checks if the current and the given embedding share common nodes
	 * 
	 * @param other
	 * @param ignore
	 *            node with labels inside ignore will be ignored (may be
	 *            <code>null</code>)
	 * @return <code>true</code>, if there are common nodes
	 */
	public boolean overlaps(HPEmbedding<NodeType, EdgeType> other,
			Collection<NodeType> ignore);

	/**
	 * release all internal structures
	 * 
	 * @param tenv
	 */
	public void release(ThreadEnvironment<NodeType, EdgeType> tenv);

	/**
	 * @return a standard embedding representing the same as the current one
	 */
	public Embedding<NodeType, EdgeType> toEmbedding();

}
