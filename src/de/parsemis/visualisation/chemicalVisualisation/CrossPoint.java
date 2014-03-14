/**
 * Created on Jun 20, 2005
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

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Diese Klasse enthaelt Informationen ueber eine Kreuzung mehrerer Ketten
 */

class CrossPoint implements DrawingUnit {

	HashMap<Integer, Integer> crossPointNeighbors;

	// HashMap chainCodes;

	HashSet<Integer> groupsElements;

	HashSet<Integer>[] groupsData;

	int[] nodes;

	int[] sizes;

	int count;

	int degree;

	int maxSize = -1;

	int secondMaxSize = -1;

	int indexOfMaxNode = -1;

	int indexOfSecondMax = -1;

	/**
	 * Constructor
	 * 
	 * @param degree
	 */
	CrossPoint(final int degree) {
		this.degree = degree;
		count = 0;
		nodes = new int[degree];
		sizes = new int[degree];
		crossPointNeighbors = new HashMap<Integer, Integer>(degree);
		// chainCodes = new HashMap(degree);
		groupsElements = new HashSet<Integer>(degree);
		initGroups();
	}

	public HashSet<Integer>[] getGroups() {
		return groupsData;
	}

	/**
	 * Die Funktion liefert die Kinder des Knotens
	 * 
	 * @param middlePoint
	 * @return children of the node
	 */
	public int[] getNodesList(final int middlePoint) {
		int node;
		final int[] result = new int[degree];
		int localIndexOfMaxNode;
		if (DataAnalyser.visitedNodes[nodes[indexOfMaxNode]] == 1) {
			localIndexOfMaxNode = indexOfSecondMax;
		} else {
			localIndexOfMaxNode = indexOfMaxNode;
		}
		for (int i = 0, j = 0; i < degree; i++) {
			node = nodes[i];
			if (DataAnalyser.visitedNodes[node] == 0) {
				if (i == localIndexOfMaxNode) {
					result[middlePoint] = node;
				} else {
					if (j == middlePoint) {
						j = (j + 1) % degree;
					} else {
						j = j % degree;
					}
					result[j++] = node;
				}
			} else {
				if (j == middlePoint) {
					j = (j + 1) % degree;
				} else {
					j = j % degree;
				}
				result[j++] = -1;
			}
		}
		return result;
	}

	/**
	 * Diese Funktion liefert die Kinder des aktuellen Knotens in einer
	 * bestimmten, fuer die horizontale Ausrichtung der Hauptkette berechneten
	 * Reihenfolge
	 * 
	 * @param middlePoint
	 * @return children of the current node
	 */
	public int[] getNodesListForHeadChain(final int middlePoint) {
		int node;
		final int[] result = new int[degree];
		boolean isMiddleSet = false;
		for (int i = 0, j = 0; i < degree; i++) {
			node = nodes[i];
			if (DataAnalyser.visitedNodes[node] == 0) {
				if ((DataAnalyser.headChain.containsNode(node))
						&& (!isMiddleSet)) {
					result[middlePoint] = node;
					isMiddleSet = true;
				} else {
					if (j == middlePoint) {
						j = (j + 1) % degree;
					} else {
						j = j % degree;
					}
					result[j++] = node;
				}
			} else {

				if (j == middlePoint) {
					j = (j + 1) % degree;
				} else {
					j = j % degree;
				}
				result[j++] = -1;

			}
		}
		return result;
	}

	/**
	 * @param node
	 * @return the size of the Branch
	 */
	public Integer getSizeOfBranch(final int node) {
		return crossPointNeighbors.get(node);
	}

	/**
	 * @param node
	 * @return the size of the Branch
	 */
	public Integer getSizeOfBranch(final Integer node) {
		return crossPointNeighbors.get(node);
	}

	@SuppressWarnings("unchecked")
	public void initGroups() {
		groupsData = new HashSet[24];
		for (int i = 0; i < 24; i++) {
			groupsData[i] = new HashSet<Integer>();
		}
	}

	/**
	 * Diese Funktion spreichert ein Kind des aktuellen Knotens und die Groesse
	 * der Seitenkette, die mit diesem Kind anfaengt.
	 * 
	 * @param node
	 * @param size
	 */

	public void saveChild(final int node, final int size) {
		nodes[count] = node;
		if (size > maxSize) {
			secondMaxSize = maxSize;
			indexOfSecondMax = indexOfMaxNode;
			maxSize = size;
			indexOfMaxNode = count;
		} else {
			if (size > secondMaxSize) {
				secondMaxSize = size;
				indexOfSecondMax = count;
			}
		}
		sizes[count++] = size;
		crossPointNeighbors.put(node, size);
	}

	/**
	 * set teh groups data
	 * 
	 * @param data
	 */
	public void setGroupsData(final HashSet<Integer>[] data) {
		groupsData = data;
	}

	/**
	 * add node to group elements
	 * 
	 * @param node
	 */

	public void setGroupsElement(final int node) {
		groupsElements.add(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String out = "{";
		for (int i = 0; i < degree; i++) {
			out += nodes[i] + "/" + sizes[i] + " ";
		}
		return out + "}, groups " + groupsElements;
	}
}
