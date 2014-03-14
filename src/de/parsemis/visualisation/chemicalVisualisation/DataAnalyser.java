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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.parsemis.chemical.Atom;
import de.parsemis.chemical.Bond;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPListGraph.Factory;
import de.parsemis.parsers.SmilesParser;
import de.parsemis.utils.GraphCycleInformation;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * This class ... TODO
 * 
 */
public class DataAnalyser {

	/** */
	public static Vector<Cycle> cyclesVector;

	private static int nodesNumber;

	private static int edgesNumber;

	/** */
	public static HPGraph<Atom, Bond> m_graph;

	/** */
	public static byte[] visitedNodes;

	/** */
	public static Chain headChain;

	// Konstanten
	/** */
	public static final float PItimesTwo = (float) (Math.PI * 2);

	/** */
	public static final float twoThirdsPI = (float) (2.0 / 3.0 * Math.PI);

	/** */
	public static final float PIdividedBySix = (float) (Math.PI / 6.0);

	/** */
	public static final float PIdividedByTwo = (float) (Math.PI / 2.0);

	/** */
	public static final float TwoPIminus30 = (float) (PItimesTwo - Math.PI / 6.0);

	// in 0.Spalte steht die Anzahl von Zyklen, in den weiteren Spalten ist
	// die Angehoerigheit zu dem Ring markiert
	/** */
	public static ArrayList<Integer>[] nodeCycleMembership;

	/** */
	public static ArrayList<Integer>[] edgeCycleMembership;

	// Hilfsgroessen fuer die Funktion initChainProperties()
	/** */
	public static byte[] visitedEdges;

	/** */
	public static ArrayList<Chain> chains;

	/** */
	public static HashMap<Integer, Integer> topBranchesRight;

	/** */
	public static HashMap<Integer, Integer> bottomBranchesRight;

	/** */
	public static HashMap<Integer, Integer> topBranchesLeft;

	/** */
	public static HashMap<Integer, Integer> bottomBranchesLeft;

	/** */
	public static int leftCounter = 0;

	/** */
	public static int rightCounter = 0;

	public static GraphCycleInformation<Atom, Bond> graphCycleInfo;

	/** */
	public static int initPoint;

	/**
	 * Diese Funktion ueberprueft, ob es noch freien Raum ober(unter-)halb der
	 * Hauptkette gibt.
	 * 
	 * @param isTop
	 * @param mainDirection
	 * @return <code>true</code>, if there ist enought space
	 */
	public static boolean isStillSpaceLeft(final boolean isTop,
			final int mainDirection) {
		HashMap<Integer, Integer> branches = new HashMap<Integer, Integer>();
		int counter;
		if (mainDirection == 1) {
			counter = rightCounter;
			if (isTop) {
				branches = topBranchesRight;
			} else {
				branches = bottomBranchesRight;
			}

		} else {
			counter = leftCounter;
			if (isTop) {
				branches = topBranchesLeft;
			} else {
				branches = bottomBranchesLeft;
			}

		}
		Integer value = branches.get(counter);
		if (value != null) {
			if (value.intValue() > 2) {
				return false;
			}
			return true;
		}
		value = branches.get(counter - 1);
		if ((value == null) || (value.intValue() <= 2)) {
			return true;
		}
		return false;

	}

	/**
	 * save something
	 * 
	 * @param chainSizeTop
	 * @param chainSizeBottom
	 * @param mainDirection
	 */
	public static void saveChainSizeForCycle(final int chainSizeTop,
			final int chainSizeBottom, final int mainDirection) {
		if (mainDirection == 1) {
			topBranchesRight.put(++rightCounter, chainSizeTop);
			bottomBranchesRight.put(rightCounter, chainSizeBottom);
		} else {
			topBranchesLeft.put(++leftCounter, chainSizeTop);
			bottomBranchesLeft.put(leftCounter, chainSizeBottom);
		}
	}

	private final Vector<VectorElement> data;

	private Vector<Integer> visitedCycles;

	// Hilfsgroessen fuer die Anpassung des Bildes an JPanel
	private float minx = Float.MAX_VALUE, maxx = Float.MIN_VALUE;

	private float miny = Float.MAX_VALUE, maxy = Float.MIN_VALUE;

	private float xDifference, yDifference;

	private ArrayList<DrawingNode> redrawQueue;

	/** */

	private HashMap<Integer, CrossPoint> crossPoints;

	private ArrayList<Integer> chainsEndPoints;

	private ArrayList<Integer> nodesNotInCycles;

	private HashMap<Integer, ArrayList<Integer>> neighborsNotInBlock;

	private FunctionalGroupsProperties<Atom, Bond> groupsProps;

	private int startNode;

	private int cyclesNumberInMolecule;

	private Chain twoBiggestChildren;

	private int blockCounter = 0;

	private ArrayList<Block> blocksList;

	/**
	 * Constructor Ein Graph-Object wird visualisiert. NodeType
	 * 
	 * @param graph
	 */
	@SuppressWarnings("unchecked")
	public DataAnalyser(final HPGraph graph) {
		m_graph = graph;
		data = new Vector<VectorElement>();
		nodesNumber = m_graph.getNodeCount();
		edgesNumber = m_graph.getEdgeCount();
		graphCycleInfo = GraphCycleInformation.getCycles(m_graph);
		initCycleProperties();
		initChainProperties();
		initPoint = initRedrawQueue();
		traverse();
	}

	/**
	 * Constructor Ein Molekuel als eine SLN-Seguence wird visualisiert.
	 * 
	 * @param sequence
	 */

	@SuppressWarnings("unchecked")
	public DataAnalyser(final String sequence) {
		data = new Vector();
		try {
			final SmilesParser myParser = new SmilesParser();
			m_graph = myParser.parse(
					sequence,
					new Factory(myParser.getNodeParser(), myParser
							.getEdgeParser())).toHPGraph();
		} catch (final ParseException e) {
			System.err.println("Parsing is failed, sequence is " + sequence);
			e.printStackTrace();
		}
		graphCycleInfo = GraphCycleInformation.getCycles(m_graph);
		nodesNumber = m_graph.getNodeCount();
		edgesNumber = m_graph.getEdgeCount();
		initCycleProperties();
		initChainProperties();
		initPoint = initRedrawQueue();
		traverse();
	}

	/**
	 * Die Koordinaten fuer alle Kinder des Ringknotens "nodeA", die nicht zu
	 * dem aktuellen Ring gehoeren, berechnen und speichern. Wenn ein Kind
	 * weitere Kinder hat, dann werden sie fuer die weitere Bearbetung in die
	 * Warteschlange einfuegt.
	 * 
	 * @param angle
	 * @param angularDemand
	 * @param x1
	 * @param y1
	 * @param nodeA
	 * @param localNeighbors
	 * @param directionAngle
	 * @param mainDir
	 * @param isInHead
	 * @param factor
	 * @param neighborSize
	 */
	public void drawChildren(final double angle, final double angularDemand,
			final float x1, final float y1, final int nodeA,
			final ArrayList<Integer> localNeighbors,
			final double directionAngle, final int mainDir,
			final boolean isInHead, final int factor, final int neighborSize) {
		int nodeB;
		final int degreeA = m_graph.getDegree(nodeA);
		final int neighborsNumber = localNeighbors.size();
		final double segment_angle = (PItimesTwo - angularDemand)
				/ (neighborsNumber + 1);
		double m_angle;
		float x2, y2;
		for (int i = 1; i <= neighborsNumber; i++) {
			final double tmp_angle = segment_angle * i;
			x2 = (float) (x1 + Math.cos(angle - tmp_angle));
			y2 = (float) (y1 + Math.sin(angle - tmp_angle));
			nodeB = localNeighbors.get(i - 1);
			if (m_graph.getDegree(nodeB) > 1) {
				m_angle = (angle - tmp_angle + Math.PI) % PItimesTwo;
				// Falls der Knoten zu der Hauptkette gehoert, dann bleibt die
				// Richtung unveraendert,
				// ansonsten haengt die Richtung von dem gerade ausgerechneten
				// Winkel ab.
				double newDirection = (angle - tmp_angle + factor
						* PIdividedBySix + PItimesTwo)
						% PItimesTwo;
				if ((isInHead) && (neighborsNumber < 2)
						&& (headChain.containsNode(nodeB))) {
					final double[] newDirections = {
							0,
							newDirection,
							(angle - tmp_angle + (factor) * (-1)
									* PIdividedBySix + PItimesTwo)
									% PItimesTwo };
					int index = this.getMainDirectionIndex(newDirections,
							directionAngle);
					if (index == 2) {
						if (neighborSize > 3) {
							index = 1;
						} // else factor = (-1)* factor;
					}
					newDirection = newDirections[index];
				}
				redrawQueue.add(new DrawingNode(nodeB, x2, y2, m_angle,
						newDirection, mainDir));

			} else {
				// gleichzeitig Maximum und Minimum bestimmen,
				// weil der Punkt niergendwo mehr auftauchen wuerde
				setMaxAndMin(x2, y2);
				visitedNodes[nodeB] = 1;
			}
			final int fromLabel = m_graph.getNodeLabel(nodeA).nucleareCharge();
			final int toLabel = m_graph.getNodeLabel(nodeB).nucleareCharge();

			// Die Koordinaten fuer das Zeichnen speichern
			data.add(new VectorElement(x1, y1, x2, y2, m_graph.getEdgeLabel(
					m_graph.getEdge(nodeA, nodeB)).bindings(), fromLabel,
					toLabel, degreeA, m_graph.getDegree(nodeB), 0, 0));
		}
	}

	/**
	 * Diese Hilfsfunktion gibt den Block zurueck, zu dem
	 * 
	 * @param cycleIndex
	 * @return a block
	 */
	private Block getBlock(final Integer cycleIndex) {
		Block block;
		for (int i = 0; i < blocksList.size(); i++) {
			block = blocksList.get(i);
			if (block.isInThisBlock(cycleIndex)) {
				return block;
			}
		}
		return null;
	}

	/**
	 * Alle Ringe, die ein Block bilden, zurueck geben.
	 * 
	 * @param cycle
	 * @param results
	 * @return all rings
	 */
	private ArrayList<Integer> getBlockMembers(final int cycle,
			final ArrayList<Integer> results) {
		final Cycle currentCycle = cyclesVector.get(cycle - 1);
		for (int i = 0; i < currentCycle.cycleSize; i++) {
			final ArrayList<Integer> tmp = nodeCycleMembership[currentCycle.nodesList
					.get(i).intValue()];
			final int size = tmp.size();
			if (size > 1) {
				for (int j = 0; j < size; j++) {
					final Integer tmp_int = tmp.get(j);
					if (results.indexOf(tmp_int) == -1) {
						results.add(tmp_int);
						getBlockMembers(tmp_int.intValue(), results);
					}
				}
			}
		}
		return results;
	}

	/**
	 * Diese Funktion gibt die fuer das Molekuel berechneten Koordinaten
	 * zurueck.
	 * 
	 * @return the coordinates of the molekule
	 */
	public Vector<VectorElement> getDrawingData() {
		return data;
	}

	/**
	 * Diese Hilfsfunktion fuer die Analyse des Molekuelgraphes fuegt die Knoten
	 * in eine fortlaufende Kette, solange ein Kreuzungspunkt nicht erreicht
	 * ist.
	 * 
	 * @param nodeA
	 * @param chainSection
	 * @return end
	 */
	private int getEndOfSimpleChain(final int nodeA, final Chain chainSection) {

		// Chain chainSection = new Chain(current);
		final HashSet<Integer>[] functionalGroups = chainSection.getGroups();
		int edge, nodeB;
		final int degree = m_graph.getDegree(nodeA);
		// Den aktuellen Knoten zur Kette addieren
		chainSection.addNodeToChain(nodeA);
		// Wenn der Knoten eine Kreuzung ist
		if ((nodeCycleMembership[nodeA].size() > 0) || (degree > 2)) {
			chainSection.addNodeToChain(chainSection.getBeginOfChain());
			chainSection.setEndOfChain(nodeA);
			// dann die Teilkette speichern
			chains.add(chainSection);
			// current.addChain(chainSection);
			return nodeA;
		}
		if (degree == 1) {
			edge = m_graph.getNodeEdge(nodeA, 0);
			// Wenn das Ende der aktuellen Kette erreicht ist
			if (visitedEdges[edge] == 1) {
				chainsEndPoints.add(nodeA);
				// dann den Anfang von der Kette auch in die Kette eintragen
				chainSection.addNodeToChain(chainSection.getBeginOfChain());
				// und den Endknoten speichern
				chainSection.setEndOfChain(nodeA);
				chains.add(chainSection);
				return nodeA;
			} else {
				// Die Teilkette faengt jetzt gerade an
				visitedEdges[edge] = 1;
				nodeB = m_graph.getOtherNode(edge, nodeA);
				groupsProps.findFunctionalGroupSimpleCase(nodeA, nodeB, edge,
						functionalGroups);
				return getEndOfSimpleChain(nodeB, chainSection);
			}
		} else {
			// Der Knoten ist kein Kreuzungspunkt, hat zwei Nachbarn,
			// von deren schon besucht ist
			edge = m_graph.getNodeEdge(nodeA, 0);
			if (visitedEdges[edge] == 1) {
				edge = m_graph.getNodeEdge(nodeA, 1);
			}
			visitedEdges[edge] = 1;
			nodeB = m_graph.getOtherNode(edge, nodeA);

			groupsProps.findFunctionalGroupComplexCase(nodeA, 1,
					new int[] { nodeB }, new int[] { edge },
					new int[] { m_graph.getDegree(nodeB) }, functionalGroups);
			return getEndOfSimpleChain(nodeB, chainSection);
		}

	}

	/**
	 * Diese Funktion speichert bei dem Durchlauf durch den Graph alle fuer das
	 * Zeichen relevante Informationen.
	 * 
	 * @param nodeA
	 * @param currentChain
	 * @param rootChainSize
	 * @return the first chain
	 */
	private Chain getHeadChain(int nodeA, final Chain currentChain,
			final int rootChainSize) {
		int cyclesNumber;
		int cycleIndex;
		int nodeB;
		int parentNode = -1;
		Chain sectionChain;
		Block block;
		// alle Atome bis zu der ersten Kreuzung oder bis zum Ende in die
		// Teilkette
		// "sectionChain" eintragen
		int degree = m_graph.getDegree(nodeA);
		sectionChain = new Chain(currentChain.getBeginOfChain());
		nodeA = getEndOfSimpleChain(nodeA, sectionChain);
		degree = m_graph.getDegree(nodeA);

		// wenn der Knoten "nodeA" der erste oder letzte Knoten in der Teilkette
		// "sectionChain" ist,
		if (degree == 1) {
			// dann diese Teilkette an die aktuelle Hauptkette anhaengen
			// und die aktuelle Hauptkette zurueckgeben
			currentChain.addChainToChain(sectionChain);
			if (nodeA != startNode) {
				return currentChain;
			}
		}
		currentChain.addChainToChain(sectionChain);
		final int parentSize = currentChain.getSize() + rootChainSize;
		final ArrayList<Chain> currentCrossPointData = new ArrayList<Chain>();
		cyclesNumber = nodeCycleMembership[nodeA].size();
		int edge;
		switch (cyclesNumber) {
		// Der aktuelle Knoten ist ein Kreuzungspunkt
		case 0:
			final CrossPoint crossPoint = new CrossPoint(degree);
			final int[] nodes = new int[degree];
			final int[] edges = new int[degree];
			final int[] degrees = new int[degree];
			for (int i = 0; i < degree; i++) {
				edge = m_graph.getNodeEdge(nodeA, i);
				nodeB = m_graph.getOtherNode(edge, nodeA);

				if (visitedEdges[edge] == 1) {
					parentNode = nodeB;
				} else {
					visitedEdges[edge] = 1;
				}
				nodes[i] = nodeB;
				edges[i] = edge;
				degrees[i] = m_graph.getDegree(nodeB);
			}
			groupsProps.findFunctionalGroupComplexCase(nodeA, degree, nodes,
					edges, degrees, crossPoint.getGroups());
			// Die Rekursion fuer jeden Nachbarn von dem aktuellen Knoten
			// (ausser
			// "parentNode") starten, wenn der Nachbar zur keinen gerade
			// gefundenen funktionelen Gruppen gehoert, sonst fuer den Nachbarn
			// des Nachbarns.
			// Ergebnisse in "currentCrossPointData" speichern, um danach das
			// goesste und zweitgroesste Kind zu finden:
			int childSize = 0;
			for (int i = 0; i < degree; i++) {
				nodeB = nodes[i];
				if (nodeB != parentNode) {
					final Chain nextChain = new Chain(nodeA);
					if (degrees[i] == 1) {
						chainsEndPoints.add(nodeB);
					}
					nextChain.addNodeToChain(nodeB);
					final Chain result = getHeadChain(nodeB, nextChain,
							parentSize);
					currentCrossPointData.add(result);
					childSize = result.getSize();

				} else {
					childSize = parentSize;
				}
				crossPoint.saveChild(nodeB, childSize);
			}
			// Den aktuellen Kreuzpunkt speichern
			crossPoints.put(nodeA, crossPoint);
			// Als naechstes die Wahl treffen, welches Kind zur Hauptkette
			// angeschlossen
			// wird, parallel die Vereinigung von zwei grossesten Kindern
			// als Kette speichern,
			// wenn diese groessere Prioritaet im Vergleich zur schon
			// existierende
			// hat
			setNewCurrentChain(currentCrossPointData, currentChain, crossPoint);
			break;
		// Der aktuelle Knoten ist in einem Ring
		default:
			cycleIndex = nodeCycleMembership[nodeA].get(0).intValue();
			// Ueberpruefen, ob der Knoten in einem Block ist
			final ArrayList<Integer> begin = new ArrayList<Integer>();
			begin.add(0, cycleIndex);
			final ArrayList<Integer> blockMembers = getBlockMembers(cycleIndex,
					begin);
			if (blockMembers.size() > 1) {
				block = new Block(blockCounter++, blockMembers);
				final HashMap<Integer, ArrayList<Integer>> neighborsNotInCycle = block
						.getNeighborsNotInThisBlock();
				int maxSize = parentSize;
				Integer maxChild = nodeA;
				int i = 0;
				for (final Map.Entry<Integer, ArrayList<Integer>> e : neighborsNotInCycle
						.entrySet()) {

					final Integer currentKey = e.getKey();
					final int currentNode = currentKey.intValue();
					final ArrayList<Integer> children = e.getValue();
					final int[][] cycleChildren = new int[children.size()][2];
					if (!children.isEmpty()) {
						final Chain nextChain = new Chain(currentNode);

						for (int j = 0; j < children.size(); j++) {
							nodeB = children.get(j).intValue();
							edge = m_graph.getEdge(currentNode, nodeB);
							if (visitedEdges[edge] == 1) {
								cycleChildren[j][0] = nodeB;
								cycleChildren[j][1] = parentSize;
								continue;
							}
							if (m_graph.getDegree(nodeB) == 1) {
								groupsProps.findFunctionalGroupSimpleCase(
										nodeA, nodeB, edge, block.getGroups());
							}
							visitedEdges[edge] = 1;
							final Chain result = getHeadChain(nodeB, nextChain,
									parentSize);
							final int size = result.getSize();
							cycleChildren[j][0] = nodeB;
							cycleChildren[j][1] = size;
							// Fuer die horizontaleAusrichtung groesses Kind
							// bestimmen
							if (size > maxSize) {
								maxSize = size;
								maxChild = currentKey;
							}
							currentCrossPointData.add(result);
						}
					}
					block.saveChild(currentKey, cycleChildren);
					i++;

				}
				chainsEndPoints.add(maxChild);
				setNewCurrentChain(currentCrossPointData, currentChain, block);
				blocksList.add(block);
				break;
			} else {
				final Cycle cycle = cyclesVector.get(cycleIndex - 1);
				final HashMap<Integer, ArrayList<Integer>> neighborsNotInCycle = cycle
						.getNeighborsNotInThisCycle();
				final Set<Integer> set = neighborsNotInCycle.keySet();
				if ((set.size() == 1) && (set.contains(nodeA))) {
					chainsEndPoints.add(nodeA);
				}
				final Iterator<Integer> it = set.iterator();
				for (int i = 0; it.hasNext(); i++) {
					final Integer currentKey = it.next();
					final int currentNode = currentKey.intValue();
					final ArrayList<Integer> children = neighborsNotInCycle
							.get(currentKey);
					if (!children.isEmpty()) {
						final Chain nextChain = new Chain(currentNode);
						final int[][] cycleChildren = new int[children.size()][2];
						for (int j = 0; j < children.size(); j++) {
							nodeB = children.get(j).intValue();
							edge = m_graph.getEdge(currentNode, nodeB);
							if (visitedEdges[edge] == 1) {
								cycleChildren[j][0] = nodeB;
								cycleChildren[j][1] = parentSize;
							} else {
								if (m_graph.getDegree(nodeB) == 1) {
									groupsProps.findFunctionalGroupSimpleCase(
											nodeA, nodeB, edge, cycle
													.getGroups());
								}
								visitedEdges[edge] = 1;
								final Chain result = getHeadChain(nodeB,
										nextChain, parentSize);
								cycleChildren[j][0] = nodeB;
								cycleChildren[j][1] = result.getSize();
								currentCrossPointData.add(result);
							}
						}
						cycle.saveChild(currentKey, cycleChildren);
					}
				}
				setNewCurrentChain(currentCrossPointData, currentChain, cycle);
				break;
			}
		} // end of switch
		currentChain.addChainToChain(sectionChain);
		return currentChain;
	}

	/**
	 * Diese Funktion bestimmt den Winkel, der am naechsten zu der Hauptrichtung
	 * der Kette liegt.
	 * 
	 * @param angels
	 * @param direction
	 * @return something
	 */
	private int getMainDirectionIndex(final double[] angels,
			final double direction) {
		int result = 0;
		double minDistance = Double.MAX_VALUE;
		for (int i = 1; i < angels.length; i++) {
			double tmp1 = Math.abs(direction - angels[i]);
			if ((PItimesTwo - tmp1) < tmp1) {
				tmp1 = PItimesTwo - tmp1;
			}
			if (tmp1 < minDistance) {
				minDistance = tmp1;
				result = i;
			}
		}
		return result;
	}

	/**
	 * @return the minimum x value
	 */
	public float getMinimumOfXValue() {
		return minx;
	}

	/**
	 * @return the minimum y value
	 */
	public float getMinimumOfYValue() {
		return miny;
	}

	/**
	 * Diese Hilfsfunktion bestimmt den Knoten, an dem die Analyse des
	 * Molekuelgraphen beginnt.
	 * 
	 * @return first node
	 */
	private int getStartNode() {
		int nodeA = 0;
		for (int i = 0; i < nodesNotInCycles.size(); i++) {
			nodeA = nodesNotInCycles.get(i).intValue();
			if (m_graph.getDegree(nodeA) == 1) {
				return nodeA;
			}
		}
		// Dieser Fall tritt nur dann ein, wenn ein Molekuel nur aus den
		// miteinander
		// verknuepften Ringen aufgebaut ist
		for (int i = 0; i < nodesNumber; i++) {

			final int cycleNumber = nodeCycleMembership[i].size();
			if (cycleNumber == 1) {
				return i;
			}
			if ((cycleNumber > 0)
					&& ((m_graph.getDegree(i) - cycleNumber) == 2)) {
				return i;
			}
		}
		return nodeA;
	}

	/**
	 * @return the x difference
	 */
	public float getXDifference() {
		return xDifference;
	}

	/**
	 * @return the y difference
	 */
	public float getYDifference() {
		return yDifference;
	}

	/**
	 * Diese Funktion startet die Analyse des Molekuelgraphen.
	 * 
	 */
	private void initChainProperties() {
		visitedEdges = new byte[m_graph.getEdgeCount()];
		crossPoints = new HashMap<Integer, CrossPoint>();
		chains = new ArrayList<Chain>();
		twoBiggestChildren = new Chain();
		chainsEndPoints = new ArrayList<Integer>();
		groupsProps = new FunctionalGroupsProperties<Atom, Bond>(m_graph);
		// Die Hauptkette bestimmen
		startNode = getStartNode();
		final Chain rootChain = getHeadChain(startNode, new Chain(startNode), 0);
		if (isBigger(twoBiggestChildren.getPriority(), rootChain.getPriority())) {
			headChain = rootChain;
			if (chainsEndPoints.isEmpty()) {
				chainsEndPoints.add(startNode);
			}
		} else {
			headChain = twoBiggestChildren;
		}
		// Die fuer die Kollisionenbehandlung benoetigten
		// Datenstrukturen initialisieren
		topBranchesRight = new HashMap<Integer, Integer>();
		bottomBranchesRight = new HashMap<Integer, Integer>();
		topBranchesLeft = new HashMap<Integer, Integer>();
		bottomBranchesLeft = new HashMap<Integer, Integer>();
	}

	/**
	 * Diese Funktion speichert Informationen ueber die in dem Molekuel
	 * vorhandenen Ringe
	 */
	@SuppressWarnings("unchecked")
	public void initCycleProperties() {
		ArrayList<Integer> cycleNodes = null;
		ArrayList<Integer> cycleEdges = null;
		visitedNodes = new byte[nodesNumber];

		// Die Anzahl von Ringen in graph bestimmen und alle von ihr
		// abhaengige Datenstrukturen initialisieren
		cyclesNumberInMolecule = graphCycleInfo.cycleCount();

		cyclesVector = new Vector<Cycle>(cyclesNumberInMolecule);
		visitedCycles = new Vector<Integer>(cyclesNumberInMolecule);
		nodeCycleMembership = new ArrayList[nodesNumber];
		edgeCycleMembership = new ArrayList[edgesNumber];
		// nodeCycleMembership = graphCycleInfo.getNodeRingMembership();
		// edgeCycleMembership = graphCycleInfo.getEdgeRingMembership();
		nodesNotInCycles = graphCycleInfo.getNodesNotInRings();

		for (int i = 0; i < nodesNumber; i++) {
			nodeCycleMembership[i] = new ArrayList<Integer>();
		}

		for (int i = 0; i < edgesNumber; i++) {
			edgeCycleMembership[i] = new ArrayList<Integer>();
		}

		for (int cycleNumber = 0; cycleNumber < cyclesNumberInMolecule; cycleNumber++) {
			cycleNodes = new ArrayList<Integer>();
			cycleEdges = new ArrayList<Integer>();
			for (int v = 0; v < nodesNumber; v++) {

				if (graphCycleInfo.isNodeInCycle(v, cycleNumber)) {
					nodeCycleMembership[v].add(cycleNumber + 1);
					cycleNodes.add(v);
				}
			}
			for (int e = 0; e < edgesNumber; e++) {
				if (graphCycleInfo.isEdgeInCycle(e, cycleNumber)) {
					edgeCycleMembership[e].add(cycleNumber + 1);
					cycleEdges.add(e);
				}
			}
			cyclesVector.addElement(new Cycle(cycleNumber + 1, cycleNodes,
					cycleEdges));
		}

		blocksList = new ArrayList<Block>();
	}

	/**
	 * Mit dieser Funktion erfolgt mittels Breitensuche die Generierung der
	 * 2D-Koordinaten fuer den Molekuelgraphen.
	 * 
	 * @return something
	 */
	private int initRedrawQueue() {
		int result = 0;
		int degree;
		int label;
		redrawQueue = new ArrayList<DrawingNode>();
		Integer point = null;
		for (int i = 0; i < chainsEndPoints.size(); i++) {
			point = chainsEndPoints.get(i);
			if (headChain.containsNode(point)) {
				result = point.intValue();

				if (nodeCycleMembership[result].size() > 0) {
					redrawQueue
							.add(new DrawingNode(result, 0, 0, 0, Math.PI, 1));
					degree = m_graph.getDegree(result);
					label = m_graph.getNodeLabel(result).nucleareCharge();
					data.add(new VectorElement(0.0f, 0.0f, 0.0f, 0.0f, 0,
							label, label, 0, degree, 0, 0));
					return result;
				}
			}
		}
		degree = m_graph.getDegree(result);
		label = m_graph.getNodeLabel(result).nucleareCharge();
		switch (degree) {
		case 1:
			// Als der Anfang ist ein Endknoten gewaehlt
			// Der Winkel betraegt 150 Grad
			redrawQueue.add(new DrawingNode(result, 0, 0,
					15.0 / 18.0 * Math.PI, 0, 1));
			break;
		case 2:
			// Wenn ein Knoten aus der einfachen Kette als der erste Knoten
			// gewaehlt
			// ist, dann den Winkel auf 270 setzen//////
			redrawQueue.add(new DrawingNode(result, 0, 0, 3.0 / 2.0 * Math.PI,
					0, 1));
			break;
		default:
			// Wenn ein Kreuzungspunkt als der erste Knoten gewaehlt ist,
			// dann den Winkel auf 0 setzen,
			redrawQueue.add(new DrawingNode(result, 0, 0, 0, 0, 1));
		}
		data.add(new VectorElement(0.0f, 0.0f, 0.0f, 0.0f, 0, label, label, 0,
				degree, 0, 0));
		return result;
	}

	/**
	 * Diese Hilfsfunktion berechnet Koordinaten fuer den naechsten Knoten und
	 * fuegt eine neue Einheit in die fuer das Zeichen vorbereitete
	 * Datenstruktur.
	 * 
	 * @param x1
	 * @param y1
	 * @param angle
	 * @param nodeA
	 * @param nodeB
	 * @param edge
	 * @param directionAngle
	 * @param mainDir
	 * @param degreeA
	 */
	public void insertNextPoint(final float x1, final float y1,
			final double angle, final int nodeA, final int nodeB,
			final int edge, final double directionAngle, final int mainDir,
			final int degreeA) {
		final float x2 = x1 + (float) (Math.cos(angle));
		final float y2 = y1 + (float) (Math.sin(angle));
		final int fromLabel = m_graph.getNodeLabel(nodeA).nucleareCharge();
		final int toLabel = m_graph.getNodeLabel(nodeB).nucleareCharge();

		redrawQueue.add(new DrawingNode(nodeB, x2, y2, (angle + Math.PI)
				% (PItimesTwo), directionAngle, mainDir));
		data.add(new VectorElement(x1, y1, x2, y2, m_graph.getEdgeLabel(edge)
				.bindings(), fromLabel, toLabel, degreeA, m_graph
				.getDegree(nodeB), 0, 0));
	}

	/**
	 * Diese Funktion bestimmt, welche Kette groesser ist. Gibt true zurueck,
	 * wenn die zweite Kette groesser ist.
	 * 
	 * @param first
	 * @param second
	 * @return <code>true</code>, if the first is bigger than the second
	 */
	private boolean isBigger(final int[] first, final int[] second) {
		if (first == null) {
			return true;
		}
		if (second[1] > first[1]) {
			return true;
		}
		if (second[0] > first[0]) {
			return true;
		}
		return false;
	}

	/**
	 * Diese Funktion startet die rekursive Berechnung der Koordinaten fuer
	 * einen Block.
	 * 
	 * @param block
	 * @param cycleIndex
	 * @param x0
	 * @param y0
	 * @param angle
	 * @param directionAngle
	 * @param mainDir
	 */
	public void setBlockPoints(final Block block, final Integer cycleIndex,
			final float x0, final float y0, final double angle,
			final double directionAngle, final int mainDir) {

		final int beginOfBlock = block.getBeginOfBlock();
		neighborsNotInBlock = block.getNeighborsNotInThisBlock();
		block.setNodeCoordinates(beginOfBlock, x0, y0);
		// Koordinaten von allen Knoten aus dem Block berechnen
		data.addAll(setRingOfBlock(block, cycleIndex, beginOfBlock, angle,
				new Vector<VectorElement>(), directionAngle, mainDir));

	}

	/**
	 * Koordinaten fuer einen Ring berechnen, der in keinem Block ist.
	 * 
	 * @param index
	 * @param x
	 * @param y
	 * @param angle
	 * @param directionAngle
	 * @param mainDir
	 */
	private void setCyclePoints(final int index, final float x, final float y,
			final double angle, final double directionAngle, int mainDir) {
		float x2, y2;
		float x1 = x;
		float y1 = y;
		int edge, toLabel;
		double m_angle = 0;
		int nodeA = index;
		final int indexLabel = m_graph.getNodeLabel(nodeA).nucleareCharge();
		int fromLabel = indexLabel;
		int neighborSize = 0;
		// Den zu bearbeitenden Ring finden
		final int cycleIndex = nodeCycleMembership[index].get(0).intValue();
		final Cycle curCycle = cyclesVector.get(cycleIndex - 1);
		final int m_size = curCycle.cycleSize;

		final HashMap<Integer, ArrayList<Integer>> neighborsNotInCycle = curCycle
				.getNeighborsNotInThisCycle();
		final double angleSizeInCycle = curCycle.getInteriorAngle();
		final double alpha = angleSizeInCycle / 2.0;
		ArrayList<Integer> toDraw = neighborsNotInCycle.get(nodeA);
		int divisor = 2;
		double diff = 0f;
		if (toDraw != null) {
			divisor += toDraw.size();
			if (nodeA == initPoint) {
				divisor--;
			}
		}
		diff = (PItimesTwo - angleSizeInCycle) / divisor;
		m_angle = (PItimesTwo + angle - diff) % PItimesTwo;

		// Besonderer Fall
		if (nodeA == initPoint) {
			m_angle = 11.0 / 6.0 * Math.PI - diff;
		}

		// Den Faktor bestimmen, nach dem die Ringknoten angeordnet werden
		final double[] newAngles = { 0, m_angle,
				(m_angle - angleSizeInCycle + PItimesTwo) % PItimesTwo };
		int factor = 1;
		if (this.getMainDirectionIndex(newAngles, directionAngle) == 2) {
			factor = -1;
		}
		// Ringelementen nach ihre Nachbarschaftsbeziehung geordnet anfordern
		final int[] m_list = curCycle.getOrderForThisDirection(index, factor,
				mainDir);
		// Kinder von nodeA zeichnen, die nicht aus dem aktuellen Ring sind
		if (toDraw != null) {
			if (nodeA == initPoint) {
				drawChildren((m_angle - angleSizeInCycle + PItimesTwo)
						% PItimesTwo, angleSizeInCycle, x1, y1, nodeA, toDraw,
						0, mainDir, false, factor * mainDir, 5);
				mainDir = -1;
			} else {
				drawChildren((m_angle - angleSizeInCycle + PItimesTwo)
						% PItimesTwo, angleSizeInCycle + diff, x1, y1, nodeA,
						toDraw, directionAngle, mainDir, false, factor,
						neighborSize);
			}
		}
		boolean isHeadFound = false;
		// Die Koordinaten fuer die Ringknoten ausrechnen
		for (int i = 0; i < m_size - 1; i++) {
			boolean isInHead = false;
			final int nodeB = m_list[i];
			edge = m_graph.getEdge(nodeA, nodeB);
			if (i > 0) {
				m_angle = m_angle + angleSizeInCycle;
			}
			x2 = (float) (x1 + Math.cos(m_angle));
			y2 = (float) (y1 + Math.sin(m_angle));
			m_angle = (m_angle + Math.PI) % (PItimesTwo);
			// gleichzeitig Maximum und Minimum bestimmen, um das Bild an die
			// Fenstergroesse anzupassen.
			setMaxAndMin(x2, y2);
			// Ueberpruefen, ob der Knoten noch andere Nachbarn hat,
			// die nicht zu dem aktuellen Ring gehoeren,und ihre
			// Koordinaten ausechnen
			toDraw = neighborsNotInCycle.get(nodeB);
			if (toDraw != null) {
				neighborSize = 0;
				if ((!isHeadFound)
						&& (((i == 1) && (factor == 1)) || ((i == (m_size - 2)) && (factor == -1)))
						&& (headChain.containsNode(nodeB))) {
					if (factor == 1) {
						neighborSize = curCycle
								.getSizeOfMaxChild(m_list[(i + 1 + m_size)
										% m_size]);
					} else {
						neighborSize = curCycle
								.getSizeOfMaxChild(m_list[(i - 1 + m_size)
										% m_size]);
					}
					isInHead = true;
					isHeadFound = true;
				}
				// Die Koordinaten fuer die Kinder ausrechnen
				drawChildren(m_angle, angleSizeInCycle, x2, y2, nodeB, toDraw,
						directionAngle, mainDir, isInHead, factor, neighborSize);
			}
			visitedNodes[nodeB] = 1;
			toLabel = m_graph.getNodeLabel(nodeB).nucleareCharge();

			// Die ausgerechneten Koordinaten des Ringknotens speichern
			data.add(new VectorElement(x1, y1, x2, y2, m_graph.getEdgeLabel(
					edge).bindings(), fromLabel, toLabel, m_graph
					.getDegree(nodeA), m_graph.getDegree(nodeB), alpha, alpha));
			nodeA = nodeB;
			fromLabel = toLabel;
			x1 = x2;
			y1 = y2;
		}
		edge = m_graph.getEdge(nodeA, index);
		data.add(new VectorElement(x1, y1, x, y, m_graph.getEdgeLabel(edge)
				.bindings(), fromLabel, indexLabel, m_graph.getDegree(nodeA),
				m_graph.getDegree(index), alpha, alpha));
		visitedNodes[index] = 1;
		visitedCycles.add(cycleIndex);
	}

	/**
	 * Die Grenzwerten fuer die paintComponent() aus Demo.java neu setzen,wenn
	 * sie sich bei der Berechnung von Koordinaten aendern.
	 * 
	 * @param x
	 * @param y
	 */
	private void setMaxAndMin(final float x, final float y) {
		if (x < minx) {
			minx = x;
		}
		if (x > maxx) {
			maxx = x;
		}
		if (y < miny) {
			miny = y;
		}
		if (y > maxy) {
			maxy = y;
		}
	}

	/**
	 * Diese Funktion setzt zwei Ketten zusammen.
	 * 
	 * @param data
	 * @param currentChain
	 * @param crossUnit
	 */
	private void setNewCurrentChain(final ArrayList<Chain> data,
			final Chain currentChain, final DrawingUnit crossUnit) {
		if (data.isEmpty()) {
			if (crossUnit instanceof Cycle) {
				currentChain.addGroups(crossUnit.getGroups());
			}
			return;
		}
		int[] firstMax = data.get(0).getPriority();
		int[] secondMax = null;
		int[] currentPriority;
		int firstIndex = 0;
		int secondIndex = 0;
		final Chain tmp2 = new Chain();
		for (int i = 1; i < data.size(); i++) {
			final Chain myChain = data.get(i);
			if (myChain != null) {
				currentPriority = myChain.getPriority();
				if (isBigger(firstMax, currentPriority)) {
					secondMax = firstMax;
					secondIndex = firstIndex;
					firstMax = currentPriority;
					firstIndex = i;
				} else {
					if (isBigger(secondMax, currentPriority)) {
						secondMax = currentPriority;
						secondIndex = i;
					}
				}
			}
		}
		tmp2.addGroups(crossUnit.getGroups());
		tmp2.addChainToChain(data.get(firstIndex));
		tmp2.addChainToChain(data.get(secondIndex));
		if (isBigger(twoBiggestChildren.getPriority(), tmp2.getPriority())) {
			twoBiggestChildren = tmp2;
		}
		currentChain.addGroups(crossUnit.getGroups());
		currentChain.addChainToChain(data.get(firstIndex));
	}

	/**
	 * Diese Funktion berechnet Koordinaten in einer fortlaufenden Kette.
	 * 
	 * @param x1
	 * @param y1
	 * @param nodeA
	 * @param angle
	 * @param directionAngle
	 * @param mainDir
	 */
	private void setNextPoint(final float x1, final float y1, final int nodeA,
			final double angle, double directionAngle, final int mainDir) {
		double m_angle;
		int nodeB, edge, mainDirectionIndex;
		final int degreeA = m_graph.getDegree(nodeA);
		for (int d = 0; d < degreeA; d++) {
			edge = m_graph.getNodeEdge(nodeA, d);
			nodeB = m_graph.getOtherNode(edge, nodeA);
			// Der Knoten B war noch nicht besucht
			if (visitedNodes[nodeB] == 0) {
				final double[] newAngles = new double[3];
				if ((m_graph.getNodeLabel(nodeA).nucleareCharge() != 6)
						&& (m_graph.getNodeLabel(nodeB).nucleareCharge() != 6)
						&& ((m_graph.getEdgeLabel(edge).bindings() == 2) || (m_graph
								.getEdgeLabel(edge).bindings() == 3))) {
					newAngles[1] = angle;
					newAngles[2] = (angle + Math.PI) % PItimesTwo;
				} else {
					newAngles[1] = (angle - twoThirdsPI + PItimesTwo)
							% PItimesTwo;
					newAngles[2] = (angle + twoThirdsPI) % PItimesTwo;

				}
				mainDirectionIndex = getMainDirectionIndex(newAngles,
						directionAngle);
				m_angle = newAngles[mainDirectionIndex];
				insertNextPoint(x1, y1, m_angle, nodeA, nodeB, edge,
						directionAngle, mainDir, degreeA);

				// Die entgegengesetzte Richtung benutzen,
				// wenn noch nicht beide Kinder besucht sind
				directionAngle = (directionAngle + Math.PI) % PItimesTwo;
			}
		}
		visitedNodes[nodeA] = 1;
	}

	/**
	 * Diese Funktion vergibt Koordinaten an einer Kreuzung.
	 * 
	 * @param x1
	 * @param y1
	 * @param nodeA
	 * @param angle
	 * @param directionAngle
	 * @param mainDir
	 * @param crossPoint
	 * @param isInHead
	 */
	private void setNextPoints(final float x1, final float y1, final int nodeA,
			final double angle, final double directionAngle, final int mainDir,
			final CrossPoint crossPoint, final boolean isInHead) {

		double m_angle, m_direction;
		int[] nodes;
		int nodeB, edge, mainDirectionIndex;
		final int degreeA = m_graph.getDegree(nodeA);
		final double segmentSize = PItimesTwo / degreeA;
		final double[] newAngles = new double[degreeA];
		for (int i = 1; i < degreeA; i++) {
			newAngles[i] = (angle + segmentSize * i) % PItimesTwo;
		}
		mainDirectionIndex = getMainDirectionIndex(newAngles, directionAngle);
		if (isInHead) {
			// Der Knoten befindet sich in der Hauptkette
			// Ein Ausnahmefall fuer die Suche der Hauptrichtung
			if (degreeA % 2 == 0) {
				mainDirectionIndex = degreeA / 2;
			}

			nodes = crossPoint.getNodesListForHeadChain(mainDirectionIndex);
		} else {
			nodes = crossPoint.getNodesList(mainDirectionIndex);
		}
		int count = 1;
		for (int d = 0; d < nodes.length; d++) {
			nodeB = nodes[d];
			if (nodeB == -1) {
				continue;
			}
			if (d == mainDirectionIndex) {
				m_direction = directionAngle;
				m_angle = newAngles[mainDirectionIndex];
			} else {
				if (count == mainDirectionIndex) {
					count = (count + 1) % degreeA;
				} else {
					count = count % degreeA;
				}
				if (count == 0) {
					count++;
				}
				m_angle = newAngles[count];

				// Die Richtung bestimmen, in welche sich die Kette entwickeln
				// wird
				if (degreeA > 3) {
					if (angle < Math.PI) {
						// plus 30 Grad
						m_direction = (m_angle + (mainDir) * PIdividedBySix)
								% PItimesTwo;
						// minus 30 Grad
					} else {
						// m_direction = (TwoPIminus30 + m_angle) % PItimesTwo;
						m_direction = (m_angle - (mainDir) * PIdividedBySix + PItimesTwo)
								% PItimesTwo;
					}
				} else {
					if (m_angle < Math.PI) {
						if ((isInHead) && (!isStillSpaceLeft(true, mainDir))) {
							// m_direction = (TwoPIminus30 + m_angle) %
							// PItimesTwo;
							m_direction = (m_angle - (mainDir) * PIdividedBySix + PItimesTwo)
									% PItimesTwo;
						} else {
							m_direction = (m_angle + (mainDir) * PIdividedBySix)
									% PItimesTwo;
						}
					} else {
						if ((isInHead) && (!isStillSpaceLeft(false, mainDir))) {
							m_direction = (m_angle + (mainDir) * PIdividedBySix)
									% PItimesTwo;
						} else {
							m_direction = (m_angle - (mainDir) * PIdividedBySix + PItimesTwo)
									% PItimesTwo;
						}
					}
				}
				// Die Werte fuer die Kollisionsbehandlung speichern
				if (isInHead) {
					final Integer branchSize = crossPoint
							.getSizeOfBranch(nodeB);
					// die Laenge der ausgehenden Zweige speichern, die sich am
					// naechsten zu Hauptkette befinden.
					if (count == (mainDirectionIndex - 1)) {
						if (mainDir == 1) {
							bottomBranchesRight.put(++rightCounter, branchSize);
						} else {
							topBranchesLeft.put(++leftCounter, branchSize);
						}
					}
					if (count == (mainDirectionIndex + 1)) {
						if (mainDir == 1) {
							topBranchesRight.put(++rightCounter, branchSize);
						} else {
							bottomBranchesLeft.put(++leftCounter, branchSize);
						}
					}
				}

				count++;
			}
			edge = m_graph.getEdge(nodeA, nodeB);
			insertNextPoint(x1, y1, m_angle, nodeA, nodeB, edge, m_direction,
					mainDir, degreeA);

		}
		visitedNodes[nodeA] = 1;
	}

	/**
	 * Diese Funktion berechnet rekursiv die Koordinaten fuer die Knoten des
	 * Blocks.
	 * 
	 * @param block
	 * @param cycleIndexInt
	 * @param index
	 * @param angle
	 * @param result
	 * @param directionAngle
	 * @param mainDir
	 * @return teh coordinates of the Block
	 */
	public Vector<VectorElement> setRingOfBlock(final Block block,
			final Integer cycleIndexInt, final int index, final double angle,
			final Vector<VectorElement> result, final double directionAngle,
			int mainDir) {
		int nodeB;
		int fromLabel, toLabel;
		float x1, y1;
		float x2 = 0, y2 = 0;
		double m_angle = angle;
		boolean isAromaticCycle;
		int[] m_list;
		// Den aktuellen Ring des Blocks aus dem Vector,
		// wo alle Ringe drin sind, holen
		final Cycle curCycle = cyclesVector.get(cycleIndexInt.intValue() - 1);
		isAromaticCycle = curCycle.isAromatic();
		final int m_size = curCycle.cycleSize;
		// Die Datenstruktur, wo die besuchten Ringknoten gespeichert werden,
		// initialisieren:
		final Vector<Integer> visitedThisRing = new Vector<Integer>(m_size);
		// Den aktuellen Ring als besucht markieren:
		visitedCycles.add(cycleIndexInt);

		// Die Anfangswerte fuer die for-Schleife initialisieren
		int nodeA = index;
		float[] xy = block.getXY(index);
		final float x1_begin = xy[0];
		final float y1_begin = xy[1];
		x1 = xy[0];
		y1 = xy[1];
		// fromLabel = m_graph.getNodeLabel(index);
		fromLabel = m_graph.getNodeLabel(index).nucleareCharge();
		// rek_helper enthaelt alle Ringe, in welchen der Knoten nodeA vorkommt
		ArrayList<Integer> rek_helper = nodeCycleMembership[nodeA];

		double angleSizeInCycle = 0;
		// Den Platz berechnen, der fuer Ring(e) benoetigt wird

		if ((rek_helper.size() > 1)) {
			angleSizeInCycle = block
					.getInteriorAngleForBeginOfBlock(rek_helper);
		} else {
			// Den Innenwinkel des Ringes berechnen
			angleSizeInCycle = block.getInteriorAngle(cycleIndexInt);
		}
		int divisor = 2;
		double diff = 0;

		// Bei demm ersten Knoten aus dem Ring ueberpruefen,
		// ob der noch nicht besuchte Knoten hat, die zu keinem
		// Ring aus dem Block gehoeren. Dabei den Winkel fuer die
		// spaetere Koordinatenberechnung bestimmen.
		ArrayList<Integer> toDraw = neighborsNotInBlock.get(nodeA);
		boolean isHead = false;
		double m_direction = directionAngle;
		double arg0 = 0;
		int factor = -1;
		int childrenNumber = 0;
		final int neighborSize = 0;
		if (toDraw != null) {
			childrenNumber = toDraw.size();
			divisor += childrenNumber;
			if (nodeA == block.getBeginOfBlock()) {
				if (nodeA == initPoint) {
					m_angle = 11.0 / 6.0 * Math.PI
							- (PItimesTwo - angleSizeInCycle) / (divisor - 1);
					diff = 0;
					arg0 = (m_angle - angleSizeInCycle + PItimesTwo)
							% PItimesTwo;
					m_direction = 0f;
					isHead = true;
				} else {
					diff = (PItimesTwo - angleSizeInCycle) / divisor;
					m_angle = (PItimesTwo + angle - diff) % PItimesTwo;
					arg0 = (m_angle - angleSizeInCycle + PItimesTwo)
							% PItimesTwo;
				}
			} else {
				drawChildren((angle + angleSizeInCycle) % PItimesTwo,
						angleSizeInCycle, x1, y1, nodeA, toDraw,
						directionAngle, mainDir, false, -1, neighborSize);
				neighborsNotInBlock.remove(nodeA);
			}

		} else {
			diff = (PItimesTwo - angleSizeInCycle) / divisor;
			m_angle = (PItimesTwo + angle - diff) % PItimesTwo;
		}
		// Den Faktor bestimmen, der die Anordnung der Ringknoten beeinfluesst
		if (index == block.getBeginOfBlock()) {
			final double[] newAngles = { 0, m_angle,
					(m_angle - angleSizeInCycle + PItimesTwo) % PItimesTwo };
			factor = 1;
			if (this.getMainDirectionIndex(newAngles, directionAngle) == 2) {
				factor = -1;
			}
			if (toDraw != null) {
				drawChildren(arg0, angleSizeInCycle + diff, x1, y1, nodeA,
						toDraw, m_direction, mainDir, isHead, factor,
						neighborSize);
				neighborsNotInBlock.remove(nodeA);
				if (nodeA == initPoint) {
					mainDir = -1;
				}
			}
			m_list = curCycle.getOrderForThisDirection(index, factor, mainDir);
		} else {
			// Ringknoten nach ihre Nachbarschaftsbeziehung geordnet anfordern
			m_list = curCycle.getOrderedNodes(index, false);
		}
		// factor *= mainDir;
		// Noch einen Ring finden, falls vorhanden, zu dem der aktuelle Knoten
		// gehoert.
		final HashSet<Integer> helper = new HashSet<Integer>();
		for (int k = 0; k < nodeCycleMembership[nodeA].size(); k++) {
			final Integer cur = nodeCycleMembership[nodeA].get(k);
			if (!visitedCycles.contains(cur)) {
				helper.add(cur);
			}
		}
		// Innenwinkel des Ringes weiter benutzen
		angleSizeInCycle = block.getInteriorAngle(cycleIndexInt);
		final double alpha = angleSizeInCycle / 2.0;
		visitedThisRing.add(nodeA);
		for (int i = 0; i < m_size; i++) {
			nodeB = m_list[i];
			final int edge = m_graph.getEdge(nodeA, nodeB);
			final int edgeLabel = m_graph.getEdgeLabel(edge).bindings();
			final boolean isInHead = headChain.containsNode(nodeB);
			if (i == 0) {
				if ((visitedNodes[nodeB] == 1)
						&& (nodeB != block.getBeginOfBlock())) {
					m_angle = angle;
				}
			} else {
				m_angle = m_angle + angleSizeInCycle;
			}

			// Wenn die Koordinaten fuer den aktuellen Knoten schon in
			// einem vorherigen Schritt berechnet sind, dann sie benutzen,
			// sonst neu berechnen
			if (i == (m_list.length - 1)) {
				x2 = x1_begin;
				y2 = y1_begin;
			} else {
				xy = block.getXY(nodeB);
				if (xy != null) {
					// Fuer diesen Knoten sind die Koordinaten schon berechnet
					x2 = xy[0];
					y2 = xy[1];
				} else {
					x2 = (float) (x1 + Math.cos(m_angle));
					y2 = (float) (y1 + Math.sin(m_angle));
					// Die neu berechneten Koordinaten des Ringknotens
					// speichern.
					block.setNodeCoordinates(nodeB, x2, y2);
				}
			}
			// Den Winkel fuer den naechsten Schritt umrechnen
			m_angle = (m_angle + Math.PI) % (PItimesTwo);

			// Die Liste mit den Ringen, zu welchen der aktueller Knoten gehoert
			// untersuchen, um rauszufinden, ob der naechster Rekursionsschritt
			// noetig ist
			final ArrayList<Integer> cycles = nodeCycleMembership[nodeB];

			Integer tmp = cycles.get(0);
			int j = 1;
			while ((j < cycles.size())
					&& ((rek_helper.indexOf(tmp) > -1) && (visitedCycles
							.indexOf(tmp) > -1))) {

				tmp = cycles.get(j);
				j++;
			}
			// Die berechneten Koordinaten fuer das Zeichnen uebergeben
			// toLabel = m_graph.getNodeLabel(nodeB);
			toLabel = m_graph.getNodeLabel(nodeB).nucleareCharge();
			if (((nodeB == index) || (visitedThisRing.indexOf(nodeB) == -1))
					&& (visitedNodes[nodeB] == 0)) {
				visitedThisRing.add(nodeB);
				int tmpEdgeLabel = edgeLabel;
				// Wenn die Bindung aromatisch ist, dann nur nicht gestrichelte
				// Linie zeichnen
				if (edgeLabel == 4) {
					tmpEdgeLabel = 1;
				}
				data.add(new VectorElement(x1, y1, x2, y2, tmpEdgeLabel,
						fromLabel, toLabel, m_graph.getDegree(nodeA), m_graph
								.getDegree(nodeB), alpha, alpha));
			}
			// rekursiver Aufruf
			if ((rek_helper.indexOf(tmp) > -1)
					&& (visitedCycles.indexOf(tmp) == -1)) {
				setRingOfBlock(block, tmp, nodeB, m_angle, result,
						directionAngle, mainDir);

			}
			// Nach dem ersten rekursiven Aufruf ueberpruefen, ob es sich um den
			// Spiro-Fall handelt (die Ringe sind miteinander nur ueber ein
			// Knoten
			// verknuepft). Dieser Fall kommt eher selten vor.
			Integer spiroCycleIndex = null;
			double takenAngle = 0;
			for (int k = 0; k < cycles.size(); k++) {
				tmp = cycles.get(k);
				final Cycle cycle = (cyclesVector.get(tmp.intValue() - 1));
				takenAngle += cycle.getInteriorAngle();
				if ((visitedCycles.indexOf(tmp) == -1)
						&& (!cycle.isEdgeInCycle(edge))
						&& (!cycle.isNodeInCycle(m_list[(i + 1) % m_size]))) {
					spiroCycleIndex = tmp;
				}
			}
			final double freeAngle = (PItimesTwo - takenAngle) / 2.0;

			// Die neuen Koordinaten fuer die Skalierung des Fensters speichern
			setMaxAndMin(x2, y2);
			// Noch den Rest von der Nachbarschaft holen
			toDraw = neighborsNotInBlock.get(nodeB);

			// Wenn der Knoten aus dem Ring noch Nachbarn hat,
			// die in keinem Ring sind
			if ((toDraw != null) && ((nodeB != index))) {
				// dann ihre Coordinaten ausrechnen
				if (spiroCycleIndex != null) {
					drawChildren(m_angle, PItimesTwo - freeAngle, x2, y2,
							nodeB, toDraw, directionAngle, mainDir, false, 1,
							neighborSize);
				} else {
					if (isInHead) {
						curCycle.saveBranchSize(m_list, i, mainDir);
					}
					drawChildren(m_angle, block
							.getInteriorAngleForBeginOfBlock(cycles), x2, y2,
							nodeB, toDraw, directionAngle, mainDir, isInHead,
							-1, neighborSize);
				}
			}
			neighborsNotInBlock.remove(nodeB);
			if (spiroCycleIndex != null) {
				// Wenn ein Ring mit der Spiro-Verbindung gefunden ist, dann
				// neue Rekursion starten
				// neighborsNotInBlock.remove(new Integer(nodeB));
				setRingOfBlock(block, spiroCycleIndex, nodeB, m_angle, result,
						directionAngle, mainDir);
			}
			visitedNodes[nodeB] = 1;
			rek_helper = cycles;
			nodeA = nodeB;
			fromLabel = toLabel;
			x1 = x2;
			y1 = y2;
		}
		// Die gestrichelten Linien innerhalb des Ringes zeichnen, falls der
		// Ring aromatisch ist
		if (isAromaticCycle) {
			data.addAll(block.setAromaticBonds(m_list));
		}
		return result;
	}

	/**
	 * Diese Funktion generiert die 2D-Koordinaten fuer den Molekuelgraphen.
	 */
	public void traverse() {
		int index;
		DrawingNode current;
		float x1, y1;
		int mainDirection;

		while (!redrawQueue.isEmpty()) {
			current = redrawQueue.get(0);
			index = current.value;
			x1 = current.x;
			y1 = current.y;
			mainDirection = current.mainDirection;
			// gleichzeitig Maximum und Minimum bestimmen
			setMaxAndMin(x1, y1);
			final int cyclesNumber = nodeCycleMembership[index].size();
			if (cyclesNumber == 0) {
				// Der Knoten ist in keinem Ring
				// Koordinaten fuer eine einfache Kette ausrechnen:
				boolean isInHead = false;
				final Integer currentNode = index;
				final CrossPoint point = crossPoints.get(currentNode);

				if (headChain.containsNode(currentNode)) {
					isInHead = true;
				}
				if (point == null) {
					if (isInHead) {
						if (mainDirection == 1) {
							rightCounter++;
						} else {
							leftCounter++;
						}
					}
					setNextPoint(x1, y1, index, current.angle,
							current.directionAngle, current.mainDirection);
				} else {
					setNextPoints(x1, y1, index, current.angle,
							current.directionAngle, current.mainDirection,
							point, isInHead);
				}
			} else {
				// Der Knoten befindet sich in einem oder gleichzeitig in
				// mehreren Ringen (Block)
				if (cyclesNumber >= 1) {
					Integer cycleIndex = nodeCycleMembership[index].get(0);

					// wenn der erste Ring noch nicht bearbeitet war
					if (visitedCycles.indexOf(cycleIndex) == -1) {
						final Block block = getBlock(cycleIndex);
						if (block == null) {
							// und zu keinem Block gehoert,
							// dann Koordinaten berechnen

							setCyclePoints(index, x1, y1, current.angle,
									current.directionAngle,
									current.mainDirection);
						} else {
							// Der Ring ist in einem Block
							// Koordinaten fuer alle Ringknoten aus dem Block
							// berechnen
							block.setBeginOfBlock(index);
							if (cyclesNumber > 1) {
								cycleIndex = block.getBeginCycle();
							}
							if (cycleIndex == null) {
								System.out
										.println("ES LIEGT EINE 3D-STRUKTUR VOR. DIE KANN NOCH NICHT RICHTIG DARGESTELLT WERDEN");
								cycleIndex = nodeCycleMembership[index].get(0);
							}
							setBlockPoints(block, cycleIndex, x1, y1,
									current.angle, current.directionAngle,
									current.mainDirection);
						}
					}
				}
			}
			redrawQueue.remove(0);
		}
		// Die Hilfsgroessen fuer die paintComponent() berechnen
		xDifference = maxx - minx;
		yDifference = maxy - miny;
	}

}