/**
 * created May 16, 2006
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

import static de.parsemis.miner.environment.Debug.INFO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import de.parsemis.algorithms.gaston.GastonCycle;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.Statistics;

/**
 * This class contains the whole chain of mining steps to generate the children
 * of a search node
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
public class DefaultExtender<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> implements Extender<NodeType, EdgeType> {

	private MiningStep<NodeType, EdgeType> first;

	protected final Collection<Extension<NodeType, EdgeType>> dummy;

	protected Collection<SearchLatticeNode<NodeType, EdgeType>> children;

	/**
	 * creates a new empty Extender that is also the end of the mining chain
	 */
	public DefaultExtender() {
		super(null);
		first = this;
		dummy = new TreeSet<Extension<NodeType, EdgeType>>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 *      java.util.Collection)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		// called at the end of the chain, so all extensions are generated and
		// the children can be built
		for (final Extension<NodeType, EdgeType> ext : extensions) {
			final SearchLatticeNode<NodeType, EdgeType> child = node
					.extend(ext);
			if (child != null) {
				children.add(child);
			}
		}
	}

	/**
	 * calls the next step for the given <code>node</code> with the given
	 * <code>extensions</code>
	 * 
	 * @param node
	 * @param extensions
	 */
	protected final void callFirst(
			final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		if (first != null) {
			first.call(node, extensions);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Extender#getChildren(java.util.Collection)
	 */
	public Collection<SearchLatticeNode<NodeType, EdgeType>> getChildren(
			final Collection<SearchLatticeNode<NodeType, EdgeType>> nodes) {
		throw new UnsupportedOperationException("not available at present");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.Extender#getChildren(de.parsemis.miner.SearchLatticeNode)
	 */
	public Collection<SearchLatticeNode<NodeType, EdgeType>> getChildren(
			final SearchLatticeNode<NodeType, EdgeType> node) {
		final long before = System.currentTimeMillis();
		// ArrayList for deterministic search
		children = new ArrayList<SearchLatticeNode<NodeType, EdgeType>>();
		dummy.clear();
		// start the run thru the chain for the given node
		callFirst(node, dummy);
		if (INFO) {
			final Statistics stats = LocalEnvironment.env(this).stats;
			synchronized (stats.syncTime) {
				stats.syncTime[1] += System.currentTimeMillis() - before;
				if (node instanceof GastonCycle)
					stats.unconnectedFragments++;
			}
		}
		return children;
	}

	public final void setFirst(final MiningStep<NodeType, EdgeType> first) {
		this.first = first;
	}

}
