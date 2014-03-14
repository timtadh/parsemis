/**
 * Created on Dec 27, 2007
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

import de.parsemis.graph.HPGraph;

/**
 * This class is for distinguish a node Refinement from a cycle Refinement.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
public class CycleRefinement extends Refinement {

	/**
	 * creates a new CycleRefinement
	 * 
	 * @param nodeA
	 *            node this refinement connects
	 * @param edgeLabel
	 *            the label of the new edge
	 * @param nodeB
	 *            node this refinement connects
	 */
	public CycleRefinement(final int nodeA, final int edgeLabel, final int nodeB) {
		super(nodeA, edgeLabel, nodeB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.testing.gaston.Refinement#clone(int)
	 */
	@Override
	public Refinement clone(final int nodeB) {
		return new CycleRefinement(this.nodeA, this.edgeLabel, this.nodeB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Refinement o) {
		if (!o.isCycleRefinement()) {
			return 1;
		}
		if (o.nodeA != nodeA) {
			return o.nodeA - nodeA;
		}
		if (o.nodeB != nodeB) {
			return o.nodeB - nodeB;
		}
		return o.edgeLabel - edgeLabel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.Refinement#getNodeB()
	 */
	@Override
	public int getNodeB() {
		return nodeB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.Refinement#getToLabel()
	 */
	@Override
	public int getToLabel() {
		return HPGraph.NO_NODE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.Refinement#isCycleRefinement()
	 */
	@Override
	public boolean isCycleRefinement() {
		return true;
	}

}
