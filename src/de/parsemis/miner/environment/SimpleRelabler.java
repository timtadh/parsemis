/**
 * created: Jan 14, 2008
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
package de.parsemis.miner.environment;

import java.util.ArrayList;

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
public class SimpleRelabler<NodeType, EdgeType> implements
		Relabler<NodeType, EdgeType> {

	final ArrayList<NodeType> nodes = new ArrayList<NodeType>();

	final ArrayList<EdgeType> edges = new ArrayList<EdgeType>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.environment.Relabler#getEdgeLabel(int)
	 */
	public EdgeType getEdgeLabel(final int idx) {
		return edges.get(idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.environment.Relabler#getEdgeLabelIndex(java.lang.Object)
	 */
	public int getEdgeLabelIndex(final EdgeType edge) {
		final int ret = edges.indexOf(edge);
		if (ret == -1) {
			edges.add(edge);
			return edges.size() - 1;
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.environment.Relabler#getNodeLabel(int)
	 */
	public NodeType getNodeLabel(final int idx) {
		return nodes.get(idx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.environment.Relabler#getNodeLabelIndex(java.lang.Object)
	 */
	public int getNodeLabelIndex(final NodeType node) {
		final int ret = nodes.indexOf(node);
		if (ret == -1) {
			nodes.add(node);
			return nodes.size() - 1;
		}
		return ret;
	}

}
