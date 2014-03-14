/**
 * created Oct 2, 2006
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
package de.parsemis.miner.chain;

import java.io.Serializable;

/**
 * This class is a factory for mining steps
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
public interface MiningStepFactory<NodeType, EdgeType> extends Serializable {

	/**
	 * @param next
	 *            the succesor of the new step
	 * @return a new GenerationParialStep
	 * @throws UnsupportedOperationException
	 */
	GenerationPartialStep<NodeType, EdgeType> createGenerationPartialStep(
			final MiningStep<NodeType, EdgeType> next)
			throws UnsupportedOperationException;

	/**
	 * @param next
	 *            the succesor of the new step
	 * @return a new MiningStep
	 * @throws UnsupportedOperationException
	 */
	MiningStep<NodeType, EdgeType> createMiningStep(
			final MiningStep<NodeType, EdgeType> next)
			throws UnsupportedOperationException;

}
