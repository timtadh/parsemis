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

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.Node;
import de.parsemis.miner.environment.LocalEnvironment;

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
public class GraphBasedFragment<NodeType, EdgeType> implements
		Fragment<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6010671940763039895L;

	private transient Collection<Embedding<NodeType, EdgeType>> embeddings;

	final BitSet graphSet;

	private final Frequency freq;

	Graph<NodeType, EdgeType> fragment;

	transient HPFragment<NodeType, EdgeType> hp = null;

	/**
	 * For copying a fragment
	 * 
	 * @param freq
	 * @param fragment
	 * @param graphSet
	 * @param storeEmbeddings
	 */
	private GraphBasedFragment(final Frequency freq,
			final Graph<NodeType, EdgeType> fragment, final BitSet graphSet,
			final boolean storeEmbeddings) {
		this.freq = freq;
		this.graphSet = graphSet;
		this.fragment = fragment;
		if (storeEmbeddings) {
			embeddings = new ArrayList<Embedding<NodeType, EdgeType>>();
		}
	}

	/**
	 * creates an empty Fragment for the given subgraph
	 * 
	 * @param graph
	 */
	public GraphBasedFragment(final Graph<NodeType, EdgeType> graph) {
		this.fragment = graph;
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		graphSet = new BitSet(env.graphCount());
		freq = env.newFrequency();
		if (env.storeEmbeddings) {
			embeddings = new ArrayList<Embedding<NodeType, EdgeType>>();
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
	public boolean add(final Embedding<NodeType, EdgeType> arg0) {
		if (fragment == null) {
			fragment = arg0.getSubGraph();
		}
		if (embeddings != null) {
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
			final Collection<? extends Embedding<NodeType, EdgeType>> arg0) {
		return embeddings().addAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
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
	public Fragment<NodeType, EdgeType> copy() {
		return new GraphBasedFragment<NodeType, EdgeType>(freq.clone(),
				fragment.clone(), (BitSet) graphSet.clone(), embeddings != null);
	}

	private final Collection<Embedding<NodeType, EdgeType>> embeddings() {
		if (embeddings == null) {
			return new ArrayList<Embedding<NodeType, EdgeType>>();
		}
		return embeddings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#embeddingToFragmentEdge(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> embeddingToFragmentEdge(
			final Embedding<NodeType, EdgeType> emb,
			final Edge<NodeType, EdgeType> embeddingEdge) {
		assert (contains(emb));
		return embeddingEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#embeddingToFragmentNode(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> embeddingToFragmentNode(
			final Embedding<NodeType, EdgeType> emb,
			final Node<NodeType, EdgeType> embeddingNode) {
		assert (contains(emb));
		return embeddingNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#fragmentToEmbeddingEdge(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Edge)
	 */
	public Edge<NodeType, EdgeType> fragmentToEmbeddingEdge(
			final Embedding<NodeType, EdgeType> emb,
			final Edge<NodeType, EdgeType> fragmentEdge) {
		assert (contains(emb));
		return fragmentEdge;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#fragmentToEmbeddingNode(de.parsemis.graph.Embedding,
	 *      de.parsemis.graph.Node)
	 */
	public Node<NodeType, EdgeType> fragmentToEmbeddingNode(
			final Embedding<NodeType, EdgeType> emb,
			final Node<NodeType, EdgeType> fragmentNode) {
		assert (contains(emb));
		return fragmentNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#getFrequency()
	 */
	public Frequency frequency() {
		return freq;
	}

	public Collection<Embedding<NodeType, EdgeType>> getEmbeddings() {
		return embeddings;
	}

	public Collection<Embedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet() {
		throw new UnsupportedOperationException(
				"getMaximalNonOverlappingSubSet is not yet supported for GraphBasedFragment");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#toGraph()
	 */
	public Graph<NodeType, EdgeType> toGraph() {
		return fragment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Fragment#graphIterator()
	 */
	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator() {
		return new Iterator<DataBaseGraph<NodeType, EdgeType>>() {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(fragment);

			int next = graphSet.nextSetBit(0);

			public boolean hasNext() {
				return next >= 0;
			}

			public DataBaseGraph<NodeType, EdgeType> next() {
				final DataBaseGraph<NodeType, EdgeType> ret = env
						.getGraph(next);
				next = graphSet.nextSetBit(next + 1);
				return ret;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
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
	public Iterator<Embedding<NodeType, EdgeType>> iterator() {
		return embeddings().iterator();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return embeddings().size();
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
	 * @see de.parsemis.miner.general.Fragment#toHPFragment()
	 */
	public HPFragment<NodeType, EdgeType> toHPFragment() {
		if (hp == null) {
			hp = new FragmentWrapper<NodeType, EdgeType>(this);
		}
		return hp;
	}
}
