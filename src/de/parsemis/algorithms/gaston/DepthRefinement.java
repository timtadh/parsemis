/**
 * Created Jan 03, 2008
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
package de.parsemis.algorithms.gaston;

/**
 * This class represents a Depth extension for trees in the
 * depth/edgeLabel/nodeLabel notation
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
public class DepthRefinement extends Refinement {

	/** the previous Refinement in the DepthSequence */
	protected DepthRefinement prev;

	final int[] rmpNodes;

	/**
	 * the rightmost child of the represented nod, means next refinement on the
	 * right most path
	 */
	// protected DepthRefinement right;
	/**
	 * creates a new DepthRefinement
	 * 
	 * @param depth
	 *            the depth of the new refinement
	 * @param edgeLabel
	 *            the label of the new edge
	 * @param nodeLabel
	 *            the label of the new node
	 * @param rmpNodes
	 */
	public DepthRefinement(final int depth, final int edgeLabel,
			final int nodeLabel, final int[] rmpNodes) {
		super(depth, edgeLabel, nodeLabel);
		this.rmpNodes = rmpNodes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.testing.gaston.Refinement#clone(int)
	 */
	@Override
	public Refinement clone(final int nodeB) {
		final int[] rmp = rmpNodes.clone();
		rmp[nodeA] = nodeB;
		return new DepthRefinement(this.nodeA, this.edgeLabel, this.nodeB, rmp);
	}

	public int compareLabels(final Refinement o) {
		if (o.isCycleRefinement()) {
			return -1;
		}
		if (this.edgeLabel != o.edgeLabel) {
			return this.edgeLabel - o.edgeLabel;
		}
		return this.nodeB - o.nodeB;
	}

	/**
	 * compares this refinement with a refinement with the given values
	 * 
	 * @param depth
	 * @param edgeLabel
	 * @param toLabel
	 * @return <code> -1 </code> if this is lesser,<code> 1 </code> if greater
	 *         else <code> 0 </code>
	 */
	public int compareTo(final int depth, final int edgeLabel, final int toLabel) {
		return compareTo(depth, edgeLabel, toLabel, 0);
	}

	/**
	 * compares this refinement with a refinement with the given values
	 * 
	 * @param depth
	 * @param edgeLabel
	 * @param toLabel
	 * @param cmov
	 *            the number added to depth
	 * @return <code> -1 </code> if this is lesser,<code> 1 </code> if greater
	 *         else <code> 0 </code>
	 */
	public int compareTo(final int depth, final int edgeLabel,
			final int toLabel, final int cmov) {
		if (this.nodeA != depth + cmov) {
			return this.nodeA - depth - cmov;
		}
		if (this.edgeLabel != edgeLabel) {
			return this.edgeLabel - edgeLabel;
		}
		return this.nodeB - toLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.Refinement#compareTo(de.parsemis.algorithms.gaston.Refinement)
	 */
	@Override
	public int compareTo(final Refinement o) {
		if (o.isCycleRefinement()) {
			return -1;
		}
		return compareTo(o.getDepth(), o.edgeLabel, o.nodeB, 0);
	}

	/**
	 * compares this refinement with an other, whose depth is shifted
	 * 
	 * @param other
	 * @param cmov
	 *            the number added to others depth
	 * @return <code> -1 </code> if this is lesser,<code> 1 </code> if greater
	 *         else <code> 0 </code>
	 */
	public int compareTo(final Refinement other, final int cmov) {
		if (other.isCycleRefinement()) {
			return -1;
		}
		return compareTo(other.getDepth(), other.edgeLabel, other.nodeB, cmov);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.Refinement#getDepth()
	 */
	@Override
	public int getDepth() {
		return nodeA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.Refinement#getNodeA()
	 */
	@Override
	public int getNodeA() {
		return rmpNodes[nodeA - 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.Refinement#toString()
	 */
	@Override
	public String toString() {
		return getDepth() + "," + getEdgeLabel() + "," + getToLabel();
		// return "d"+getDepth()+"/n"+getNodeA() + " l" + getEdgeLabel() + " n"
		// + getNodeB() + "/l"
		// + getToLabel();
	}
}
