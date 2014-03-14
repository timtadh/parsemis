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

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.utils.Frequented;
import de.parsemis.utils.Generic;

/**
 * This interface describe a special collection for high performance embeddings.
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
public interface HPFragment<NodeType, EdgeType> extends
		Collection<HPEmbedding<NodeType, EdgeType>>,
		Generic<NodeType, EdgeType>, Frequented, Serializable {
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
	public HPFragment<NodeType, EdgeType> copy();

	public void finalizeIt();

	/**
	 * @return a maximal subset of the embeddings so that no embeddings overlap
	 */
	public Collection<HPEmbedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet();

	/**
	 * @return an iterator over the graphs this fragment is part of
	 */
	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator();

	/**
	 * release all internal structures
	 * 
	 * @param tenv
	 */
	public void release(ThreadEnvironment<NodeType, EdgeType> tenv);

	/**
	 * @return a standard representation of this fragment
	 */
	public Fragment<NodeType, EdgeType> toFragment();

	/**
	 * @return a graph representing this fragment
	 */
	public HPGraph<NodeType, EdgeType> toHPGraph();
}
