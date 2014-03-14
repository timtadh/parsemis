/**
 * created May 19, 2006
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
import java.util.BitSet;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.utils.Frequented;
import de.parsemis.utils.Generic;

/**
 * This interface encapsulates the required ability to be a DataBaseGraph.
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
public interface DataBaseGraph<NodeType, EdgeType> extends Frequented,
		Generic<NodeType, EdgeType>, Serializable {

	/** @return a (sub-)set of the relevant edges of the graph */
	public BitSet getEdges();

	/** @return the index of this graph in the local environment */
	public int getIndex();

	/** @return a (sub-)set of the relevant nodes of the graph */
	public BitSet getNodes();

	/** @return the original graph represented by this database graph */
	public Graph<NodeType, EdgeType> toGraph();

	/** @return the original graph represented by this database graph */
	public HPGraph<NodeType, EdgeType> toHPGraph();

}
