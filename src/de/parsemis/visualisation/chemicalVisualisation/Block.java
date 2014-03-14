/**
 * Created on Sep 19, 2005
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
import java.util.Iterator;
import java.util.Vector;

/**
 * Diese Klasse enthaelt Informationen ueber einen Block, d.h. die miteinander
 * verknuepften Ringe
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 */

public class Block implements DrawingUnit {

	/**
	 * Eine Hilfsklasse um die in einem Block berechnete Koordinaten zu
	 * speichern
	 * 
	 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
	 */
	static class BlockPoint {
		float x, y;

		BlockPoint(final float cur_x, final float cur_y) {
			x = cur_x;
			y = cur_y;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[" + x + ", " + y + "]";
		}
	}

	private final int blockIndex;

	private final int blockSize;

	private final ArrayList<Integer> blockMembers;

	private int beginOfBlock;

	private final HashMap<Integer, BlockPoint> blockPoints;

	private final HashMap<Integer, Double> regularInteriorAngles;

	HashSet<Integer>[] groupsData;

	/**
	 * Constructor
	 * 
	 * @param blockIndex
	 * @param members
	 *            Ringe aus diesem Block
	 */
	public Block(final int blockIndex, final ArrayList<Integer> members) {
		this.blockIndex = blockIndex;
		blockMembers = members;
		blockSize = blockMembers.size();
		blockPoints = new HashMap<Integer, BlockPoint>();
		regularInteriorAngles = new HashMap<Integer, Double>(blockSize);
		initGroups();
	}

	/**
	 * Diese Hilfsfunktion berechnet den Winkel zwischen zwei Vektoren.
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return an angle
	 */
	public double getAlpha(final double x0, final double y0, final double x1,
			final double y1, final double x2, final double y2) {
		final double[] vector1 = { x1 - x0, y1 - y0 };
		final double[] vector2 = { x2 - x0, y2 - y0 };
		final double skalarproduct = vector1[0] * vector2[0] + vector1[1]
				* vector2[1];
		final double length1 = Math.sqrt(vector1[0] * vector1[0] + vector1[1]
				* vector1[1]);
		final double length2 = Math.sqrt(vector2[0] * vector2[0] + vector2[1]
				* vector2[1]);
		return Math.acos(skalarproduct / (length1 * length2));
	}

	/**
	 * Diese Funktion liefert den Index eines Ringes aus diesem Block, der als
	 * erster bei der Kordinatenvergabe bearbeitet wird.
	 * 
	 * @return the first index
	 */
	public Integer getBeginCycle() {
		int edge, nodeB;
		for (int i = 0; i < DataAnalyser.m_graph.getDegree(beginOfBlock); i++) {
			edge = DataAnalyser.m_graph.getNodeEdge(beginOfBlock, i);
			nodeB = DataAnalyser.m_graph.getOtherNode(edge, beginOfBlock);
			// Der Knoten B ist noch nicht besucht
			if ((DataAnalyser.visitedNodes[nodeB] == 0)
					&& (DataAnalyser.edgeCycleMembership[edge].size() == 1)) {
				return DataAnalyser.edgeCycleMembership[edge].get(0);
			}
		}
		return null;
	}

	/**
	 * @return the begin of the block
	 */
	public int getBeginOfBlock() {
		return beginOfBlock;
	}

	/**
	 * @return the block index
	 */
	public int getBlockIndex() {
		return blockIndex;
	}

	/**
	 * @return the cycles from this block
	 */
	public ArrayList<Integer> getCyclesFromThisBlock() {
		return blockMembers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.visualisation.chemicalVisualisation.DrawingUnit#getGroups()
	 */
	public HashSet<Integer>[] getGroups() {
		return groupsData;
	}

	/**
	 * gibt den vorher gespeicherten Innenwinkel eines Ringes zurueck
	 * 
	 * @param cycleIndex
	 * @return an interior angle
	 */
	public double getInteriorAngle(final Integer cycleIndex) {
		return regularInteriorAngles.get(cycleIndex).doubleValue();
	}

	/**
	 * berechnet die Summe der Innenwinkeln fuer die bestimmten Ringe aus diesem
	 * Block
	 * 
	 * @param cycles
	 * @return sums of interior angels
	 */
	public double getInteriorAngleForBeginOfBlock(
			final ArrayList<Integer> cycles) {
		double result = 0;
		for (int i = 0; i < cycles.size(); i++) {
			final Integer cycleIndex = cycles.get(i);
			result += regularInteriorAngles.get(cycleIndex).doubleValue();
		}
		return result;
	}

	/**
	 * Diese Funktion gibt alle Knoten mit den Kindern zurueck, wenn diese
	 * Kinder nicht zu diesem Block gehoeren
	 * 
	 * @return something
	 */
	public HashMap<Integer, ArrayList<Integer>> getNeighborsNotInThisBlock() {
		Integer cycleIndex;
		final HashSet<Integer> helper = new HashSet<Integer>();
		for (int i = 0; i < blockSize; i++) {
			cycleIndex = blockMembers.get(i);
			final Cycle cycle = DataAnalyser.cyclesVector.get(cycleIndex
					.intValue() - 1);
			final ArrayList<Integer> nodes = cycle.nodesList;
			helper.addAll(nodes);
			regularInteriorAngles.put(cycleIndex, new Double(cycle
					.getInteriorAngle()));
		}
		final HashMap<Integer, ArrayList<Integer>> result = new HashMap<Integer, ArrayList<Integer>>(
				helper.size());
		final Iterator<Integer> iter = helper.iterator();
		while (iter.hasNext()) {
			final ArrayList<Integer> list = new ArrayList<Integer>(10);
			final int nodeA = iter.next().intValue();
			final int degree = DataAnalyser.m_graph.getDegree(nodeA);
			for (int d = 0; d < degree; d++) {
				final int edge = DataAnalyser.m_graph.getNodeEdge(nodeA, d);
				final int nodeB = DataAnalyser.m_graph
						.getOtherNode(edge, nodeA);
				if (DataAnalyser.visitedNodes[nodeB] == 0) {
					// Knoten ausschliesen, die zu cycles[]
					// gehoeren
					if (DataAnalyser.graphCycleInfo.isNodeInCycles(nodeB)) {
						int j = 0;
						int check = 0;
						while (j < blockSize) {
							if (!DataAnalyser.graphCycleInfo.isNodeInCycle(
									nodeB, blockMembers.get(j).intValue() - 1)) {
								check++;
							}
							j++;
						}
						if (check == blockSize) {
							list.add(nodeB);
						}
					} else {
						list.add(nodeB);
					}

				}
			}
			if (!list.isEmpty()) {
				result.put(nodeA, list);
			}
		}
		System.out.println("result = " + result);
		return result;
	}

	/**
	 * Die Funktion gibt die schon berechneten Koordinaten eines Knotens aus
	 * diesem Block
	 * 
	 * @param node
	 * @return coordinates of a node
	 */
	public float[] getXY(final int node) {
		final float result[] = new float[2];
		final BlockPoint p = blockPoints.get(node);
		if (p == null) {
			return null;
		} else {
			result[0] = p.x;
		}
		result[1] = p.y;
		return result;
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
		// groupsData[22].add(new Integer(blockIndex));
		groupsData[22].addAll(blockMembers);
	}

	/**
	 * ueberprueft ob ein Ring aus diesem Block stamm
	 * 
	 * @param cycleIndex
	 * @return <code>true</code>, if the given ring is part of the block
	 */
	public boolean isInThisBlock(final Integer cycleIndex) {
		if (blockMembers.contains(cycleIndex)) {
			return true;
		}
		return false;
	}

	/**
	 * speichert Kinder eines Knotens, die nicht zu diesem Block gehoeren
	 * 
	 * @param node
	 * @param children
	 */
	public void saveChild(final Integer node, final int[][] children) {
		final ArrayList<Integer> cycles = DataAnalyser.nodeCycleMembership[node
				.intValue()];
		for (int i = 0; i < cycles.size(); i++) {
			final Integer cycleIndex = cycles.get(i);
			final Cycle cycle = DataAnalyser.cyclesVector.get(cycleIndex
					.intValue() - 1);
			cycle.saveChild(node, children);
		}
	}

	/**
	 * Diese Funktion generiert die aromatischen Bindungen innenhalb eines
	 * regulaeren oder nichtregulaeren Rings aus dem Block
	 * 
	 * @param list
	 * @return something
	 */
	public Vector<VectorElement> setAromaticBonds(final int[] list) {
		final Vector<VectorElement> result = new Vector<VectorElement>();
		final int size = list.length;
		final BlockPoint p0_init = blockPoints.get(list[list.length - 1]);
		final BlockPoint p1_init = blockPoints.get(list[0]);
		BlockPoint p0 = p0_init;
		BlockPoint p1 = p1_init;
		BlockPoint p2 = null;
		final double[] angles = new double[list.length];
		for (int i = 0; i < size; i++) {
			p2 = blockPoints.get(list[(i + 1) % size]);
			angles[i] = getAlpha(p1.x, p1.y, p0.x, p0.y, p2.x, p2.y) / 2.0;
			p0 = p1;
			p1 = p2;
		}
		p1 = p1_init;
		for (int i = 1; i < size; i++) {
			p2 = blockPoints.get(list[i]);
			result.add(new VectorElement(p1.x, p1.y, p2.x, p2.y, 5, 0, 0, 0, 0,
					angles[i - 1], angles[i]));
			p1 = p2;
		}
		p2 = p1_init;
		result.add(new VectorElement(p1.x, p1.y, p2.x, p2.y, 5, 0, 0, 0, 0,
				angles[size - 1], angles[0]));
		return result;
	}

	/**
	 * sets the begin of the block
	 * 
	 * @param bob
	 */
	public void setBeginOfBlock(final int bob) {
		beginOfBlock = bob;
	}

	/**
	 * insert a new point for the given node n
	 * 
	 * @param n
	 * @param x
	 * @param y
	 */
	public void setNodeCoordinates(final int n, final float x, final float y) {
		blockPoints.put(n, new BlockPoint(x, y));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String out = "blockIndex:" + blockIndex + ", Cycles: ";
		for (int i = 0; i < blockMembers.size(); i++) {
			out += blockMembers.get(i) + " ";
		}
		out += "\n";
		return out;
	}

}
