/**
 * created Jun 2, 2006
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
package de.parsemis.algorithms.gSpan;

import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.miner.general.HPExtendedEmbedding;

/**
 * Adds some performance relevant methods for gSpan edmbeddings.
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
public interface GSpanHPEmbedding<NodeType, EdgeType> extends
		HPExtendedEmbedding<NodeType, EdgeType> {

	/**
	 * extends the current embedding and builds a new larger one
	 * 
	 * @param ext
	 * @param edge
	 * @param superB
	 * @return a new extended embedding
	 */
	@SuppressWarnings("unchecked")
	public abstract HPEmbedding<NodeType, EdgeType> extend(
			final GSpanExtension<NodeType, EdgeType> ext, final int edge,
			final int superB);

	/**
	 * stores this embedding to the given <code>target</code> environment, if
	 * possible
	 * 
	 * @param target
	 */
	public abstract void release(
			final ThreadEnvironment<NodeType, EdgeType> target);

}