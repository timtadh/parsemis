/**
 * created Aug 18, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPListGraph;
import de.parsemis.graph.MutableGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.environment.Settings;
import de.parsemis.parsers.GraphParser;
import de.parsemis.parsers.SimpleDirectedGraphParser;
import de.parsemis.parsers.StringLabelParser;

/**
 * This class is a small random graph-generator
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public final class GraphGenerator {

	/** graph should be connected */
	public static final byte CONNECTED = 1;

	/** graph should be directed */
	public static final byte DIRECTED = 2;

	/** graph should be a DAG */
	public static final byte DAG = 4;

	private final static <NodeType, EdgeType> void addGraph(
			final MutableGraph<NodeType, EdgeType> dest,
			final Graph<NodeType, EdgeType> src, final double nodeReuse) {
		final HashMap<NodeType, ArrayList<Node<NodeType, EdgeType>>> reuse = new HashMap<NodeType, ArrayList<Node<NodeType, EdgeType>>>(
				src.getNodeCount());
		for (final Iterator<Node<NodeType, EdgeType>> it = dest.nodeIterator(); it
				.hasNext();) {
			final Node<NodeType, EdgeType> node = it.next();
			ArrayList<Node<NodeType, EdgeType>> l = reuse.get(node.getLabel());
			if (l == null) {
				reuse.put(node.getLabel(),
						l = new ArrayList<Node<NodeType, EdgeType>>());
			}
			l.add(node);
		}

		final HashMap<Node<NodeType, EdgeType>, Node<NodeType, EdgeType>> map = new HashMap<Node<NodeType, EdgeType>, Node<NodeType, EdgeType>>(
				src.getNodeCount());
		for (final Iterator<Node<NodeType, EdgeType>> it = src.nodeIterator(); it
				.hasNext();) {
			final Node<NodeType, EdgeType> node = it.next();
			if (Math.random() < nodeReuse) {
				final ArrayList<Node<NodeType, EdgeType>> l = reuse.get(node
						.getLabel());
				if (l != null && l.size() > 0) {
					for (final Node<NodeType, EdgeType> n : l) {
						System.out.print(n.getIndex() + " ");
					}
					final Node<NodeType, EdgeType> node2 = randElement(l);
					l.remove(node2);
					map.put(node, node2);
					System.out.println(": " + node2.getInDegree() + " :");
					for (final Node<NodeType, EdgeType> n : l) {
						System.out.print(" " + n.getIndex());
					}
				} else {
					map.put(node, dest.addNode(node.getLabel()));
				}
			} else {
				map.put(node, dest.addNode(node.getLabel()));
			}
		}

		for (final Iterator<Edge<NodeType, EdgeType>> it = src.edgeIterator(); it
				.hasNext();) {
			final Edge<NodeType, EdgeType> edge = it.next();
			dest.addEdge(map.get(edge.getNodeA()), map.get(edge.getNodeB()),
					edge.getLabel(), edge.getDirection());
		}
	}

	private final static <NodeType, EdgeType> ArrayList<ArrayList<Node<NodeType, EdgeType>>> components(
			final ArrayList<Node<NodeType, EdgeType>> nodes) {
		final BitSet used = new BitSet(nodes.size());
		final BitSet todo = new BitSet(nodes.size());
		final ArrayList<ArrayList<Node<NodeType, EdgeType>>> ret = new ArrayList<ArrayList<Node<NodeType, EdgeType>>>();
		for (int ack = 0; ack >= 0 && ack < nodes.size(); ack = used
				.nextClearBit(0)) {
			final ArrayList<Node<NodeType, EdgeType>> list = new ArrayList<Node<NodeType, EdgeType>>();
			ret.add(list);
			todo.set(ack);
			while (!todo.isEmpty()) {
				final int n = todo.nextSetBit(0);
				todo.clear(n);
				used.set(n);
				list.add(nodes.get(n));
				for (final Iterator<Edge<NodeType, EdgeType>> it = nodes.get(n)
						.edgeIterator(); it.hasNext();) {
					final int o = nodes.indexOf(it.next().getOtherNode(
							nodes.get(n)));
					todo.set(o, !used.get(o));
				}
			}
		}
		return ret;
	}

	/**
	 * Computes the faculty.
	 * 
	 * @param n
	 *            a number
	 * @return the faculty (n!) of the number
	 */
	public static long fac(final int n) {
		long f = 1;

		for (int i = 2; i <= n; i++) {
			f *= i;
		}
		return f;
	}

	/**
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 * @param factory
	 * @param nodeLabels
	 * @param nodeLabelDistribution
	 * @param edgeLabels
	 * @param edgeLabelDistribution
	 * @param nodeCount
	 * @param edgeCount
	 * @param flag
	 * @param seeds
	 * @param seedWeights
	 * @return a new random graph
	 */
	public static <NodeType, EdgeType> Graph<NodeType, EdgeType> generate(
			final GraphFactory<NodeType, EdgeType> factory,
			final NodeType[] nodeLabels, final double[] nodeLabelDistribution,
			final EdgeType[] edgeLabels, final double[] edgeLabelDistribution,
			final int nodeCount, int edgeCount, final byte flag,
			final Graph<NodeType, EdgeType>[] seeds, final double[] seedWeights) {
		if (nodeCount <= 1) {
			edgeCount = 0;
		}
		if ((flag & CONNECTED) == CONNECTED && edgeCount < nodeCount - 1) {
			edgeCount = nodeCount - 1;
		}
		if (nodeCount < 20) {
			final long full = fac(nodeCount - 1)
					/ ((flag & DAG) == DAG || (flag & DIRECTED) != DIRECTED ? 2
							: 1);
			if (edgeCount > full) {
				edgeCount = (int) full;
			}
		}
		final MutableGraph<NodeType, EdgeType> g = (MutableGraph<NodeType, EdgeType>) (seeds == null ? factory
				.newGraph()
				: randElement(seeds, seedWeights).clone());

		if (nodeCount == 0) {
			return g;
		}

		if (seeds != null) { // insert seeds
			for (Graph<NodeType, EdgeType> s = randElement(seeds, seedWeights); g
					.getNodeCount()
					+ s.getNodeCount() < nodeCount; s = randElement(seeds,
					seedWeights)) {
				addGraph(g, s, 0.2);
			}
		}
		final ArrayList<Node<NodeType, EdgeType>> nodes = new ArrayList<Node<NodeType, EdgeType>>(
				nodeCount);
		for (final Iterator<Node<NodeType, EdgeType>> it = g.nodeIterator(); it
				.hasNext();) {
			nodes.add(it.next());
		}
		while (g.getNodeCount() < nodeCount) {
			// complete nodes
			nodes
					.add(g.addNode(randElement(nodeLabels,
							nodeLabelDistribution)));
		}

		if ((flag & CONNECTED) == CONNECTED) { // connect the components
			final ArrayList<ArrayList<Node<NodeType, EdgeType>>> comps = components(nodes);
			for (int i = comps.size() - 1; i > 0; --i) {
				final ArrayList<Node<NodeType, EdgeType>> first = comps
						.remove(i);
				final ArrayList<Node<NodeType, EdgeType>> second = randElement(comps);
				g
						.addEdge(
								randElement(first),
								randElement(second),
								randElement(edgeLabels, edgeLabelDistribution),
								((flag & DIRECTED) == DIRECTED ? (Math.random() > 0.5 ? Edge.INCOMING
										: Edge.OUTGOING)
										: Edge.UNDIRECTED));
				second.addAll(first);
			}
		}
		int counter = 0;
		while (g.getEdgeCount() < edgeCount && counter < Integer.MAX_VALUE - 1) { // complete
			// edges
			++counter;
			final Node<NodeType, EdgeType> nodeA = randElement(nodes);
			final Node<NodeType, EdgeType> nodeB = randElement(nodes);
			final int direction = ((flag & DIRECTED) == DIRECTED ? (Math
					.random() > 0.5 ? Edge.INCOMING : Edge.OUTGOING)
					: Edge.UNDIRECTED);
			if (nodeA != nodeB
					&& ((flag & DAG) != DAG || (g.getEdge(nodeA, nodeB) == null && g
							.getEdge(nodeB, nodeA) == null))
					&& (g.getEdge(direction == Edge.INCOMING ? nodeB : nodeA,
							direction == Edge.INCOMING ? nodeA : nodeB) == null)) {
				g.addEdge(nodeA, nodeB, randElement(edgeLabels,
						edgeLabelDistribution), direction);
			}
		}
		if ((flag & DAG) == DAG) { // turn edges to create a DAG
			final ArrayList<Node<NodeType, EdgeType>> topsort = new ArrayList<Node<NodeType, EdgeType>>(
					nodeCount);
			final BitSet done = new BitSet(nodeCount);
			for (int i = 0; i < nodes.size(); ++i) {
				if (nodes.get(i).getOutDegree() == 0) {
					topsort.add(nodes.get(i));
				}
			}
			if (topsort.isEmpty()) { // no sink found? make one
				final Node<NodeType, EdgeType> node = randElement(nodes);
				final ArrayList<Edge<NodeType, EdgeType>> wrong = new ArrayList<Edge<NodeType, EdgeType>>(
						node.getOutDegree());
				for (final Iterator<Edge<NodeType, EdgeType>> it = node
						.outgoingEdgeIterator(); it.hasNext();) {
					wrong.add(it.next());
				}
				for (final Edge<NodeType, EdgeType> e : wrong) {
					g.addEdge(e.getNodeA(), e.getNodeB(), e.getLabel(), -e
							.getDirection());
					g.removeEdge(e);
				}
				topsort.add(node);
				done.set(nodes.indexOf(node));
			}
			for (int i = 0; i < nodeCount; i++) {
				final Node<NodeType, EdgeType> node = topsort.get(i);
				final ArrayList<Node<NodeType, EdgeType>> parents = new ArrayList<Node<NodeType, EdgeType>>();
				for (final Iterator<Edge<NodeType, EdgeType>> it = node
						.incommingEdgeIterator(); it.hasNext();) {
					parents.add(it.next().getOtherNode(node));
				}

				for (final Node<NodeType, EdgeType> parent : parents) {
					if (topsort.contains(parent)) {
						continue;
					}
					topsort.add(parent);
					final ArrayList<Edge<NodeType, EdgeType>> wrong = new ArrayList<Edge<NodeType, EdgeType>>();
					for (final Iterator<Edge<NodeType, EdgeType>> it = parent
							.outgoingEdgeIterator(); it.hasNext();) {
						final Edge<NodeType, EdgeType> edge = it.next();
						if (!topsort.contains(edge.getOtherNode(parent))) {
							wrong.add(edge);
						}
					}
					for (final Edge<NodeType, EdgeType> e : wrong) {
						g.addEdge(e.getNodeA(), e.getNodeB(), e.getLabel(), -e
								.getDirection());
						g.removeEdge(e);
					}
				}
			}
		}
		return g;
	}

	/**
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 * @param factory
	 * @param nodeLabels
	 * @param nodeLabelDistribution
	 * @param edgeLabels
	 * @param edgeLabelDistribution
	 * @param graphs
	 * @param averageNodeCount
	 * @param edgeDensity
	 * @param flag
	 * @param seeds
	 * @param averageFragmentSize
	 * @return a collection of new random graphs
	 */
	@SuppressWarnings("unchecked")
	public static <NodeType, EdgeType> Collection<Graph<NodeType, EdgeType>> generate(
			final GraphFactory<NodeType, EdgeType> factory,
			final NodeType[] nodeLabels, final double[] nodeLabelDistribution,
			final EdgeType[] edgeLabels, final double[] edgeLabelDistribution,
			final int graphs, final int averageNodeCount,
			final double edgeDensity, final byte flag, final int seeds,
			final int averageFragmentSize) {
		final HashSet<Graph<NodeType, EdgeType>> ret = new HashSet<Graph<NodeType, EdgeType>>(
				graphs);
		Graph<NodeType, EdgeType>[] seedGraphs = null;
		double[] seedWeights = null;
		if (seeds > 0) {
			System.out.print("generating seeds ");
			seedGraphs = new Graph[seeds];
			seedWeights = new double[seeds];
			double seedWeightSum = 0;
			for (int i = 0; i < seeds; ++i) {
				final int nodeCount = (int) (Math.random() * 2 * (averageFragmentSize - 1)) + 1;
				final int edgeCount = (int) (Math.random() * 2 * (edgeDensity
						* nodeCount * nodeCount)) + 1;
				seedGraphs[i] = generate(factory, nodeLabels,
						nodeLabelDistribution, edgeLabels,
						edgeLabelDistribution, nodeCount, edgeCount, flag,
						null, null);
				seedWeightSum += seedWeights[i] = Math.random()
						/ Math.sqrt(Math.sqrt(edgeCount));
				System.out.print(".");
			}
			seedWeights[0] /= seedWeightSum;
			for (int i = 1; i < seeds; ++i) {
				seedWeights[i] = seedWeights[i] / seedWeightSum
						+ seedWeights[i - 1];
			}
			System.out.print(" done.");
		}
		System.out.print("generating graphs ");
		for (int i = 0; i < graphs; ++i) {
			final int nodeCount = (int) (Math.random() * 2 * (averageNodeCount - 1)) + 1;
			final int edgeCount = (int) (Math.random() * 2 * (edgeDensity
					* nodeCount * nodeCount)) + 1;
			ret.add(generate(factory, nodeLabels, nodeLabelDistribution,
					edgeLabels, edgeLabelDistribution, nodeCount, edgeCount,
					flag, seedGraphs, seedWeights));
			System.out.print(".");
		}
		System.out.print(" done.");
		return ret;
	}

	/**
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 * @param graphs
	 * @param flag
	 * @return a new random graph
	 */
	public static <NodeType, EdgeType> Collection<Graph<String, String>> generate(
			final int graphs, final byte flag) {
		return generate(
				new HPListGraph.Factory<String, String>(
						new StringLabelParser(), new StringLabelParser()),
				new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" },
				new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 },
				new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" },
				new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 },
				graphs, 30, 0.2f, flag, 0, 14);
	}

	/**
	 * @param nodeCount
	 * @param edgeCount
	 * @param flag
	 * @return a new random graph
	 */
	public static Graph<String, String> generate(final int nodeCount,
			final int edgeCount, final byte flag) {
		return generate(
				new HPListGraph.Factory<String, String>(
						new StringLabelParser(), new StringLabelParser()),
				new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" },
				new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 },
				new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" },
				new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 },
				nodeCount, edgeCount, flag, null, null);
	}

	public static <NodeType, EdgeType> Collection<Graph<String, String>> generateDAG(
			final int graphs) {
		return generate(
				new HPListGraph.Factory<String, String>(
						new StringLabelParser(), new StringLabelParser()),
				// new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I",
				// "J" },
				// new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9,
				// 1.0 },
				new String[] { "A", "B", "C", "D", "E" }, new double[] { 0.2,
						0.4, 0.6, 0.8, 1.0 },
				new String[] { "0", "1", "2", "3" }, new double[] { 0.25, 0.5,
						0.75, 1.0 }, graphs, 8, 0.2f,
				(byte) (DAG | DIRECTED | CONNECTED), graphs / 20, 3);
	}

	/**
	 * @param nodeCount
	 * @param edgeCount
	 * @return a new random DAG
	 */
	public static Graph<String, String> generateDAG(final int nodeCount,
			final int edgeCount) {
		return generate(
				new HPListGraph.Factory<String, String>(
						new StringLabelParser(), new StringLabelParser()),
				new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" },
				new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0 },
				new String[] { "1", "1" }, new double[] { 0.0, 1.0 },
				nodeCount, edgeCount, (byte) (DAG | DIRECTED | CONNECTED),
				null, null);
	}

	private static OutputStream getFileStream(final String filename)
			throws IOException {
		if (filename == null) {
			return null;
		}
		if (filename.equals("-")) {
			return System.out;
		}
		OutputStream out = new FileOutputStream(filename);
		if (filename.endsWith(".gz")) {
			out = new GZIPOutputStream(out);
		}
		return out;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static final void main(final String[] args) throws Exception {
		if (args.length == 0) {
			final GraphParser<String, String> p = new SimpleDirectedGraphParser<String, String>(
					new StringLabelParser(), new StringLabelParser());
			p.serialize(System.out, generate(15,
					(byte) (CONNECTED | DIRECTED | DAG)));
		} else {
			if (args.length == 1) {
				final GraphParser<String, String> p = Settings.parseFileName(
						args[0], new StringLabelParser(),
						new StringLabelParser());
				final OutputStream out = getFileStream(args[0]);
				p.serialize(out, generateDAG(100));
				out.close();
			} else if (args.length == 2) {
				final GraphParser<String, String> p = new SimpleDirectedGraphParser<String, String>(
						new StringLabelParser(), new StringLabelParser());
				final int nodeCount = Integer.parseInt(args[0]);
				final int edgeCount = Integer.parseInt(args[1]);

				System.out
						.print(p.serialize(generateDAG(nodeCount, edgeCount)));
			} else {
				System.err
						.println("Usage: java -cp ... GraphGenerator nodeCount edgeCount");
				System.exit(-1);
			}
		}
	}

	private final static <ElemType> ElemType randElement(
			final ArrayList<ElemType> list) {
		return list.get((int) (Math.random() * list.size()));
	}

	public final static <ElemType> ElemType randElement(
			final ElemType[] labels, final double[] distribution) {
		final double r = Math.random();
		int index = 0;
		while (r > distribution[index]) {
			index++;
		}
		return labels[index];
		// final int index = Arrays.binarySearch(distribution, Math.random());
		// return labels[index<0?-index-1:index];
	}

}
