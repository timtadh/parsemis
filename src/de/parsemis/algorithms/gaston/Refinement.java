/**
 * Created Jan 04, 2008
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
package de.parsemis.algorithms.gaston;

import de.parsemis.graph.HPGraph;

/**
 * This class represents a refinement for the Gaston algorithm.
 * 
 * It also defines an order on the refinements.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class Refinement implements Comparable<Refinement>, Cloneable {

	protected int nodeA;

	protected final int nodeB, edgeLabel;

	/**
	 * creates a new initial Refinement for a first Node
	 * 
	 * @param toLabel
	 */
	public Refinement(final int toLabel) {
		this(HPGraph.NO_NODE, HPGraph.NO_EDGE, toLabel);
	}

	/**
	 * creates a node Refinement
	 * 
	 * @param nodeA
	 *            the node the refinements is connected to
	 * @param edgeLabel
	 *            the label of the new edge
	 * @param toLabel
	 *            the label of the new node
	 */
	public Refinement(final int nodeA, final int edgeLabel, final int toLabel) {
		this.nodeA = nodeA;
		this.edgeLabel = edgeLabel;
		this.nodeB = toLabel;
	}

	public Refinement clone(final int nodeB) {
		return new Refinement(this.nodeA, this.edgeLabel, this.nodeB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Refinement o) {
		if (o.isCycleRefinement()) {
			return -1;
		}
		if (o.nodeA != nodeA) {
			return nodeA - o.nodeA;
		}
		if (o.edgeLabel != edgeLabel) {
			return edgeLabel - o.edgeLabel;
		}
		return nodeB - o.nodeB;
	}

	/**
	 * @return the depth of the DepthRefinements (else -1)
	 */
	public int getDepth() {
		return -1;
	}

	/**
	 * @return the edge label
	 */
	public int getEdgeLabel() {
		return edgeLabel;
	}

	/**
	 * @return the number of the node A
	 */
	public int getNodeA() {
		return nodeA;
	}

	/**
	 * @return the number of the node B (for cycle refinements else
	 *         <code> Graph.NO_NODE </code>)
	 */
	public int getNodeB() {
		return HPGraph.NO_NODE;
	}

	/**
	 * @return the label of the new node (<code> Graph.NO_NODE </code> for
	 *         cycles)
	 */
	public int getToLabel() {
		return nodeB;
	}

	/**
	 * @return <code> true </code> if this refinement is a CycleRefinement
	 */
	public boolean isCycleRefinement() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getNodeA() + " " + getEdgeLabel() + " " + getNodeB() + "/"
				+ getToLabel();
	}

}
