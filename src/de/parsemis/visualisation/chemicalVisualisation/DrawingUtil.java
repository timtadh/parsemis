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

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DrawingUtil {

	/**
	 * prints the chain to System.out
	 * 
	 * @param chains
	 */
	public static void printChains(final ArrayList<Chain> chains) {
		System.out.println("CHAINS::");
		for (int i = 0; i < chains.size(); i++) {

			System.out.println("[" + i + "] " + chains.get(i));
			System.out.println();
		}
	}

	/**
	 * prints the cross point to System.out
	 * 
	 * @param points
	 */
	// TODO: what map is it?
	public static void printCrossPoints(final HashMap<?, ?> points) {
		System.out.println(points);
	}

	/**
	 * prints the current chain data to System.out
	 * 
	 * @param data
	 */
	public static void printCurrentChainData(final ArrayList<Chain> data) {
		for (int i = 0; i < data.size(); i++) {
			System.out.println(data.get(i));
		}
	}

	/**
	 * prints the cycles data to System.out
	 * 
	 * @param cyclesData
	 */
	public static void printCyclesData(final int[][] cyclesData) {
		System.out.println("Jetzt kommt die cyclesData");
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < cyclesData[i].length; j++) {
				System.out.print(cyclesData[i][j] + " ");
			}
			System.out.print("\n");
		}
		System.out.println("Ende von cyclesData");
	}

	/**
	 * prints the cycle vectores to System.out
	 * 
	 * @param cyclesVector
	 */
	public static void printCyclesVector(final Vector<Cycle> cyclesVector) {
		System.out.println("Jetzt kommt cyclesVector");
		for (int i = 0; i < cyclesVector.size(); i++) {
			System.out.print(cyclesVector.get(i));
		}
		System.out.println("Ende von cyclesVector");
	}

	/**
	 * prints edge cycle membership to System.out
	 * 
	 * @param cyclesData
	 * @param nodeCycleMembership
	 */
	public static void printEdgeCycleMembership(final int[][] cyclesData,
			final ArrayList<Integer>[] nodeCycleMembership) {
		for (int i = 0; i < cyclesData[1].length; i++) {
			System.out.print("Kante " + i + ":");
			for (int j = 0; j < nodeCycleMembership[i].size(); j++) {
				System.out
						.print(nodeCycleMembership[i].get(j).intValue() + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * prints the groups to System.out
	 * 
	 * @param groups
	 */
	// TODO: what sets are that?
	public static void printGroups(final ArrayList<HashSet<?>[]> groups) {
		for (int i = 0; i < groups.size(); i++) {
			final HashSet<?>[] current = groups.get(i);
			System.out.print("[" + i + "]");
			System.out.println(current);
		}
	}

	/**
	 * prints the hash set array to System.out
	 * 
	 * @param array
	 */
	// TODO: what set is it?
	public static void printHashSetArray(final HashSet<?>[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i] + ", ");
		}
		System.out.println();
	}

	/**
	 * prints the int array to System.out
	 * 
	 * @param a
	 */
	public static void printIntArray(final int[] a) {
		for (int i = 0; i < a.length; i++) {
			System.out.print(a[i] + " ");
		}
		System.out.println();
	}

	/**
	 * prints the int matrix to System.out
	 * 
	 * @param matrix
	 * @param x
	 * @param y
	 */
	public static void printIntMatrix(final int[][] matrix, final int x,
			final int y) {
		System.out.println("Matrix commt:");
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * prints the node cycle membership to System.out
	 * 
	 * @param cyclesData
	 * @param nodeCycleMembership
	 */
	public static void printNodeCycleMembership(final int[][] cyclesData,
			final ArrayList<Integer>[] nodeCycleMembership) {
		for (int i = 0; i < cyclesData[0].length; i++) {
			System.out.print("Knoten " + i + ":");
			for (int j = 0; j < nodeCycleMembership[i].size(); j++) {
				System.out.print((nodeCycleMembership[i].get(j)).intValue()
						+ " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * prints the visited edges to System.out
	 * 
	 * @param array
	 */
	public static void printVisitedEdges(final byte[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i] + "");
		}
		System.out.println();
	}

	/**
	 * prints the visited array to System.out
	 * 
	 * @param visited
	 * @param nodesNumber
	 */
	public void printVisitedArray(final byte[] visited, final int nodesNumber) {
		for (int i = 0; i < nodesNumber; i++) {
			System.out.print(visited[i] + " ");
		}
		System.out.println();
	}
}
