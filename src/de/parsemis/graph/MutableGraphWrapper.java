/**
 * created May 31, 2006
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
package de.parsemis.graph;

/**
 * A wrapper to use MutableGraphs like normal HPMutableGraphs.
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
class MutableGraphWrapper<NodeType, EdgeType> extends
		GraphWrapper<NodeType, EdgeType> implements
		HPMutableGraph<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4466217199673722623L;

	/**
	 * creates a wrapper for the given master graph
	 * 
	 * @param master
	 */
	MutableGraphWrapper(final MutableGraph<NodeType, EdgeType> master) {
		super(master);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#addEdgeIndex(int, int, EdgeType,
	 *      int)
	 */
	public int addEdgeIndex(final int nodeAIdx, final int nodeBIdx,
			final EdgeType label, final int direction) {
		edges = null;
		partitions = null;
		final MutableGraph<NodeType, EdgeType> master = (MutableGraph<NodeType, EdgeType>) super.master;
		final Edge<NodeType, EdgeType> edge = master.addEdge(master
				.getNode(nodeAIdx), master.getNode(nodeBIdx), label, direction);
		return edge == null ? -1 : edge.getIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#addNodeAndEdgeIndex(int, NodeType,
	 *      EdgeType, int)
	 */
	public int addNodeAndEdgeIndex(final int nodeAIdx,
			final NodeType nodeLabel, final EdgeType edgeLabel,
			final int direction) {
		nodes = null;
		edges = null;
		partitions = null;
		final MutableGraph<NodeType, EdgeType> master = (MutableGraph<NodeType, EdgeType>) super.master;
		return master.addNodeAndEdge(master.getNode(nodeAIdx), nodeLabel,
				edgeLabel, direction).getIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#addNodeIndex(NodeType)
	 */
	public int addNodeIndex(final NodeType label) {
		nodes = null;
		partitions = null;
		final MutableGraph<NodeType, EdgeType> master = (MutableGraph<NodeType, EdgeType>) super.master;
		return master.addNode(label).getIndex();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.utils.Cloneable#clone()
	 */
	@Override
	public HPGraph<NodeType, EdgeType> clone() {
		return new MutableGraphWrapper<NodeType, EdgeType>(
				((MutableGraph<NodeType, EdgeType>) master.clone()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#removeEdge(int)
	 */
	public boolean removeEdge(final int edgeIdx) {
		edges = null;
		partitions = null;
		final MutableGraph<NodeType, EdgeType> master = (MutableGraph<NodeType, EdgeType>) super.master;
		return master.removeEdge(master.getEdge(edgeIdx));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.graph.HPMutableGraph#removeNode(int)
	 */
	public boolean removeNode(final int nodeIdx) {
		nodes = null;
		partitions = null;
		final MutableGraph<NodeType, EdgeType> master = (MutableGraph<NodeType, EdgeType>) super.master;
		return master.removeNode(master.getNode(nodeIdx));
	}

}
