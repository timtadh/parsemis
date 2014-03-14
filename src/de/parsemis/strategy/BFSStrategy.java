/**
 * created Nov 14, 2007
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
package de.parsemis.strategy;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.parsemis.algorithms.Algorithm;
import de.parsemis.miner.chain.Extender;
import de.parsemis.miner.chain.SearchLatticeNode;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.Fragment;

/**
 * This class represents a local breadth first extension strategy.
 * 
 * @author Alexander Dreweke (dreweke@informatik.uni-erlangen.de)
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class BFSStrategy<NodeType, EdgeType> implements
		Strategy<NodeType, EdgeType> {

	public Collection<Fragment<NodeType, EdgeType>> search(
			final Algorithm<NodeType, EdgeType> algo) {
		final List<SearchLatticeNode<NodeType, EdgeType>> list = new LinkedList<SearchLatticeNode<NodeType, EdgeType>>();
		final Collection<Fragment<NodeType, EdgeType>> result = LocalEnvironment
				.env(this).getReturnSet();

		final Extender<NodeType, EdgeType> extender = algo.getExtender(0);
		assert (extender != null) : "extender == null";

		final Iterator<SearchLatticeNode<NodeType, EdgeType>> it = algo
				.initialNodes();
		while (it.hasNext()) {
			list.add(it.next());
		}

		while (!list.isEmpty()) {
			final SearchLatticeNode<NodeType, EdgeType> current = list
					.remove(0);
			list.addAll(extender.getChildren(current));

			if (current.store()) {
				current.store(result);
			} else {
				current.release();
			}

			current.finalizeIt();
		}
		return result;
	}
}
