/**
 * Created on Jul 6, 2006
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

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Embedding;
import de.parsemis.miner.general.Fragment;
import de.parsemis.miner.general.GraphBasedFragment;

/**
 * This class implements a parser for the subdue graph file format
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
public class SubdueParser<NodeType, EdgeType> implements
		GraphParser<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @return a default Subdue-parser for Integer labels
	 */
	public static GraphParser<String, String> instance() {
		return new SubdueParser<String, String>(new StringLabelParser(),
				new StringLabelParser());
	}

	private final LabelParser<NodeType> np;

	private final LabelParser<EdgeType> ep;

	private int[] nodes;

	private final boolean directed = true;

	/**
	 * creates a new parser for the given label types
	 * 
	 * @param np
	 * @param ep
	 */
	public SubdueParser(final LabelParser<NodeType> np,
			final LabelParser<EdgeType> ep) {
		this.np = np;
		this.ep = ep;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#getEdgeParser()
	 */
	public LabelParser<EdgeType> getEdgeParser() {
		return ep;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#getNodeParser()
	 */
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
		final ArrayList<Graph<NodeType, EdgeType>> graphs = new ArrayList<Graph<NodeType, EdgeType>>();
		final BufferedReader bin = new BufferedReader(new InputStreamReader(io));

		int i = -1;
		String line = bin.readLine();
		HPMutableGraph<NodeType, EdgeType> g = null;
		while (line != null) {
			final HPMutableGraph<NodeType, EdgeType> neu = parseLine(line, g,
					factory, ++i);
			if (neu != g) {
				g = neu;
				graphs.add(g.toGraph());
			}
			line = bin.readLine();
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
		HPMutableGraph<NodeType, EdgeType> g = null;
		final String[] lines = input.split("[\\n\\r]");
		for (int i = 0; i < lines.length; ++i) {
			g = parseLine(lines[i], g, factory, i);
		}
		return g.toGraph();
	}

	private HPMutableGraph<NodeType, EdgeType> parseLine(final String line,
			HPMutableGraph<NodeType, EdgeType> g,
			final GraphFactory<NodeType, EdgeType> factory, final int i)
			throws ParseException {
		if (g == null) { // init for first Graph
			g = (HPMutableGraph<NodeType, EdgeType>) factory.newGraph()
					.toHPGraph();
			// ((ClassifiedGraph) g).setClassFrequencies(new
			// float[]{(float)1.0,(float)0.0});
			nodes = new int[10];
		}
		if (line.length() == 0) {
			return g;
		}
		final char c = line.charAt(0);
		if (c == '%') {
			return g;
		}
		if (line.equals("XP")) {
			if (g.getNodeCount() != 0) {
				g = (HPMutableGraph<NodeType, EdgeType>) factory.newGraph()
						.toHPGraph();
			}
			// ((ClassifiedGraph) g).setClassFrequencies(new
			// float[]{(float)1.0,(float)0.0});
			nodes = new int[10];
			return g;
		}
		if (line.equals("XN")) {
			if (g.getNodeCount() != 0) {
				g = (HPMutableGraph<NodeType, EdgeType>) factory.newGraph()
						.toHPGraph();
			}
			// ((ClassifiedGraph) g).setClassFrequencies(new
			// float[]{(float)0.0,(float)1.0});
			nodes = new int[10];
			return g;
		}

		final String[] parts = line.split("\\s+");

		final int idxa = Integer.parseInt(parts[1]);
		if (c == 'v') {
			if (idxa >= nodes.length) {
				final int[] tmp = new int[3 * idxa / 2];
				System.arraycopy(nodes, 0, tmp, 0, nodes.length);
				nodes = tmp;
			}
			nodes[idxa] = g.addNodeIndex(np.parse(parts[2]));
			return g;
		}
		if (c == 'd' || c == 'e' | c == 'u') {
			if (idxa >= nodes.length) {
				throw new ParseException("The node " + idxa
						+ " is not yet defined", i);
			}
			final int idxb = Integer.parseInt(parts[2]);
			if (idxb >= nodes.length) {
				throw new ParseException("The node " + idxb
						+ " is not yet defined", i);
			}

			final String l = (parts.length > 3 ? parts[3] : "NONE");
			g.addEdgeIndex(nodes[idxa], nodes[idxb], ep.parse(l), (c == 'd'
					|| directed && c == 'e' ? Edge.OUTGOING : Edge.UNDIRECTED));

			return g;
		}
		throw new ParseException(line + ": not parseble", i);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serialize(de.parsemis.graph.Graph)
	 */
	public String serialize(final Graph<NodeType, EdgeType> graph) {
		final HPGraph<NodeType, EdgeType> g = graph.toHPGraph();
		final StringBuffer buf = new StringBuffer(1024);

		buf.append("% " + g.getName() + "\n");
		for (int node = 0; node < g.getMaxNodeIndex(); node++) {
			if (!g.isValidNode(node)) {
				continue;
			}
			buf.append("v " + (1 + node) + " "
					+ np.serialize(g.getNodeLabel(node)) + "\n");
		}

		for (int edge = 0; edge < g.getMaxEdgeIndex(); edge++) {
			if (!g.isValidEdge(edge)) {
				continue;
			}
			switch (g.getDirection(edge)) {
			case Edge.UNDIRECTED:
				buf.append("u " + (1 + g.getNodeA(edge)) + " "
						+ (1 + g.getNodeB(edge)) + " "
						+ ep.serialize(g.getEdgeLabel(edge)) + "\n");
				break;
			case Edge.OUTGOING:
				buf.append("d " + (1 + g.getNodeA(edge)) + " "
						+ (1 + g.getNodeB(edge)) + " "
						+ ep.serialize(g.getEdgeLabel(edge)) + "\n");
				break;
			case Edge.INCOMING:
				buf.append("d " + (1 + g.getNodeB(edge)) + " "
						+ (1 + g.getNodeA(edge)) + " "
						+ ep.serialize(g.getEdgeLabel(edge)) + "\n");
				break;
			default:
				buf.append("e " + (1 + g.getNodeA(edge)) + " "
						+ (1 + g.getNodeB(edge)) + " "
						+ ep.serialize(g.getEdgeLabel(edge)) + "\n");
			}
		}

		return buf.toString();
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
		final BufferedOutputStream bout;
		if (!(out instanceof BufferedOutputStream)) {
			bout = new BufferedOutputStream(out);
		} else {
			bout = (BufferedOutputStream) out;
		}

		for (final Graph<NodeType, EdgeType> graph : graphs) {
			final HPGraph<NodeType, EdgeType> g = graph.toHPGraph();
			bout.write(("XP\n").getBytes());
			bout.write(("% " + g.getName() + "\n").getBytes());
			for (int node = 0; node < g.getMaxNodeIndex(); node++) {
				if (!g.isValidNode(node)) {
					continue;
				}
				bout
						.write(("v " + (1 + node) + " "
								+ np.serialize(g.getNodeLabel(node)) + "\n")
								.getBytes());
			}

			for (int edge = 0; edge < g.getMaxEdgeIndex(); edge++) {
				if (!g.isValidEdge(edge)) {
					continue;
				}
				switch (g.getDirection(edge)) {
				case Edge.UNDIRECTED:
					bout.write(("u " + (1 + g.getNodeA(edge)) + " "
							+ (1 + g.getNodeB(edge)) + " "
							+ ep.serialize(g.getEdgeLabel(edge)) + "\n")
							.getBytes());
					break;
				case Edge.OUTGOING:
					bout.write(("d " + (1 + g.getNodeA(edge)) + " "
							+ (1 + g.getNodeB(edge)) + " "
							+ ep.serialize(g.getEdgeLabel(edge)) + "\n")
							.getBytes());
					break;
				case Edge.INCOMING:
					bout.write(("d " + (1 + g.getNodeB(edge)) + " "
							+ (1 + g.getNodeA(edge)) + " "
							+ ep.serialize(g.getEdgeLabel(edge)) + "\n")
							.getBytes());
					break;
				default:
					bout.write(("e " + (1 + g.getNodeA(edge)) + " "
							+ (1 + g.getNodeB(edge)) + " "
							+ ep.serialize(g.getEdgeLabel(edge)) + "\n")
							.getBytes());
				}
			}
		}
		bout.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serializeFragments(java.io.OutputStream,
	 *      java.util.Collection)
	 */
	public void serializeFragments(final OutputStream out,
			final Collection<Fragment<NodeType, EdgeType>> frags)
			throws IOException {
		final BufferedOutputStream bout;
		if (!(out instanceof BufferedOutputStream)) {
			bout = new BufferedOutputStream(out);
		} else {
			bout = (BufferedOutputStream) out;
		}

		for (final Fragment<NodeType, EdgeType> frag : frags) {
			final HPGraph<NodeType, EdgeType> g = frag.toGraph()
					.toHPGraph();
			bout.write(("XP\n").getBytes());
			bout.write(("% " + g.getName() + "\n").getBytes());
			for (int node = 0; node < g.getMaxNodeIndex(); node++) {
				if (!g.isValidNode(node)) {
					continue;
				}
				bout
						.write(("v " + (1 + node) + " "
								+ np.serialize(g.getNodeLabel(node)) + "\n")
								.getBytes());
			}

			for (int edge = 0; edge < g.getMaxEdgeIndex(); edge++) {
				if (!g.isValidEdge(edge)) {
					continue;
				}
				switch (g.getDirection(edge)) {
				case Edge.UNDIRECTED:
					bout.write(("u " + (1 + g.getNodeA(edge)) + " "
							+ (1 + g.getNodeB(edge)) + " "
							+ ep.serialize(g.getEdgeLabel(edge)) + "\n")
							.getBytes());
					break;
				case Edge.OUTGOING:
					bout.write(("d " + (1 + g.getNodeA(edge)) + " "
							+ (1 + g.getNodeB(edge)) + " "
							+ ep.serialize(g.getEdgeLabel(edge)) + "\n")
							.getBytes());
					break;
				case Edge.INCOMING:
					bout.write(("d " + (1 + g.getNodeB(edge)) + " "
							+ (1 + g.getNodeA(edge)) + " "
							+ ep.serialize(g.getEdgeLabel(edge)) + "\n")
							.getBytes());
					break;
				default:
					bout.write(("e " + (1 + g.getNodeA(edge)) + " "
							+ (1 + g.getNodeB(edge)) + " "
							+ ep.serialize(g.getEdgeLabel(edge)) + "\n")
							.getBytes());
				}
			}
			bout.write(("% => " + frag.frequency() + "[").getBytes());
			if (frag instanceof GraphBasedFragment) {
				final Iterator<DataBaseGraph<NodeType, EdgeType>> git = frag
						.graphIterator();
				if (git.hasNext()) {
					bout.write(git.next().toGraph().getName().getBytes());
				}
				while (git.hasNext()) {
					bout.write(" ,".getBytes());
					bout.write(git.next().toGraph().getName().getBytes());
				}
			} else {
				final Iterator<Embedding<NodeType, EdgeType>> eit = frag
						.iterator();
				if (eit.hasNext()) {
					bout.write(eit.next().toString().getBytes());
				}
				while (eit.hasNext()) {
					bout.write(" ,".getBytes());
					bout.write(eit.next().toString().getBytes());
				}
			}
			bout.write("]\n".getBytes());

		}
		bout.flush();
	}

}
