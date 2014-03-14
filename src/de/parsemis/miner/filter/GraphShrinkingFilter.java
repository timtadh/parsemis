/**
 * created Jul 6, 2006
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

import java.util.ArrayList;
import java.util.Collection;

import de.parsemis.graph.Graph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.MiningStep;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.general.Fragment;

/**
 * This class weights all storable nodes by (count-1)*(size-1) [as SubDue] where
 * count represents number of embeddings and size is the sum of nodes and edges
 * it finally returns just the n best weighted fragments
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
public class GraphShrinkingFilter<NodeType, EdgeType> extends
		MiningStep<NodeType, EdgeType> implements
		FragmentFilter<NodeType, EdgeType> {
	private static class ShrinkElem<NodeType, EdgeType> {
		protected ShrinkElem<NodeType, EdgeType> next, prev;

		protected final int prod;

		protected final SearchLatticeNode<NodeType, EdgeType> node;

		protected ShrinkElem(final int p,
				final SearchLatticeNode<NodeType, EdgeType> n) {
			prod = p;
			node = n;
		}
	}

	private final ShrinkElem<NodeType, EdgeType> head;

	private final int n;

	/**
	 * creates a new mining step
	 * 
	 * @param next
	 */
	public GraphShrinkingFilter(final MiningStep<NodeType, EdgeType> next) {
		super(next);
		// createthe head marker of the local sorted (ring) list
		head = new ShrinkElem<NodeType, EdgeType>(0, null);
		head.next = head.prev = head;
		n = 3; // TODO: make configurable
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
		if (node.store()) {
			final Fragment<NodeType, EdgeType> frag = node.toFragment();
			final Graph<NodeType, EdgeType> g = frag.toGraph();

			// create new ShrinkElem Object
			final int count = frag.size();
			final int size = g.getNodeCount() + g.getEdgeCount();
			final ShrinkElem<NodeType, EdgeType> tmp = new ShrinkElem<NodeType, EdgeType>(
					(count - 1) * (size - 1), node);

			// and insert it in the local sorted (ring) list
			ShrinkElem<NodeType, EdgeType> ack = head;
			while (ack.next.prod > tmp.prod) {
				ack = ack.next;
			}
			tmp.next = ack.next;
			tmp.prev = ack;
			tmp.next.prev = tmp.prev.next = tmp;
		}
		callNext(node, extensions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.general.FragmentFilter#filter(java.util.Collection)
	 */
	public Collection<Fragment<NodeType, EdgeType>> filter(
			final Collection<Fragment<NodeType, EdgeType>> frags) {
		int count = n;
		final Collection<Fragment<NodeType, EdgeType>> ret = new ArrayList<Fragment<NodeType, EdgeType>>();

		// insert first n Fragments into ret
		for (ShrinkElem<NodeType, EdgeType> ack = head.next; count > 0
				&& ack != head; ack = ack.next, --count) {
			final Fragment<NodeType, EdgeType> f = ack.node.toFragment();
			ret.add(f);
		}

		return ret;
	}

}
