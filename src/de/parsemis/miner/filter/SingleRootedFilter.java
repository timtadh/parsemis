/**
 * created Sep 27, 2006
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
package de.parsemis.miner.filter;

import java.util.Collection;
import java.util.Iterator;

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.general.Fragment;

/**
 * This class provide a post filter to filter out single rooted fragments. These
 * are directed graphs (normally DAGs) only with one node without incoming
 * edges.
 * 
 * As mining step it removes multi-rooted fragments. This is only valid, if the
 * extensions rules will not created single-rooted fragments out of multi-rooted
 * ones!
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
public class SingleRootedFilter<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> implements
		FragmentFilter<NodeType, EdgeType> {

	/**
	 * creates a new SingleRootedFilter for use as FragmentFilter
	 */
	public SingleRootedFilter() {
		super(null);
	}

	/**
	 * creates a new SingleRootedFilter for use as MiningStep
	 * 
	 * @param next
	 */
	public SingleRootedFilter(final MiningStep<NodeType, EdgeType> next) {
		super(next);
	}

	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		node.store(node.store()
				&& getSingleRoot(node.toHPFragment().toHPGraph()) >= 0);
		callNext(node, extensions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.general.FragmentFilter#filter(java.util.Collection)
	 */
	public Collection<Fragment<NodeType, EdgeType>> filter(
			final Collection<Fragment<NodeType, EdgeType>> frags) {
		for (final Iterator<Fragment<NodeType, EdgeType>> fit = frags
				.iterator(); fit.hasNext();) {
			final Fragment<NodeType, EdgeType> ack = fit.next();
			if (getSingleRoot(ack.toGraph().toHPGraph()) < 0) {
				fit.remove();
			}
		}
		return frags;
	}

	private final int getSingleRoot(final HPGraph<NodeType, EdgeType> graph) {
		if (graph.getNodeCount() < 1) {
			return -1;
		}
		int ret = -1;
		for (int ack = graph.getMaxNodeIndex() - 1; ack >= 0; ack--) {
			if (graph.isValidNode(ack) && graph.getInDegree(ack) == 0) {
				if (ret != -1) {
					return -1;
				} else {
					ret = ack;
				}
			}
		}
		return ret;
	}

}
