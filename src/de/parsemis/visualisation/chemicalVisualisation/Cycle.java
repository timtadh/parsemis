/**
 * Created on Jun 3, 2005
 *
 * @by Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Copyright 2005 Olga Urzova
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.visualisation.chemicalVisualisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import de.parsemis.chemical.Bond;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Diese Klasse verwaltet Informationen ueber einen Ring des Molekuels
 * 
 */
public class Cycle implements DrawingUnit {

	int cycleIndex;

	int cycleSize;

	HashMap<Integer, int[][]> cycleNeighbors;

	HashSet<Integer>[] groupsData;

	ArrayList<Integer> nodesList = null;

	ArrayList<Integer> edgesList = null;

	/**
	 * Constructor
	 * 
	 * @param cycleIndex
	 * @param cycleNodes
	 * @param cycleEdges
	 */
	Cycle(final int cycleIndex, final ArrayList<Integer> cycleNodes,
			final ArrayList<Integer> cycleEdges) {
		this.cycleIndex = cycleIndex;
		this.nodesList = cycleNodes;
		this.edgesList = cycleEdges;
		this.cycleSize = nodesList.size();
		this.cycleNeighbors = new HashMap<Integer, int[][]>(cycleSize);
		initGroups();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.visualisation.chemicalVisualisation.DrawingUnit#getGroups()
	 */
	public HashSet<Integer>[] getGroups() {
		// TODO Auto-generated method stub
		return groupsData;
	}

	/**
	 * Deise Funktion berechnet den Innenwinkel in dem Ring
	 * 
	 * @return the interior angle
	 */
	public double getInteriorAngle() {
		return Math.PI - DataAnalyser.PItimesTwo / cycleSize;
	}

	/**
	 * Die Funktion gibt alle Ringknoten mit ihren Kindern, wenn diese nich zu
	 * dem aktuellen Ring gehoeren
	 * 
	 * @return some nodes
	 */
	public HashMap<Integer, ArrayList<Integer>> getNeighborsNotInThisCycle() {
		final HashMap<Integer, ArrayList<Integer>> result = new HashMap<Integer, ArrayList<Integer>>(
				cycleSize);
		for (int i = 0; i < cycleSize; i++) {
			final ArrayList<Integer> list = new ArrayList<Integer>(10);
			final int nodeA = nodesList.get(i).intValue();
			final int degree = DataAnalyser.m_graph.getDegree(nodeA);
			for (int d = 0; d < degree; d++) {
				final int edge = DataAnalyser.m_graph.getNodeEdge(nodeA, d);
				final int nodeB = DataAnalyser.m_graph
						.getOtherNode(edge, nodeA);
				if (DataAnalyser.visitedNodes[nodeB] == 0) {
					// Knoten ausschliesen, die zu cycles[]
					// gehoeren
					// int bitmask = DataAnalyser.cyclesData[0][nodeB];

					if (!DataAnalyser.graphCycleInfo.isNodeInCycle(nodeB,
							cycleIndex - 1)) {
						list.add(nodeB);
					}
					// if ((bitmask & (1 << (cycleIndex - 1))) != (1 <<
					// (cycleIndex - 1)))
					// list.add(new Integer(nodeB));
					// }
				}
			}
			if (!list.isEmpty()) {
				result.put(nodeA, list);
			}
		}
		return result;
	}

	/**
	 * Diese Funktion liefert nach ihre Nachbarschaftsbeziehung geordnete
	 * Ringknoten
	 * 
	 * @param nodeIndex
	 *            des Ringknotens
	 * @param isForSimpleCycle
	 *            Zugehooerigkeit des Ringes zu einem Block
	 * @return ring nodes
	 */
	public int[] getOrderedNodes(final int nodeIndex,
			final boolean isForSimpleCycle) {
		int nodeB, n;
		final int[] result = new int[cycleSize];
		final Vector<Integer> visit = new Vector<Integer>(cycleSize);
		int j = 0;
		int nodeA = nodesList.get(j++).intValue();
		int edge = DataAnalyser.m_graph.getEdge(nodeIndex, nodeA);
		if (isForSimpleCycle) {
			while (edge == -1) {
				nodeA = nodesList.get(j++).intValue();
				edge = DataAnalyser.m_graph.getEdge(nodeIndex, nodeA);
			}
		} else {
			for (int i = 0; i < cycleSize; i++) {
				nodeB = nodesList.get(i).intValue();
				if (DataAnalyser.m_graph.getEdge(nodeIndex, nodeB) > -1) {
					if (DataAnalyser.visitedNodes[nodeB] == 1) {
						nodeA = nodeB;
						i = cycleSize;
					} else {
						nodeA = nodeB;
					}
				}
			}
		}
		result[0] = nodeA;
		result[cycleSize - 1] = nodeIndex;
		visit.add(nodeIndex);
		visit.add(nodeA);
		for (int i = 1; i < (cycleSize - 1); i++) {
			n = 0;
			Integer B = nodesList.get(n);
			nodeB = B.intValue();
			while ((visit.indexOf(B) > -1)
					|| (DataAnalyser.m_graph.getEdge(nodeA, nodeB) == -1)) {
				B = nodesList.get(++n);
				nodeB = B.intValue();
			}
			result[i] = nodeB;
			visit.add(B);
			nodeA = nodeB;
		}
		return result;
	}

	/**
	 * Diese Funktion liefert fuer einen Ring seine Ringknoten in einer
	 * bestimmten von der Richtung der Seitenkette abhaengigen Reihenfolge
	 * 
	 * @param index
	 * @param factor
	 * @param mainDir
	 * @return ring nodes
	 */
	public int[] getOrderForThisDirection(final int index, final int factor,
			final int mainDir) {
		final int[] nodes = getOrderedNodes(index, true);
		int[] result = nodes;
		final int zero = getSizeOfMaxChild(nodes[0]);
		final int first = getSizeOfMaxChild(nodes[1]);
		final int bevorlast = getSizeOfMaxChild(nodes[cycleSize - 3]);
		final int last = getSizeOfMaxChild(nodes[cycleSize - 2]);
		boolean isTop = true;
		boolean isFree;
		switch (factor) {
		case 1:
			if (mainDir == 1) {
				isTop = !isTop;
			}
			isFree = DataAnalyser.isStillSpaceLeft(isTop, mainDir);
			if (first < bevorlast) {
				if (isFree(zero, isFree)) {
					result = reverse(nodes);
				}

			} else {
				if (!isFree(last, isFree)) {
					result = reverse(nodes);
				}
			}
			break;
		case -1:

			if (mainDir == -1) {
				isTop = !isTop;
			}
			isFree = DataAnalyser.isStillSpaceLeft(isTop, mainDir);
			if (bevorlast < first) {
				if (isFree(last, isFree)) {
					result = reverse(nodes);
				}
			} else {
				if (!isFree(zero, isFree)) {
					result = reverse(nodes);
				}
			}
			break;
		}
		for (int i = 0; i < result.length - 1; i++) {
			final int node = result[i];
			if (DataAnalyser.headChain.containsNode(node)) {
				saveBranchSize(result, i, mainDir);
			}
		}
		if (index == DataAnalyser.initPoint) {
			saveBranchSize(result, cycleSize - 1, 1);
		}

		return result;
	}

	/**
	 * Die Funktion gibt die maximale Groesse der Kindern von einem Ringknoten
	 * 
	 * @param cycleNode
	 * @return maximal size of the children
	 */
	public int getSizeOfMaxChild(final int cycleNode) {
		final int[][] children = cycleNeighbors.get(cycleNode);
		if (children == null) {
			return 0;
		}
		int max = children[0][1];

		for (int i = 1; i < (children.length / 2); i++) {
			if (children[i][1] > max) {
				max = children[i][1];
			}
		}
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.visualisation.chemicalVisualisation.DrawingUnit#initGroups()
	 */
	@SuppressWarnings("unchecked")
	public void initGroups() {
		groupsData = new HashSet[24];
		for (int i = 0; i < 24; i++) {
			groupsData[i] = new HashSet<Integer>();
		}
		groupsData[22].add(cycleIndex);
	}

	/**
	 * Diese Funktion ueberprueft ob alle Bindungen in dem Ring aromatisch sind
	 * 
	 * @return <code>true</code>, if all bonds are aromatical
	 */
	public boolean isAromatic() {
		for (int i = 0; i < edgesList.size(); i++) {
			final Bond b = DataAnalyser.m_graph.getEdgeLabel(edgesList.get(i)
					.intValue());
			if (b.bindings() != 4) {
				return false;
			}

			// if (DataAnalyser.m_graph.getEdgeBondLabel(((Integer)
			// edgesList.get(i))
			// .intValue()) != 4)
			// return false;
		}
		return true;
	}

	/**
	 * Diese Funktion ueberprueft ob eine Kante zu dem Ring gehoert
	 * 
	 * @param edge
	 * @return <code>true</code>, if the edge contains to the cycle
	 */
	public boolean isEdgeInCycle(final int edge) {
		return edgesList.contains(edge);
	}

	private boolean isFree(final int size, final boolean isFree) {
		if (isFree) {
			return true;
		}
		if (size <= 2) {
			return true;
		}
		return false;
	}

	/**
	 * Diese Funktion ueberprueft ob ein Knoten aus diesem Ring ist
	 * 
	 * @param node
	 * @return <code>true</code>, if the node is the cycle
	 */
	public boolean isNodeInCycle(final int node) {
		return nodesList.contains(node);
	}

	/**
	 * Diese Hilfsfunktion aendert die Reihenfolge der Knoten in einer Liste mit
	 * den Ringknoten
	 * 
	 * @param list
	 * @return swaped nodes
	 */
	private int[] reverse(final int[] list) {
		final int size = list.length / 2;
		for (int i = 0, j = list.length - 2; i < size; i++, j--) {
			final int tmp = list[i];
			list[i] = list[j];
			list[j] = tmp;
		}
		return list;
	}

	/**
	 * Diese Funktion berechnet die fuer die Kollisionsbehandlung in einem
	 * Molekuel mit Ringen benoetigten Parameter
	 * 
	 * @param result
	 * @param i
	 *            index des Knotens in der Liste mit den Ringknoten, fuer den
	 *            die Groesse der Kinder ermittelt wird
	 * @param mainDir
	 *            Die Richtung, in welche das Molekuel geyeichnet wird
	 */
	public void saveBranchSize(final int[] result, final int i,
			final int mainDir) {
		int branchSizeBottom = 1;
		int middle = cycleSize / 4;
		for (int j = (i - 1 + cycleSize) % cycleSize, count = 0; count <= middle; j = (j - 1 + cycleSize)
				% cycleSize, count++) {
			final int size = getSizeOfMaxChild(result[j]);
			if (size == 0) {
				branchSizeBottom += 1;
			} else {
				branchSizeBottom += size;
				break;
			}
		}

		int branchSizeTop = 1;
		for (int j = (i + 1) % cycleSize, count = 0; count <= middle; j = (j + 1)
				% cycleSize, count++) {
			final int size = getSizeOfMaxChild(result[j]);
			if (size == 0) {
				branchSizeTop += 1;
			} else {
				branchSizeTop += size;
				break;
			}
		}
		if (i == cycleSize - 1) {
			middle = branchSizeBottom;
			branchSizeBottom = branchSizeTop;
			branchSizeTop = middle;
		}
		DataAnalyser.saveChainSizeForCycle(branchSizeTop, branchSizeBottom,
				mainDir);
	}

	/**
	 * Diese Funktion speichert die Kinder eines Ringknotens
	 * 
	 * @param cycleNode
	 * @param children
	 */
	public void saveChild(final Integer cycleNode, final int[][] children) {
		cycleNeighbors.put(cycleNode, children);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String out = "";
		Integer current;
		out += "Cycle number " + cycleIndex + ":";
		for (int i = 0; i < cycleSize; i++) {
			current = nodesList.get(i);
			if (current == null) {
				out += "null()";
			} else {

				out += current.intValue() + ", ";

			}
		}
		out += "edges: ";
		for (int i = 0; i < edgesList.size(); i++) {
			current = edgesList.get(i);
			if (current == null) {
				out += "null ";
			} else {
				out += current.intValue() + " ";
			}
		}
		out += " isAromatic: " + isAromatic() + "\n";
		return out;
	}

}