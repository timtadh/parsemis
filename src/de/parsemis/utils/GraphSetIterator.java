/**
 * Created on 04.01.2008
 *
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * Copyright 2008 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.utils;

import java.util.BitSet;
import java.util.Iterator;

import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;

/**
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
public class GraphSetIterator<NodeType, EdgeType> implements
		Iterator<DataBaseGraph<NodeType, EdgeType>> {
	final LocalEnvironment<NodeType, EdgeType> env;

	final BitSet graphSet;

	int nextBit;

	public GraphSetIterator(final BitSet graphSet,
			final LocalEnvironment<NodeType, EdgeType> env) {
		this.env = env;
		this.graphSet = graphSet;
		nextBit = graphSet.nextSetBit(0);
	}

	public boolean hasNext() {
		return nextBit >= 0;
	}

	public DataBaseGraph<NodeType, EdgeType> next() {
		final DataBaseGraph<NodeType, EdgeType> ret = env.getGraph(nextBit);
		nextBit = graphSet.nextSetBit(nextBit + 1);
		return ret;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}