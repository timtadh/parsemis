/**
 * created May 23, 2006
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

import java.util.BitSet;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPFragment;

/**
 * Represents a fragment to count (and store if wished) embeddings
 * for graph based mining.
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
public class GraphBasedHPFragment<NodeType, EdgeType> extends
		de.parsemis.miner.general.GraphBasedHPFragment<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** used for the pool */
	transient GraphBasedHPFragment<NodeType, EdgeType> next;

	private/* final */transient GThreadEnvironment<NodeType, EdgeType> tenv;

	/**
	 * For copying a fragment
	 * 
	 * @param freq
	 * @param fragment
	 * @param graphSet
	 * @param storeEmbeddings
	 * @param tenv
	 */
	private GraphBasedHPFragment(final Frequency freq,
			final HPGraph<NodeType, EdgeType> fragment, final BitSet graphSet,
			final boolean storeEmbeddings,
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		super(freq, fragment, graphSet, storeEmbeddings);
		this.tenv = tenv;
	}

	/**
	 * creates a new empty fragment
	 * 
	 * @param tenv
	 */
	public GraphBasedHPFragment(
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		this(null, tenv);
	}

	/**
	 * creates a new empty fragment
	 * 
	 * @param graph
	 */
	public GraphBasedHPFragment(final HPGraph<NodeType, EdgeType> graph) {
		this(graph, null);
	}

	/**
	 * creates an empty Fragment for the given subgraph
	 * 
	 * @param graph
	 * @param tenv
	 */
	public GraphBasedHPFragment(final HPGraph<NodeType, EdgeType> graph,
			final GThreadEnvironment<NodeType, EdgeType> tenv) {
		super(graph);
		this.tenv = tenv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#copy()
	 */
	@Override
	public HPFragment<NodeType, EdgeType> copy() {
		return new GraphBasedHPFragment<NodeType, EdgeType>(getFreq(),
				getFrag(), getSet(), storeEmbeddings(), tenv);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.GraphBasedHPFragment#set(de.parsemis.graph.HPGraph)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected GraphBasedHPFragment<NodeType, EdgeType> set(
			final HPGraph<NodeType, EdgeType> subgraph) {
		return (de.parsemis.algorithms.gSpan.GraphBasedHPFragment<NodeType, EdgeType>) super
				.set(subgraph);
	}
}
