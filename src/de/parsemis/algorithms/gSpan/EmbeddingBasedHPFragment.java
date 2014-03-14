/**
 * created Jun 14, 2006
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

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.HPFragment;

/**
 * Represents a fragment to count and store embeddings for embedding based mining.
 * <p>
 * It can/will be stored in local object pool to avoid object generation/garbage
 * collection.
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
public class EmbeddingBasedHPFragment<NodeType, EdgeType> extends
		de.parsemis.miner.general.EmbeddingBasedHPFragment<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** used for the pool */
	transient EmbeddingBasedHPFragment<NodeType, EdgeType> next;

	private final transient GThreadEnvironment<NodeType, EdgeType> tenv;

	/**
	 * creates a new empty fragment
	 * 
	 * @param tenv
	 */
	public EmbeddingBasedHPFragment(
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		this(null, tenv);
	}

	/**
	 * creates a new empty fragment for the given subgraph
	 * 
	 * @param subgraph
	 * @param tenv
	 */
	public EmbeddingBasedHPFragment(final HPGraph<NodeType, EdgeType> subgraph,
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		super(subgraph);
		this.tenv = tenv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#copy()
	 */
	@Override
	public HPFragment<NodeType, EdgeType> copy() {
		final HPFragment<NodeType, EdgeType> ret = new EmbeddingBasedHPFragment<NodeType, EdgeType>(
				super.toHPGraph(), tenv);
		ret.addAll(this);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#release(de.parsemis.utils.ThreadEnvironment)
	 */
	@Override
	public void release(final ThreadEnvironment<NodeType, EdgeType> target) {
		super.release(target);
		if (tenv == target) {
			tenv.push(this);
		}
	}

	/**
	 * Initialization do allow reusability
	 * 
	 * @param subgraph
	 * @return a newly initialized fragment
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected EmbeddingBasedHPFragment<NodeType, EdgeType> set(
			final HPGraph<NodeType, EdgeType> subgraph) {
		return (EmbeddingBasedHPFragment<NodeType, EdgeType>) super
				.set(subgraph);
	}

}
