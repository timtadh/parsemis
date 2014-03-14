/**
 * created Nov 13, 2007
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2007 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.jp;

import de.parsemis.miner.environment.GraphEnvironment;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.utils.Generic;

/**
 * This class represents the first local environment in the a JavaParty
 * Environment. So the other threads get access to the Graph DataBase.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 * 
 * @resident
 */
public class RemoteGlobalEnvironment<NodeType, EdgeType> implements
		GraphEnvironment<NodeType, EdgeType>, Generic<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.environment.GraphEnvironment#getGraph(int)
	 */
	public DataBaseGraph<NodeType, EdgeType> getGraph(final int idx) {
		return LocalEnvironment.env(this).getGraph(idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.environment.GraphEnvironment#graphCount()
	 */
	public int graphCount() {
		assert LocalEnvironment.env(this) != null : "LocalEnvironment not correct initialized on"
				+ Host.name();
		return LocalEnvironment.env(this).graphCount();
	}

}
