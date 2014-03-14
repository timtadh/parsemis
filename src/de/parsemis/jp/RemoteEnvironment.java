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

import java.util.Collection;

import de.parsemis.algorithms.Algorithm;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.Fragment;
import de.parsemis.strategy.StackList;
import de.parsemis.utils.Generic;

/**
 * This class implicitly creates the local environment on remote hosts.
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
 * @remote
 */
public class RemoteEnvironment<NodeType, EdgeType> implements
		Generic<NodeType, EdgeType> {
	private final int idx;

	/**
	 * creates a remote environment and sets the local environment of the
	 * current host.
	 * 
	 * @param env
	 *            the (implicitly cloned) local environment
	 * @param idx
	 *            the index of the current host
	 */
	public RemoteEnvironment(final LocalEnvironment<NodeType, EdgeType> env,
			final int idx) {
		if (LocalEnvironment.environ == null) {
			LocalEnvironment.set(env);
		}
		this.idx = idx;
	}

	/**
	 * @param algo
	 * @param sl
	 * @param answer
	 * @return a new JPThread according to the parameters
	 */
	public final RemoteJPThread<NodeType, EdgeType> createJPThread(
			final Algorithm<NodeType, EdgeType> algo,
			final StackList<NodeType, EdgeType> sl,
			final Collection<Fragment<NodeType, EdgeType>> answer) {
		return /** @at idx */
		new RemoteJPThread<NodeType, EdgeType>(idx, algo, sl, answer);
	}

}
