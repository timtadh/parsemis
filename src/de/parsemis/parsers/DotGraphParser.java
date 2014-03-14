/**
 * created May 8, 2006
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Fragment;
import de.parsemis.parsers.antlr.DotLexer;
import de.parsemis.parsers.antlr.DotParser;
import de.parsemis.utils.IntIterator;

/**
 * This class implements a (simple and not complete) parser of the Dot-file
 * format.
 * <p>
 * To use the parser a current version of antlr has to be available.
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
public class DotGraphParser<NodeType, EdgeType> implements
		GraphParser<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @return a default DOT-parser for String labels
	 */
	public static final GraphParser<String, String> instance() {
		return new DotGraphParser<String, String>(new StringLabelParser(),
				new StringLabelParser());
	}

	private final LabelParser<NodeType> np;

	private final LabelParser<EdgeType> ep;

	/**
	 * creates a new parser for the given label types
	 * 
	 * @param np
	 * @param ep
	 */
	public DotGraphParser(final LabelParser<NodeType> np,
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
	public Collection<Graph<NodeType, EdgeType>> parse(final InputStream in,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException, IOException {
		final DotLexer l = new DotLexer(in);
		final DotParser p = new DotParser(l);

		final LinkedList<Graph<NodeType, EdgeType>> graphs = new LinkedList<Graph<NodeType, EdgeType>>();

		while (in.available() > 1) {
			// TODO: detect the correct file end, have to change the grammar
			// correctly throw exceptions
			final Graph<NodeType, EdgeType> tmp = parseSingleGraph(p, factory);
			graphs.add(tmp);
		}

		return graphs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#parse(java.lang.String,
	 *      de.parsemis.graph.GraphFactory)
	 */
	public Graph<NodeType, EdgeType> parse(final String text,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException {
		final StringReader sr = new StringReader(text);

		final DotLexer l = new DotLexer(sr);
		final DotParser p = new DotParser(l);

		return parseSingleGraph(p, factory);
	}

	private Graph<NodeType, EdgeType> parseSingleGraph(final DotParser p,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException {
		try {
			p.graph();
		} catch (final RecognitionException e) {
			e.printStackTrace();
			throw new ParseException(e.toString(), e.getLine());
		} catch (final TokenStreamException e) {
			e.printStackTrace();
			throw new ParseException(e.toString(), 0);
		}

		final HPMutableGraph<NodeType, EdgeType> g = (HPMutableGraph<NodeType, EdgeType>) factory
				.newGraph(p.getName()).toHPGraph();

		final Map<String, Integer> nodes = new HashMap<String, Integer>();

		// Add nodes to graph.
		final Map<String, String> nodeLabels = p.getNodeMap();
		for (final Map.Entry<String, String> e : nodeLabels.entrySet()) {
			nodes.put(e.getKey(), g.addNodeIndex(np.parse(e.getValue())));
		}

		// Add edges to graph.
		final Collection<DotParser.EdgeDesc> edges = p.getEdges();
		for (final DotParser.EdgeDesc e : edges) {
			g.addEdgeIndex(nodes.get(e.nodeA), nodes.get(e.nodeB), ep
					.parse(e.label), (e.undirected ? Edge.UNDIRECTED
					: Edge.OUTGOING));
		}

		return g.toGraph();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serialize(de.parsemis.graph.Graph)
	 */
	public String serialize(final Graph<NodeType, EdgeType> graph) {
		return serialize(graph, "transparent");
	}

	/**
	 * serialize the given graph and set its background color to the given color
	 * string
	 * 
	 * @param graph2
	 * @param bgcolor
	 * @return a serialized graph
	 */
	public String serialize(final Graph<NodeType, EdgeType> graph2,
			final String bgcolor) {
		final HPGraph<NodeType, EdgeType> graph = graph2.toHPGraph();
		final StringBuffer buf = new StringBuffer(2048);

		buf
				.append((graph.getEdgeCount() > 0
						&& graph.getDirection(graph.edgeIndexIterator().next()) != Edge.UNDIRECTED ? "di"
						: "")
						+ "graph " + "\"" + graph.getName() + "\"" + " {\n");
		buf.append("\tgraph [bgcolor=\"" + bgcolor + "\"]\n");
		for (final IntIterator nit = graph.nodeIndexIterator(); nit.hasNext();) {
			final int n = nit.next();
			buf.append("\tNode_" + n);
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(graph);
			if (env != null && env.nnil != null
					&& env.nnil.equals(graph.getNodeLabel(n))) {
				buf
						.append(" [label=\"glueNode\",fontcolor=\"#a9bbf4\",color=\"#a9bbf4\"];\n");
			} else {
				buf.append(" [label=\"" + np.serialize(graph.getNodeLabel(n))
						+ "\"];\n");
			}
		}

		for (final IntIterator eit = graph.edgeIndexIterator(); eit.hasNext();) {
			final int e = eit.next();
			switch (graph.getDirection(e)) {
			case Edge.UNDIRECTED:
				buf.append("\tNode_" + graph.getNodeA(e) + " -- Node_"
						+ graph.getNodeB(e));
				break;
			case Edge.OUTGOING:
				buf.append("\tNode_" + graph.getNodeA(e) + " -> Node_"
						+ graph.getNodeB(e));
				break;
			case Edge.INCOMING:
				buf.append("\tNode_" + graph.getNodeB(e) + " -> Node_"
						+ graph.getNodeA(e));
				break;
			}
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(graph);
			if (env != null && env.enil != null
					&& env.enil.equals(graph.getEdgeLabel(e))) {
				buf
						.append(" [label=\"glueEdge\",fontcolor=\"#a9bbf4\",color=\"#a9bbf4\"];\n");
			} else {
				buf.append(" [label=\"" + ep.serialize(graph.getEdgeLabel(e))
						+ "\"];\n");
			}
		}

		buf.append("}");
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
		final Iterator<Graph<NodeType, EdgeType>> git = graphs.iterator();
		if (git.hasNext()) {
			out.write(serialize(git.next()).getBytes());
		}
		while (git.hasNext()) {
			out.write("\n".getBytes());
			out.write(serialize(git.next()).getBytes());
		}
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
		// TODO: check for comments in dot
		final BufferedOutputStream output = new BufferedOutputStream(out);

		// out.write((frags.size() + "\n").getBytes());
		for (final Fragment<NodeType, EdgeType> frag : frags) {
			output.write(serialize(frag.toGraph()).getBytes());
			output.write(("# => " + frag.frequency() + "[").getBytes());
			final Iterator<DataBaseGraph<NodeType, EdgeType>> git = frag
					.graphIterator();
			if (git.hasNext()) {
				output.write(git.next().toGraph().getName().getBytes());
			}
			while (git.hasNext()) {
				output.write(" ,".getBytes());
				output.write(git.next().toGraph().getName().getBytes());
			}
			output.write("]\n".getBytes());
		}
		output.flush();
	}

}
