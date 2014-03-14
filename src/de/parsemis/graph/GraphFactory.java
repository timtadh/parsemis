/**
 * created May 8, 2006
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

/**
 * A factory for mutable graph objects.
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
public interface GraphFactory<NodeType, EdgeType> extends Serializable {

	/**
	 * @return a new empty graph
	 */
	public MutableGraph<NodeType, EdgeType> newGraph();

	/**
	 * @param name
	 * @return a new empty graph with the given name
	 */
	public MutableGraph<NodeType, EdgeType> newGraph(String name);

}
