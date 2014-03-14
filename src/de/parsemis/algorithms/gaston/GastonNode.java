/**
 * Created Jan 05, 2008
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

import java.util.Collection;

import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.EmbeddingBasedHPFragment;
import de.parsemis.miner.general.Frequency;
import de.parsemis.miner.general.GraphBasedHPFragment;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.miner.general.HPFragment;
import de.parsemis.utils.Frequented;

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
public abstract class GastonNode<NodeType, EdgeType> extends
		SearchLatticeNode<NodeType, EdgeType> implements Frequented {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Leg<NodeType, EdgeType> me;

	protected HPFragment<NodeType, EdgeType> finalMe;

	public Collection<Leg<NodeType, EdgeType>> siblings;

	@SuppressWarnings("unused")
	protected int threadIdx;

	public GastonNode(final int level, final Leg<NodeType, EdgeType> leg,
			final Collection<Leg<NodeType, EdgeType>> siblings,
			final int threadIdx) {
		super(level);
		me = leg;
		this.siblings = siblings;
		this.threadIdx = threadIdx;
	}

	@Override
	public GastonFragment<NodeType, EdgeType> allEmbeddings() {
		return me.frag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.SearchLatticeNode#finalizeIt()
	 */
	@Override
	public void finalizeIt() {
		me = null;
		finalMe = null;
		siblings = null;
	}

	public Frequency frequency() {
		return toHPFragment().frequency();
	}

	public abstract Collection<Extension<NodeType, EdgeType>> getExtensions();

	public Leg<NodeType, EdgeType> getLeg() {
		return me;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.SearchLatticeNode#getThreadNumber()
	 */
	@Override
	public int getThreadNumber() {
		return threadIdx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.SearchLatticeNode#release()
	 */
	@Override
	public void release() {
		// do nothing
	}

	@Override
	public void setFinalEmbeddings(
			final Collection<HPEmbedding<NodeType, EdgeType>> embs) {
		finalMe = LocalEnvironment.env(this).embeddingBased ? new EmbeddingBasedHPFragment<NodeType, EdgeType>()
				: new GraphBasedHPFragment<NodeType, EdgeType>();
		finalMe.addAll(embs);
	}

	@Override
	public void setThreadNumber(final int threadIdx) {
		this.threadIdx = threadIdx;
	}

	@Override
	public HPFragment<NodeType, EdgeType> toHPFragment() {
		return (finalMe != null ? finalMe : me.frag);
	}

}