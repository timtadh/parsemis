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
package de.parsemis.miner.general;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.MaxCliqueStep;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.utils.FrequentedArrayList;
import de.parsemis.utils.FrequentedCollection;
import de.parsemis.utils.GraphSetIterator;

/**
 * This class represents a fragment to count and store embeddings for embedding
 * based mining
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
		FrequentedArrayList<HPEmbedding<NodeType, EdgeType>> implements
		HPFragment<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5178993425197555211L;

	HPGraph<NodeType, EdgeType> subgraph;

	BitSet graphSet;

	private transient FrequentedCollection<HPEmbedding<NodeType, EdgeType>> mc;

	transient Fragment<NodeType, EdgeType> frag = null;

	/**
	 * creates a new empty fragment
	 */
	public EmbeddingBasedHPFragment() {
		super(null);
	}

	/**
	 * creates a new empty fragment
	 * 
	 * @param subgraph
	 */
	public EmbeddingBasedHPFragment(final HPGraph<NodeType, EdgeType> subgraph) {
		super(LocalEnvironment.env(subgraph).newFrequency());
		this.graphSet = new BitSet(LocalEnvironment.env(this).graphCount());
		this.subgraph = subgraph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#add(de.parsemis.miner.DataBaseGraph)
	 */
	public void add(final DataBaseGraph<NodeType, EdgeType> graph)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException(
				"add just DataBaseGraph for embedding based mining not allowed");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(E)
	 */
	@Override
	public boolean add(final HPEmbedding<NodeType, EdgeType> arg0) {
		if (subgraph == null) {
			subgraph = arg0.getSubGraph();
		}
		if (super.add(arg0)) {
			mc = null;
			graphSet.set(arg0.getDataBaseGraph().getIndex());
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(
			final Collection<? extends HPEmbedding<NodeType, EdgeType>> arg0) {
		mc = null;
		boolean ret = false;
		for (final HPEmbedding<NodeType, EdgeType> e : arg0) {
			ret |= add(e);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		mc = null;
		subgraph = null;
		graphSet = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#copy()
	 */
	public HPFragment<NodeType, EdgeType> copy() {
		final HPFragment<NodeType, EdgeType> ret = new EmbeddingBasedHPFragment<NodeType, EdgeType>(
				subgraph);
		ret.addAll(this);
		return ret;
	}

	public void finalizeIt() {
		for (final HPEmbedding<NodeType, EdgeType> emb : this) {
			emb.freeTransient();
		}
		mc = null;
	}

	@Override
	public Frequency frequency() {
		return maxClique().frequency();
	}

	public Collection<HPEmbedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet() {
		return maxClique();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#graphIterator()
	 */
	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator() {
		if (graphSet == null) {
			// generate graphset out the stored embedding
			graphSet = new BitSet(LocalEnvironment.env(this).graphCount());
			for (final HPEmbedding<NodeType, EdgeType> e : this) {
				graphSet.set(e.getDataBaseGraph().getIndex());
			}
		}
		return new GraphSetIterator<NodeType, EdgeType>(graphSet,
				LocalEnvironment.env(subgraph));
	}

	private final FrequentedCollection<HPEmbedding<NodeType, EdgeType>> maxClique() {
		if (mc == null) {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(this);
			mc = new FrequentedArrayList<HPEmbedding<NodeType, EdgeType>>(env
					.newFrequency());
			MaxCliqueStep.findHPMaxClique(this, env.ignoreNodes, mc);
		}
		return mc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#release(de.parsemis.utils.ThreadEnvironment)
	 */
	public void release(final ThreadEnvironment<NodeType, EdgeType> target) {
		for (final HPEmbedding<NodeType, EdgeType> emb : this) {
			emb.release(target);
		}
		clear();
		// push fragment in subclass
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(final Object arg0) {
		if (super.remove(arg0)) {
			mc = null;
			graphSet = null;
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean removeAll(final Collection<?> arg0) {
		mc = null;
		boolean ret = false;
		for (final HPEmbedding<NodeType, EdgeType> e : (Collection<HPEmbedding<NodeType, EdgeType>>) arg0) {
			ret |= remove(e);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException("retain not allowed");
	}

	/**
	 * initialisation do allow reusability
	 * 
	 * @param subgraph
	 * @return a newly initialized fragment
	 */
	protected EmbeddingBasedHPFragment<NodeType, EdgeType> set(
			final HPGraph<NodeType, EdgeType> subgraph) {
		this.subgraph = subgraph;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#toFragment()
	 */
	public Fragment<NodeType, EdgeType> toFragment() {
		if (frag == null) {
			frag = new HPFragmentWrapper<NodeType, EdgeType>(this);
		}
		return frag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#toHPGraph()
	 */
	public HPGraph<NodeType, EdgeType> toHPGraph() {
		return subgraph;
	}
}
