/**
 * created May 22, 2006
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

/**
 * This interface describes a factory for ThreadEnvironments
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
public interface ThreadEnvironmentFactory<NodeType, EdgeType> {

	/**
	 * @param idx
	 *            thread index
	 * @param env
	 *            TODO
	 * @return a new ThreadEnvironment
	 */
	public ThreadEnvironment<NodeType, EdgeType> getNewEnvironment(int idx,
			LocalEnvironment<NodeType, EdgeType> env);

}
