/**
 * created May 24, 2006
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
package de.parsemis.parsers;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.graph.MutableGraph;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.GraphBasedFragment;
import de.parsemis.miner.general.HPEmbedding;

/**
 * This class is the parser of the simple undirected graph (.sug) format that
 * stores the graphs as symmetric adjacent matrices
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class SimpleUndirectedGraphParser<NodeType, EdgeType> implements
		GraphParser<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @return a default SUG-parser for Integer labels
	 */
	public static GraphParser<Integer, Integer> instance() {
		return new SimpleUndirectedGraphParser<Integer, Integer>(
				new IntLabelParser(), new IntLabelParser());
	}

	private final LabelParser<NodeType> np;

	private final LabelParser<EdgeType> ep;

	/**
	 * creates a new parser for the given label types
	 * 
	 * @param np
	 * @param ep
	 */
	public SimpleUndirectedGraphParser(final LabelParser<NodeType> np,
			final LabelParser<EdgeType> ep) {
		this.np = np;
		this.ep = ep;
	}

	public LabelParser<EdgeType> getEdgeParser() {
		return ep;
	}

	public LabelParser<NodeType> getNodeParser() {
		return np;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#parse(java.io.InputStream,
	 *      de.parsemis.graph.GraphFactory)
	 */
	public Collection<Graph<NodeType, EdgeType>> parse(final InputStream io,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException, IOException {
		final BufferedReader in = new BufferedReader(new InputStreamReader(io));

		String line = in.readLine();
		final int count = Integer.parseInt(line);
		final ArrayList<Graph<NodeType, EdgeType>> graphs = new ArrayList<Graph<NodeType, EdgeType>>(
				count);

		for (int i = 0; i < count; i++) {
			final String name = in.readLine();
			final HPMutableGraph<NodeType, EdgeType> g = (HPMutableGraph<NodeType, EdgeType>) factory
					.newGraph(name).toHPGraph();

			line = in.readLine();
			if (line != null && !line.startsWith("#")) {
				String[] cols = line.split(" ");

				int row = 0;
				final int[] nodes = new int[cols.length];
				do {
					nodes[row] = g.addNodeIndex(np.parse(cols[row]));
					for (int col = row - 1; col >= 0; col--) {
						if (cols[col].charAt(0) != '-') {
							g.addEdgeIndex(nodes[row], nodes[col], ep
									.parse(cols[col]), Edge.UNDIRECTED);
						}
					}
					row++;
					line = in.readLine();
					if (line != null) {
						cols = line.split(" ");
					}
				} while ((line != null) && !line.startsWith("#"));
			}
			graphs.add(g.toGraph());
		}

		return graphs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#parse(java.lang.String,
	 *      de.parsemis.graph.GraphFactory)
	 */
	public Graph<NodeType, EdgeType> parse(final String input,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException {
		final StringTokenizer rows = new StringTokenizer(input, "\n");

		final MutableGraph<NodeType, EdgeType> g = factory.newGraph(rows
				.nextToken());

		int row = 0;
		final int[] nodes = new int[rows.countTokens()];
		while (rows.hasMoreTokens()) {
			final String[] cols = rows.nextToken().split(" ");

			nodes[row] = g.addNode(np.parse(cols[row])).getIndex();
			for (int col = row - 1; col >= 0; col--) {
				if (cols[col].charAt(0) != '-') {
					g.addEdge(g.getNode(nodes[row]), g.getNode(nodes[col]), ep
							.parse(cols[col]), Edge.UNDIRECTED);
				}
			}
			row++;
		}
		return g;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serialize(de.parsemis.graph.Graph)
	 */
	public String serialize(final Graph<NodeType, EdgeType> g) {
		final HPGraph<NodeType, EdgeType> graph = g.toHPGraph();
		final int nodeCount = graph.getNodeCount();
		/*
		 * int maxLength = 0; for (int row = nodeCount - 1; row >= 0; row--) {
		 * for (int col = nodeCount - 1; col >= 0; col--) { if (row == col) {
		 * maxLength = Math.max(maxLength,
		 * Util.getDigits(g.getNodeLabel(g.getNode(row)))); } else { int edge =
		 * g.getEdge(g.getNode(row), g.getNode(col)); if (edge != Graph.NO_EDGE) {
		 * maxLength = Math.max(maxLength,
		 * Util.getDigits(g.getEdgeLabel(edge))); } } } }
		 * 
		 * m_format = new
		 * DecimalFormat("0000000000000000000000000000000000000".substring(0,
		 * maxLength));
		 */
		final StringBuffer b = new StringBuffer();
		b.append(graph.getName()).append('\n');
		for (int row = 0; row < nodeCount; row++) {
			for (int col = 0; col < nodeCount; col++) {
				if (row == col) {
					final NodeType nl = graph.getNodeLabel(row);
					final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
							.env(graph);
					if (env != null && env.nnil != null && env.nnil.equals(nl)) {
						b.append("n");
					} else {
						b.append(np.serialize(nl));
					}
				} else {
					final int edge = graph.getEdge(row, col);
					if (edge != HPGraph.NO_EDGE) {
						final EdgeType el = graph.getEdgeLabel(edge);
						final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
								.env(graph);
						if (env != null && env.enil != null
								&& env.enil.equals(el)) {
							b.append("e");
						} else {
							b.append(ep.serialize(el));
						}
					} else {
						b.append("-");
					}
				}
				if (col < nodeCount) {
					b.append(' ');
				}
			}
			if (row < nodeCount) {
				b.append('\n');
			}
		}

		return b.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serialize(java.io.OutputStream,
	 *      java.util.Collection)
	 */
	public void serialize(final OutputStream out,
			final Collection<Graph<NodeType, EdgeType>> graphs)
			throws IOException {
		final BufferedOutputStream output = new BufferedOutputStream(out);

		output.write((graphs.size() + "\n").getBytes());
		for (final Graph<NodeType, EdgeType> graph : graphs) {
			output.write(serialize(graph).getBytes());
			output.write("#\n".getBytes());
		}
		output.flush();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serialize(java.io.OutputStream,
	 *      de.parsemis.miner.FragmentSet)
	 */
	public void serializeFragments(final OutputStream out,
			final Collection<Fragment<NodeType, EdgeType>> frags)
			throws IOException {
		final BufferedOutputStream output = new BufferedOutputStream(out);

		out.write((frags.size() + "\n").getBytes());
		for (final Fragment<NodeType, EdgeType> frag : frags) {
			output.write(serialize(frag.toGraph()).getBytes());
			output.write(("# => " + frag.frequency() + "[").getBytes());

			if (frag instanceof GraphBasedFragment) {
				final Iterator<DataBaseGraph<NodeType, EdgeType>> git = frag
						.graphIterator();
				if (git.hasNext()) {
					output.write(git.next().toGraph().getName().getBytes());
				}
				while (git.hasNext()) {
					output.write(" ,".getBytes());
					output.write(git.next().toGraph().getName().getBytes());
				}
			} else {
				final Iterator<HPEmbedding<NodeType, EdgeType>> eit = frag
						.toHPFragment().iterator();
				if (eit.hasNext()) {
					output.write(eit.next().toString().getBytes());
				}
				while (eit.hasNext()) {
					output.write(" ,".getBytes());
					output.write(eit.next().toString().getBytes());
				}
			}

			output.write("]\n".getBytes());
		}
		output.flush();
	}

}
