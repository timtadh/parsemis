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
package de.parsemis.miner.environment;

import java.io.Serializable;

import de.parsemis.miner.general.DataBaseGraph;

/**
 * This interface encapsulates the functionallity for a local and distributed
 * (DataBase)Graph environment.
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
public interface GraphEnvironment<NodeType, EdgeType> extends Serializable {

	/**
	 * @param idx
	 * @return the database graph for the given index <code>idx</code>
	 */
	public DataBaseGraph<NodeType, EdgeType> getGraph(final int idx);

	/**
	 * @return the number of graphs in the database
	 */
	public int graphCount();

}
