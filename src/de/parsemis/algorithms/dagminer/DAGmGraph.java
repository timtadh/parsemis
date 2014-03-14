/**
 * created 13.06.2006
 *
 * @by Tobias Werth (sitowert@i2.informatik.uni-erlangen.de)
 *
 * Copyright 2006 Tobias Werth
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.algorithms.dagminer;

import java.io.Serializable;
import java.util.BitSet;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Frequency;
import de.parsemis.utils.Generic;
import de.parsemis.utils.GraphUtils;

/**
 * @author Tobias Werth (sitowert@i2.informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class DAGmGraph<NodeType, EdgeType> implements
		Generic<NodeType, EdgeType>, DataBaseGraph<NodeType, EdgeType>,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static boolean DAGCHECK = true;
	private final HPGraph<NodeType, EdgeType> originalGraph;
	private final int idx;
	private final Frequency freq;

	public DAGmGraph(final HPGraph<NodeType, EdgeType> originalGraph,
			final int idx, final Frequency freq) {
		this.originalGraph = originalGraph;
		if (DAGCHECK) {
			if (!GraphUtils.isDAG(originalGraph)) {
				throw new RuntimeException("The graph "
						+ originalGraph.getName() + " contains cycles...");
			}
		}
		this.idx = idx;
		this.freq = freq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.DataBaseGraph#frequency()
	 */
	public Frequency frequency() {
		return freq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.DataBaseGraph#getEdges()
	 */
	public BitSet getEdges() {
		// TODO: remove infrequent ones??
		return (BitSet) originalGraph.getEdges().clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.DataBaseGraph#getIndex()
	 */
	public int getIndex() {
		return idx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.general.DataBaseGraph#getNodes()
	 */
	public BitSet getNodes() {
		// TODO: remove infrequent ones??
		return (BitSet) originalGraph.getNodes().clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.DataBaseGraph#toGraph()
	 */
	public Graph<NodeType, EdgeType> toGraph() {
		return originalGraph.toGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.DataBaseGraph#toHPGraph()
	 */
	public HPGraph<NodeType, EdgeType> toHPGraph() {
		return originalGraph;
	}

}