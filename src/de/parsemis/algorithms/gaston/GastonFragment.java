/**
 * Created Jan 06, 2008
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
package de.parsemis.algorithms.gaston;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.MaxCliqueStep;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.ThreadEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.miner.general.HPFragment;
import de.parsemis.miner.general.HPFragmentWrapper;
import de.parsemis.utils.FrequentedArrayList;
import de.parsemis.utils.FrequentedCollection;
import de.parsemis.utils.GraphSetIterator;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class GastonFragment<NodeType, EdgeType> extends
		ArrayList<HPEmbedding<NodeType, EdgeType>> implements
		HPFragment<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final protected Frequency graphBasedFrequency;

	final HPGraph<NodeType, EdgeType> subgraph;

	final BitSet graphSet;

	final protected int correspondingNode;

	private transient FrequentedCollection<HPEmbedding<NodeType, EdgeType>> mc;

	transient Fragment<NodeType, EdgeType> frag = null;

	public GastonFragment(final HPGraph<NodeType, EdgeType> sub,
			final int correspondingNode) {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		this.graphBasedFrequency = env.newFrequency();
		graphSet = new BitSet(env.graphCount());
		this.correspondingNode = correspondingNode;
		this.subgraph = sub;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPFragment#add(de.parsemis.miner.general.DataBaseGraph)
	 */
	public void add(final DataBaseGraph<NodeType, EdgeType> graph)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Gaston requires embeddings");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#add(java.lang.Object)
	 */
	@Override
	public boolean add(final HPEmbedding<NodeType, EdgeType> o) {
		assert o instanceof GastonEmbedding : "just GastonEmbeddings can be added to a GastonFragment";
		if (super.add(o)) {
			mc = null;
			if (o instanceof GastonEmbedding) {
				@SuppressWarnings("unchecked")
				final GastonEmbedding<NodeType, EdgeType> emb = (GastonEmbedding<NodeType, EdgeType>) o;
				emb.setFrag(this);
			}
			final int graphIdx = o.getDataBaseGraph().getIndex();
			if (graphSet.get(graphIdx)) {
				return true;
			}
			graphSet.set(graphIdx);
			graphBasedFrequency.add(o.getDataBaseGraph().frequency());
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(
			final Collection<? extends HPEmbedding<NodeType, EdgeType>> c) {
		boolean ret = false;
		for (final HPEmbedding<NodeType, EdgeType> emb : c) {
			ret |= add(emb);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		graphSet.clear();
		graphBasedFrequency.sub(graphBasedFrequency);
		mc = null;
		// subgraph = null;
		// correspondingNode = HPGraph.NO_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPFragment#copy()
	 */
	public HPFragment<NodeType, EdgeType> copy() {
		final GastonFragment<NodeType, EdgeType> ret = new GastonFragment<NodeType, EdgeType>(
				subgraph, correspondingNode);
		ret.addAll(this);
		return ret;
	}

	public void finalizeIt() {
		for (final HPEmbedding<NodeType, EdgeType> emb : this) {
			emb.freeTransient();
		}
		mc = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	public Frequency frequency() {
		if (LocalEnvironment.env(this).embeddingBased) {
			return maxClique().frequency();
		}
		return graphBasedFrequency;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPFragment#getMaximalNonOverlappingSubSet()
	 */
	public Collection<HPEmbedding<NodeType, EdgeType>> getMaximalNonOverlappingSubSet() {
		return maxClique();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPFragment#graphIterator()
	 */
	public Iterator<DataBaseGraph<NodeType, EdgeType>> graphIterator() {
		return new GraphSetIterator<NodeType, EdgeType>(graphSet,
				LocalEnvironment.env(this));
	}

	private final FrequentedCollection<HPEmbedding<NodeType, EdgeType>> maxClique() {
		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		if (mc == null) {
			mc = new FrequentedArrayList<HPEmbedding<NodeType, EdgeType>>(env
					.newFrequency());
		}
		MaxCliqueStep.findHPMaxClique(this, env.ignoreNodes, mc);
		return mc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.HPFragment#release(de.parsemis.miner.environment.ThreadEnvironment)
	 */
	public void release(final ThreadEnvironment<NodeType, EdgeType> tenv) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ArrayList#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object o) {
		throw new UnsupportedOperationException(
				"remove is not yet supported for GastonFragment");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException(
				"removeAll is not yet supported for GastonFragment");
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
	 * @see de.parsemis.miner.general.HPFragment#toHPGraph()
	 */
	public HPGraph<NodeType, EdgeType> toHPGraph() {
		return subgraph;
	}
}
