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
package de.parsemis.miner.general;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.MaxCliqueStep;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.utils.GraphSetIterator;

/**
 * This class represents a fragment to count (and store if wished) embeddings
 * for graph based mining
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
public class GraphBasedHPFragment<NodeType, EdgeType> implements
		HPFragment<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1296327785122905121L;

	HPGraph<NodeType, EdgeType> fragment;

	final BitSet graphSet;

	private final Frequency freq;

	private transient Collection<HPEmbedding<NodeType, EdgeType>> embeddings;

	private transient Collection<HPEmbedding<NodeType, EdgeType>> mc;

	transient Fragment<NodeType, EdgeType> frag = null;

	/**
	 * creates a new empty fragment
	 */
	public GraphBasedHPFragment() {
		this(null, LocalEnvironment.environ.storeEmbeddings);
	}

	/**
	 * creates a new empty fragment
	 * 
	 * @param storeEmbeddings
	 */
	public GraphBasedHPFragment(final boolean storeEmbeddings) {
		this(null, storeEmbeddings);
	}

	/**
	 * For copying a fragment
	 * 
	 * @param freq
	 * @param fragment
	 * @param graphSet
	 * @param storeEmbeddings
	 */
	protected GraphBasedHPFragment(final Frequency freq,
			final HPGraph<NodeType, EdgeType> fragment, final BitSet graphSet,
			final boolean storeEmbeddings) {
		this.freq = freq;
		this.graphSet = graphSet;
		this.fragment = fragment;
		if (storeEmbeddings) {
			embeddings = new ArrayList<HPEmbedding<NodeType, EdgeType>>();
		}
	}

	/**
	 * creates a new empty fragment
	 * 
	 * @param graph
	 */
	public GraphBasedHPFragment(final HPGraph<NodeType, EdgeType> graph) {
		this(graph, LocalEnvironment.environ.storeEmbeddings);
	}

	/**
	 * creates a new empty fragment
	 * 
	 * @param graph
	 * @param storeEmbeddings
	 */
	public GraphBasedHPFragment(final HPGraph<NodeType, EdgeType> graph,
			final boolean storeEmbeddings) {
		this.fragment = graph;
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		graphSet = new BitSet(env.graphCount());
		freq = env.newFrequency();
		if (storeEmbeddings) {
			embeddings = new ArrayList<HPEmbedding<NodeType, EdgeType>>();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#add(de.parsemis.graph.Graph)
	 */
	public void add(final DataBaseGraph<NodeType, EdgeType> graph)
			throws UnsupportedOperationException {
		final int idx = graph.getIndex();
		if (graphSet.get(idx)) {
			return;
		}
		graphSet.set(idx);
		freq.add(graph.frequency());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#add(E)
	 */
	public boolean add(final HPEmbedding<NodeType, EdgeType> arg0) {
		if (fragment == null) {
			fragment = arg0.getSubGraph();
		}
		if (storeEmbeddings()) {
			mc = null;
			embeddings.add(arg0);
		}
		this.add(arg0.getDataBaseGraph());
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(
			final Collection<? extends HPEmbedding<NodeType, EdgeType>> arg0) {
		boolean ret = false;
		for (final HPEmbedding<NodeType, EdgeType> emb : arg0) {
			ret |= add(emb);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		mc = null;
		graphSet.clear();
		embeddings().clear();
		freq.sub(freq);
		fragment = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(final Object arg0) {
		return embeddings().contains(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(final Collection<?> arg0) {
		return embeddings().containsAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#copy()
	 */
	public HPFragment<NodeType, EdgeType> copy() {
		return new GraphBasedHPFragment<NodeType, EdgeType>(getFreq(),
				getFrag(), getSet(), storeEmbeddings());
	}

	private final Collection<HPEmbedding<NodeType, EdgeType>> embeddings() {
		if (embeddings == null) {
			// TODO: check for storeEmbeddings and recalculate them
			return new ArrayList<HPEmbedding<NodeType, EdgeType>>();
		}
		return embeddings;
	}

	public void finalizeIt() {
		if (storeEmbeddings()) {
			for (final HPEmbedding<NodeType, EdgeType> emb : embeddings) {
				emb.freeTransient();
			}
		}
		mc = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#getFrequency()
	 */
	public Frequency frequency() {
		return freq;
	}

	protected HPGraph<NodeType, EdgeType> getFrag() {
		return fragment.clone();
	}

	protected Frequency getFreq() {
		return freq.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPFragment#getMaximalNonOverlappingSubSet()
	 */
	public Collection<HPEmbedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet() {
		if (mc == null) {
			mc = MaxCliqueStep.findHPMaxClique(this,
					LocalEnvironment.env(this).ignoreNodes);
		}
		return mc;
	}

	protected BitSet getSet() {
		return (BitSet) graphSet.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#graphIterator()
	 */
	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator() {
		return new GraphSetIterator<NodeType, EdgeType>(graphSet,
				LocalEnvironment.env(fragment));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#iterator()
	 */
	public Iterator<HPEmbedding<NodeType, EdgeType>> iterator() {
		return embeddings().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.HPFragment#release(de.parsemis.utils.ThreadEnvironment)
	 */
	public void release(final ThreadEnvironment<NodeType, EdgeType> target) {
		if (storeEmbeddings()) {
			for (final HPEmbedding<NodeType, EdgeType> emb : embeddings) {
				emb.release(target);
			}
		}
		clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(final Object arg0) {
		throw new UnsupportedOperationException(
				"remove is not yet supported for GraphBasedFragment");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException(
				"removeAll is not yet supported for GraphBasedFragment");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(final Collection<?> arg0) {
		throw new UnsupportedOperationException(
				"retainAll is not yet supported for GraphBasedFragment");
	}

	protected GraphBasedHPFragment<NodeType, EdgeType> set(
			final HPGraph<NodeType, EdgeType> subgraph) {
		this.fragment = subgraph;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return embeddings().size();
	}

	public boolean storeEmbeddings() {
		return embeddings != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		return toArray(new Object[size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#toArray(T[])
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(final T[] arg0) {
		int i = -1;
		for (final T emb : (Collection<T>) embeddings()) {
			arg0[++i] = emb;
		}
		return arg0;
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
	 * @see de.parsemis.miner.Fragment#toGraph()
	 */
	public HPGraph<NodeType, EdgeType> toHPGraph() {
		return fragment;
	}

}
