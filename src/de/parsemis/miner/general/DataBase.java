/**
 * created Nov 27, 2007
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
package de.parsemis.miner.general;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.Settings;
import de.parsemis.utils.IntIterator;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class DataBase<NodeType, EdgeType> {

	final SortedSet<NodeType> freqNodes;

	final SortedSet<EdgeType> freqEdges;

	final Map<NodeType, Frequency> nfreq;

	final Map<EdgeType, Frequency> efreq;

	public DataBase(final Collection<Graph<NodeType, EdgeType>> graphs,
			final Settings<NodeType, EdgeType> settings) {
		nfreq = new HashMap<NodeType, Frequency>();
		efreq = new HashMap<EdgeType, Frequency>();

		// compute the frequency of each node and edge label
		for (final Graph<NodeType, EdgeType> o_graph : graphs) {
			final HPGraph<NodeType, EdgeType> graph = o_graph.toHPGraph();
			final Frequency gfreq = settings.getFrequency(o_graph);
			for (final IntIterator nit = graph.nodeIndexIterator(); nit
					.hasNext();) {
				final int node = nit.next();
				final NodeType label = graph.getNodeLabel(node);
				final Frequency freq = nfreq.get(label);
				if (freq == null) {
					nfreq.put(label, gfreq.clone());
				} else {
					freq.add(gfreq);
				}
			}
			for (final IntIterator eit = graph.edgeIndexIterator(); eit
					.hasNext();) {
				final int edge = eit.next();
				final EdgeType label = graph.getEdgeLabel(edge);
				final Frequency freq = efreq.get(label);
				if (freq == null) {
					efreq.put(label, gfreq.clone());
				} else {
					freq.add(gfreq);
				}
			}
		}

		if (settings.naturalOrderedNodeLabels) {
			freqNodes = settings.reverseOrderedNodeLabels ? new TreeSet<NodeType>(
					new Comparator<NodeType>() {
						@SuppressWarnings("unchecked")
						public int compare(final NodeType o1, final NodeType o2) {
							return ((Comparable) o2).compareTo(o1);
						}
					})
					: new TreeSet<NodeType>();
		} else {
			// sort nodes according to their frequency
			freqNodes = settings.reverseOrderedNodeLabels ? new TreeSet<NodeType>(
					new Comparator<NodeType>() {
						public int compare(final NodeType a, final NodeType b) {
							final int ret = nfreq.get(a)
									.compareTo(nfreq.get(b));
							if (ret == 0) {
								return (a.equals(b) ? 0 : System
										.identityHashCode(a)
										- System.identityHashCode(b));
							}
							return ret;
						}
					})
					: new TreeSet<NodeType>(new Comparator<NodeType>() {
						public int compare(final NodeType a, final NodeType b) {
							final int ret = nfreq.get(b)
									.compareTo(nfreq.get(a));
							if (ret == 0) {
								return (a.equals(b) ? 0 : System
										.identityHashCode(b)
										- System.identityHashCode(a));
							}
							return ret;
						}
					});
			;
		}

		// remove infrequent node labels
		for (final Map.Entry<NodeType, Frequency> entry : nfreq.entrySet()) {
			if (settings.minFreq.compareTo(entry.getValue()) <= 0) {
				freqNodes.add(entry.getKey());
			}
		}

		if (settings.naturalOrderedEdgeLabels) {
			freqEdges = settings.reverseOrderedEdgeLabels ? new TreeSet<EdgeType>(
					new Comparator<EdgeType>() {
						@SuppressWarnings("unchecked")
						public int compare(final EdgeType o1, final EdgeType o2) {
							return ((Comparable) o2).compareTo(o1);
						}
					})
					: new TreeSet<EdgeType>();
		} else {
			// sort edges according to their frequency
			freqEdges = settings.reverseOrderedEdgeLabels ? new TreeSet<EdgeType>(
					new Comparator<EdgeType>() {
						public int compare(final EdgeType a, final EdgeType b) {
							final int ret = efreq.get(a)
									.compareTo(efreq.get(b));
							if (ret == 0) {
								return (a.equals(b) ? 0 : System
										.identityHashCode(a)
										- System.identityHashCode(b));
							}
							return ret;
						}
					})
					: new TreeSet<EdgeType>(new Comparator<EdgeType>() {
						public int compare(final EdgeType a, final EdgeType b) {
							final int ret = efreq.get(b)
									.compareTo(efreq.get(a));
							if (ret == 0) {
								return (a.equals(b) ? 0 : System
										.identityHashCode(b)
										- System.identityHashCode(a));
							}
							return ret;
						}
					});
			;
		}
		// remove infrequent edge labels
		for (final Map.Entry<EdgeType, Frequency> entry : efreq.entrySet()) {
			if (settings.minFreq.compareTo(entry.getValue()) <= 0) {
				freqEdges.add(entry.getKey());
			}
		}

	}

	public Frequency edgeFreq(final EdgeType label) {
		return efreq.get(label);
	}

	public SortedSet<EdgeType> frequentEdgeLabels() {
		return freqEdges;
	}

	public SortedSet<NodeType> frequentNodeLabels() {
		return freqNodes;
	}

	public Frequency nodeFreq(final NodeType label) {
		return nfreq.get(label);
	}

}
