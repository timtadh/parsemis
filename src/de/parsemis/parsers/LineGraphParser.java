/**
 * created Feb 2, 2007
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
import java.util.Stack;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.HPEmbedding;
import de.parsemis.utils.IntIterator;

/**
 * This class can parse graphs in LineGraph (.lg) format
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
public class LineGraphParser<NodeType, EdgeType> implements
		GraphParser<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @return a default LG-parser for Integer labels
	 */
	public static GraphParser<Integer, Integer> instance() {
		return new LineGraphParser<Integer, Integer>(new IntLabelParser(),
				new IntLabelParser());
	}

	private final LabelParser<NodeType> np;

	private final LabelParser<EdgeType> ep;

	/**
	 * creates a new parser for the given label types
	 * 
	 * @param np
	 * @param ep
	 */
	public LineGraphParser(final LabelParser<NodeType> np,
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

	/**
	 * parsers multiple graphs for the given input stream and creats a
	 * Collection of the correpsonding graphs
	 * 
	 * @param io
	 * @param factory
	 *            a factory that creates a corresponding empty graph
	 * @return a collection of the represented graphs
	 * @throws ParseException
	 * @throws IOException
	 */
	public Collection<Graph<NodeType, EdgeType>> parse(final InputStream io,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException, IOException {
		final ArrayList<Graph<NodeType, EdgeType>> graphs = new ArrayList<Graph<NodeType, EdgeType>>();
		String text = "";
		final BufferedReader bin = new BufferedReader(new InputStreamReader(io));

		String line = bin.readLine();
		Graph<NodeType, EdgeType> g = null;
		while (line != null) {
			if (line.startsWith("t # ")) {
				if (text != "") {
					text = text.substring(0, text.length() - 1);
					g = parse(text, factory);
					graphs.add(g);
				}
				text = line + "\n";
			} else {
				text += line + "\n";
			}
			line = bin.readLine();
		}
		text = text.substring(0, text.length() - 1);
		g = parse(text, factory);
		graphs.add(g);
		return graphs;
	}

	/**
	 * parses one graph from the given input string und creates the graph that
	 * ist represented by it
	 * 
	 * @param input
	 * @param factory
	 *            a factory that creates a corresponding empty graph
	 * @return the corrsponding graph
	 * @throws ParseException
	 *             if the input string cannot be parsed correctly
	 */
	public Graph<NodeType, EdgeType> parse(final String input,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException {

		final String[] rows = input.split("\n");
		final String[] firstline = rows[0].split("\\s+");

		final HPMutableGraph<NodeType, EdgeType> g = (HPMutableGraph<NodeType, EdgeType>) factory
				.newGraph(firstline[2]).toHPGraph();

		// read graph from rows

		// nodes
		int i = 0;
		for (i = 1; (i < rows.length) && (rows[i].charAt(0) == 'v'); i++) {
			final String[] parts = rows[i].split("\\s+");
			final int index = Integer.parseInt(parts[1]);
			if (index != i - 1) {
				throw new ParseException("The node list is not sorted", i);
			}
			final int idx = g.addNodeIndex(getNodeParser().parse(parts[2]));
			assert idx == index : "graph do not orderd insert nodes";

		}

		// edges
		for (; (i < rows.length) && (rows[i].charAt(0) == 'e'); i++) {
			final String[] parts = rows[i].split("\\s+");

			g.addEdgeIndex(Integer.parseInt(parts[1]), Integer
					.parseInt(parts[2]), getEdgeParser().parse(parts[3]),
					Edge.OUTGOING);
		}

		return g.toGraph();
	}

	/**
	 * @param graph
	 * @return a serialized representation of the given graph
	 */
	public String serialize(final Graph<NodeType, EdgeType> graph) {
		// serialize graph
		String text = "";
		text += "t # " + graph.getName() + "\n";
		for (int i = 0; i < graph.getNodeCount(); i++) {
			text += "v " + graph.getNode(i).getIndex() + " "
					+ graph.getNode(i).getLabel() + "\n";
		}
		for (int i = 0; i < graph.getEdgeCount(); i++) {
			if (graph.getEdge(i).getDirection() == Edge.INCOMING) {
				text += "e " + graph.getNodeB(graph.getEdge(i)).getIndex()
						+ " " + graph.getNodeA(graph.getEdge(i)).getIndex()
						+ " " + graph.getEdge(i).getLabel() + "\n";
			} else {
				text += "e " + graph.getNodeA(graph.getEdge(i)).getIndex()
						+ " " + graph.getNodeB(graph.getEdge(i)).getIndex()
						+ " " + graph.getEdge(i).getLabel() + "\n";
			}
		}
		return text;
	}

	/**
	 * writes all serialized representations of the given graphs to the output
	 * stream
	 * 
	 * @param out
	 * @param graphs
	 * @throws IOException
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

	/**
	 * writes all serialized fragments to the output stream
	 * 
	 * @param out
	 * @param frags
	 * @throws IOException
	 */
	public void serializeFragments(final OutputStream out,
			final Collection<Fragment<NodeType, EdgeType>> frags)
			throws IOException {
		final BufferedOutputStream output = new BufferedOutputStream(out);

		HPGraph<NodeType, EdgeType> g = null;
		HPGraph<NodeType, EdgeType> gSuper = null;
		int e;
		final Stack<String> ids = new Stack<String>();

		for (final Fragment<NodeType, EdgeType> frag : frags) {

			output.write(serialize(frag.toGraph()).getBytes());

			for (final Embedding<NodeType, EdgeType> emb2 : frag) {
				final HPEmbedding<NodeType, EdgeType> emb = emb2
						.toHPEmbedding();
				g = emb.getSubGraph();
				gSuper = emb.getSuperGraph();
				final IntIterator nit = g.nodeIndexIterator();

				if (nit.hasNext()) {
					e = nit.next();
					ids.push(Integer.toString(emb.getSuperGraphNode(e)));
				}

				while (nit.hasNext()) {
					e = nit.next();
					ids.push(Integer.toString(emb.getSuperGraphNode(e)));
				}
				output.write("#=> ".getBytes());
				output.write(gSuper.getName().getBytes());
				while (!ids.isEmpty()) {
					output.write(" ".getBytes());
					output.write(ids.pop().getBytes());
				}
				output.write("\n".getBytes());
			}

		}
		output.flush();
	}
}
