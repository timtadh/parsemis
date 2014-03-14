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
package de.parsemis.strategy;

import static de.parsemis.miner.environment.Debug.VERBOSE;
import static de.parsemis.miner.environment.Debug.VVERBOSE;
import static de.parsemis.miner.environment.Debug.VVVERBOSE;
import static de.parsemis.miner.environment.Debug.out;

import java.util.Collection;
import java.util.Iterator;

import de.parsemis.algorithms.Algorithm;
import de.parsemis.miner.chain.Extender;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.Fragment;
import de.parsemis.utils.Frequented;

/**
 * This class represents the local recursive strategy.
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
public class RecursiveStrategy<NodeType, EdgeType> implements
		Strategy<NodeType, EdgeType> {

	private Extender<NodeType, EdgeType> extender;

	private Collection<Fragment<NodeType, EdgeType>> ret;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.strategy.Strategy#search(de.parsemis.miner.Algorithm,
	 *      int)
	 */
	public Collection<Fragment<NodeType, EdgeType>> search(
			final Algorithm<NodeType, EdgeType> algo) {
		ret = LocalEnvironment.env(this).getReturnSet();
		extender = algo.getExtender(0);

		for (final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo
				.initialNodes(); it.hasNext();) {
			final SearchLatticeNode<NodeType, EdgeType> code = it.next();
			final long time = System.currentTimeMillis();
			if (VERBOSE) {
				out.print("doing seed " + code + " ...");
			}
			if (VVERBOSE) {
				out.println();
			}

			search(code);
			it.remove();

			if (VERBOSE) {
				out.println("\tdone (" + (System.currentTimeMillis() - time)
						+ " ms)");
			}
		}

		return ret;
	}

	@SuppressWarnings("unchecked")
	private void search(final SearchLatticeNode<NodeType, EdgeType> node) {

		final Collection<SearchLatticeNode<NodeType, EdgeType>> tmp = extender
				.getChildren(node);

		for (final SearchLatticeNode<NodeType, EdgeType> child : tmp) {
			if (VVVERBOSE) {
				out.println("doing " + child);
			}

			search(child);

		}

		if (VVERBOSE) {
			out.println("node " + node + " done. Store: " + node.store()
					+ " children " + tmp.size() + " freq "
					+ ((Frequented) node).frequency());
		}

		if (node.store()) {
			node.store(ret);
		} else {
			node.release();
		}

		node.finalizeIt();
	}

}
