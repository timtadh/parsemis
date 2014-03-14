/**
 * Created on Jun 16, 2005
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

import java.util.HashSet;

import de.parsemis.chemical.Atom;
import de.parsemis.chemical.Bond;
import de.parsemis.graph.HPGraph;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class FunctionalGroupsProperties<NodeType, EdgeType> {
	private static void addHashSets(final HashSet<Integer>[] headSets,
			final HashSet<Integer>[] newData, final int[] groupsNumber) {
		for (int i = 0; i < headSets.length; i++) {
			headSets[i].addAll(newData[i]);
			groupsNumber[i] = headSets[i].size();
		}
	}

	HPGraph<NodeType, EdgeType> m_graph;

	/**
	 * a new FunctionalGroupsProperties
	 * 
	 * @param graph
	 */
	public FunctionalGroupsProperties(final HPGraph<NodeType, EdgeType> graph) {
		this.m_graph = graph;
	}

	/**
	 * finds the functional group complex case
	 * 
	 * @param nodeA
	 * @param degree
	 * @param nodes
	 * @param edges
	 * @param degrees
	 * @param groups
	 */
	public void findFunctionalGroupComplexCase(final int nodeA,
			final int degree, final int[] nodes, final int[] edges,
			final int[] degrees, final HashSet<Integer>[] groups) {
		final int nodeALabel = ((Atom) m_graph.getNodeLabel(nodeA))
				.nucleareCharge();
		final int[] groupsNumber = new int[24];
		final Integer parentNode = nodes[nodes.length - 1];
		boolean isParentRemove = false;
		boolean currentIsPartOfGroup = false;
		final Integer currentNode = nodeA;
		@SuppressWarnings("unchecked")
		final HashSet<Integer>[] functionalGroups = new HashSet[24];
		for (int j = 0; j < 24; j++) {
			functionalGroups[j] = new HashSet<Integer>();
		}
		for (int i = 0; i < degree; i++) {
			@SuppressWarnings("unchecked")
			final HashSet<Integer>[] nodeGroups = new HashSet[24];
			for (int j = 0; j < 24; j++) {
				nodeGroups[j] = new HashSet<Integer>();
			}
			findFunctionalGroupSimpleCase(nodeA, nodes[i], edges[i], nodeGroups);
			addHashSets(functionalGroups, nodeGroups, groupsNumber);
		}

		switch (nodeALabel) {
		// Der Knoten A ist ein C-Atom
		case 6:
			if (groupsNumber[7] > 0) {
				currentIsPartOfGroup = true;
				groupsNumber[7] -= 1;
				if (!isParentRemove) {
					isParentRemove = removeNodeFromGroupsArray(parentNode,
							functionalGroups[7], groups);
				}
				/*
				 * O Haloformylgruppe // - C \ X
				 */
				if (groupsNumber[4] > 0) {
					groupsNumber[4] -= 1;
					if (!isParentRemove) {
						isParentRemove = removeNodeFromGroupsArray(parentNode,
								functionalGroups[4], groups);
					}
					groups[14].add(currentNode);
				} else {
					/*
					 * O Carboxygruppe // - C \\ OH
					 */
					if (functionalGroups[5].size() > 0) {
						groupsNumber[5] -= 1;
						if (!isParentRemove) {
							isParentRemove = removeNodeFromGroupsArray(
									parentNode, functionalGroups[5], groups);
						}
						groups[13].add(currentNode);
					} else {
						/*
						 * O R-...oxycarbonylgruppe // - C \ OR
						 */
						if (functionalGroups[6].size() > 0) {
							groupsNumber[6] -= 1;
							if (!isParentRemove) {
								isParentRemove = removeNodeFromGroupsArray(
										parentNode, functionalGroups[6], groups);
							}
							groups[15].add(currentNode);
						} else {
							/*
							 * O Carbomoylgruppe // - C \ NH2
							 */
							if (functionalGroups[8].size() > 0) {
								groupsNumber[8] -= 1;
								if (!isParentRemove) {
									isParentRemove = removeNodeFromGroupsArray(
											parentNode, functionalGroups[8],
											groups);
								}
								groups[16].add(currentNode);
							} else {
								/*
								 * O Formylgruppe // - C \ H
								 */
								if (degree < 3) {
									groups[17].add(currentNode);
									currentIsPartOfGroup = true;
								} else {
									/*
									 * \ Oxogruppe C = O /
									 */

									currentIsPartOfGroup = true;
									groups[23].add(currentNode);
								}
							}
						}
					}
				}
			} else {
				if (groupsNumber[10] > 0) {
					groupsNumber[10] -= 1;
					currentIsPartOfGroup = true;
					groups[10].add(currentNode);
				} else {

					// Der Knoten gehoert zu keiner functionalen Gruppen
					currentIsPartOfGroup = false;
				}
				if (groupsNumber[5] > 0) {
					currentIsPartOfGroup = false;
				}
			}
			// den Status von parentAtom bestimmen
			if (!currentIsPartOfGroup) {
				groups[0].add(nodeA);
			}
			break;
		case 16: // Der Knoten ist ein S-Atom

			if (functionalGroups[7].size() == 2) {
				groupsNumber[7] -= 2;

				if (!isParentRemove) {
					isParentRemove = removeNodeFromGroupsArray(parentNode,
							functionalGroups[7], groups);
				}
				/*
				 * O Sulfogruppe || - S - OH || O
				 */
				if (functionalGroups[5].size() > 0) {
					groupsNumber[5] -= 1;
					if (!isParentRemove) {
						isParentRemove = removeNodeFromGroupsArray(parentNode,
								functionalGroups[5], groups);
					}
					groups[18].add(currentNode);
				} else {
					/*
					 * O Alkylsulfogruppe || - S - O - R || O
					 */

					if (functionalGroups[6].size() > 0) {
						groupsNumber[6] -= 1;
						if (!isParentRemove) {
							isParentRemove = removeNodeFromGroupsArray(
									parentNode, functionalGroups[6], groups);
						}
						groups[19].add(currentNode);

					} else {
						/*
						 * O Sulfonylchloridgruppe || - S - Cl || O
						 */
						if (functionalGroups[4].size() > 0) {
							groupsNumber[4] -= 1;
							if (!isParentRemove) {
								isParentRemove = removeNodeFromGroupsArray(
										parentNode, functionalGroups[4], groups);
							}
							groups[20].add(currentNode);
						} else {
							/*
							 * O Sulfonylgruppe || - S - || O
							 */
							groups[21].add(currentNode);
						}
					}
				}
				currentIsPartOfGroup = true;
			}
			if (!currentIsPartOfGroup) {
				groups[1].add(nodeA);
			}
			break;
		}
		// Den Rest von funktionellen Gruppen speichern
		int begin;
		if (degree > 2) {
			begin = 4;
		} else {
			begin = 0;
		}
		for (int i = begin; i < 13; i++) {
			if (groupsNumber[i] > 0) {
				groups[i].addAll(functionalGroups[i]);
			}
		}
	}

	/**
	 * finds the functional group simple case
	 * 
	 * @param nodeA
	 * @param nodeB
	 * @param edge
	 * @param groups
	 */
	public void findFunctionalGroupSimpleCase(final int nodeA, final int nodeB,
			final int edge, final HashSet<Integer>[] groups) {
		final Integer currentNeighbor = nodeB;
		if (DataAnalyser.nodeCycleMembership[nodeB].size() > 0) {
			groups[22].add(currentNeighbor);
			return;
		}
		final int degree = m_graph.getDegree(nodeB);
		final int edgeLabel = ((Bond) m_graph.getEdgeLabel(edge)).bindings();
		final int nodeALabel = ((Atom) m_graph.getNodeLabel(nodeA))
				.nucleareCharge();
		final int nodeBLabel = ((Atom) m_graph.getNodeLabel(nodeB))
				.nucleareCharge();

		switch (nodeBLabel) {
		// Der Nachbar ist ein C-Atom
		case 6: // inc. die Anzahl von C-Atomen
			groups[0].add(currentNeighbor);

			// einfache bindung
			if (edgeLabel == 1) {
				return;
			}
			// doppelbindung
			if (edgeLabel == 2) {
				groups[2].add(currentNeighbor);
			}
			// dreifachbindung
			if (edgeLabel == 3) {
				groups[3].add(currentNeighbor);
			}
			break;
		// Der Nachbar ist ein N-Atom
		case 7:
			if (edgeLabel == 1) {
				// Amino-gruppe -NH2 | Diazo -N2 | Azido -N3 | Nitroso -NO |
				// Nitro -NO2
				if (degree == 1) {
					groups[8].add(currentNeighbor);

				} else {
					// inc. die Anzahl von nicht C-Atomen
					groups[1].add(currentNeighbor);
				}
			}
			if (edgeLabel == 2) {
				// imino-gruppe =NH
				if (degree == 1) {
					groups[9].add(currentNeighbor);
				} else {
					// Anzahl nicht C-Atomen
					groups[1].add(currentNeighbor);
					// Doppelbindung
					groups[2].add(currentNeighbor);
				}
			}

			if ((edgeLabel == 3) && (nodeALabel == 6)) {
				// Cyanogruppe #N
				if (degree == 1) {
					groups[10].add(currentNeighbor);

				} else {
					// -C#N- Der Fall kommt wahrsch. nie vor
					groups[3].add(currentNeighbor);
				}
			}

			break;
		// Der Nachbar ist ein O-Atom
		case 8:
			if (edgeLabel == 1) {
				// Hydroxy -OH
				if (degree == 1) {
					groups[5].add(currentNeighbor);
				} else {
					// Alkyloxy -RO
					groups[6].add(currentNeighbor);
				}
			}
			if (edgeLabel == 2) {
				// Oxogruppe =0
				if (degree == 1) {

					groups[7].add(currentNeighbor);
				} else {
					// inc. die Anzahl von Doppelbindungen
					groups[2].add(currentNeighbor);
					// inc. die Anzahl von nicht C-Atomen
					groups[1].add(currentNeighbor);
				}
			}

			break;
		// Der Nachbar ist ein S-Atom
		case 16:/*
				 * if (nodeALabel == 6) { groups[0].add(currentNeighbor); } else
				 * groups[1].add(currentNeighbor);
				 */
			if (edgeLabel == 1) {
				// Thiolgruppe -SH
				if (degree == 1) {
					groups[11].add(currentNeighbor);
				} else {
					// inc. die Anzahl von nicht C-Atomen
					if (degree == 2) {
						groups[1].add(currentNeighbor);
					}
				}
			}
			// -C=S- Der Fall kommt vermutl. nicht vor
			if (edgeLabel == 2) {
				if (degree > 1) {
					groups[1].add(currentNeighbor);
					groups[2].add(currentNeighbor);
				}
			}
			break;
		default:

			if (degree == 1) {
				groups[4].add(currentNeighbor);
			} else {

				groups[1].add(currentNeighbor);
			}
			break;
		}
	}

	private boolean removeNodeFromGroupsArray(final Integer node,
			final HashSet<Integer> functionalGroup,
			final HashSet<Integer>[] groups) {
		boolean result = false;
		if (functionalGroup.contains(node)) {
			for (int i = 0; i < groups.length; i++) {
				groups[i].remove(node);
			}
			result = true;
		}
		return result;
	}
}
