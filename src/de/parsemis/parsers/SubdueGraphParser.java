/**
 * Created May 24, 2007
 * 
 * @by Sebastian Lenz (siselenz@stud.informatik.uni-erlangen.de)
 * 
 * Copyright 2007 Sebastian Lenz
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.general.Fragment;

/**
 * This class implements a (simple and not complete) parser of the subdue-graph
 * format
 * 
 * @author Sebastian Lenz (siselenz@stud.informatik.uni-erlangen.de)
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */

public class SubdueGraphParser<NodeType, EdgeType> implements
		GraphParser<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @return a default Subdue-parser for String labels
	 */
	public static final GraphParser<String, String> instance() {
		return new SubdueGraphParser<String, String>(new StringLabelParser(),
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
	public SubdueGraphParser(final LabelParser<NodeType> np,
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
	 * inputstream in -> input file called from utils.FileConverter
	 */
	public Collection<Graph<NodeType, EdgeType>> parse(final InputStream in,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException, IOException {

		return parseSingleGraph(in, factory);
	}

	/*
	 * not used yet -> no implementation
	 * 
	 */
	public Graph<NodeType, EdgeType> parse(final String text,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException {
		return null;
	}

	private Collection<Graph<NodeType, EdgeType>> parseSingleGraph(
			final InputStream in, final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException, IOException {
		int i = 1;
		String lines;
		final LinkedList<Graph<NodeType, EdgeType>> graphs = new LinkedList<Graph<NodeType, EdgeType>>();
		final Map<String, Integer> nodes = new HashMap<String, Integer>();

		// in -> filename
		final BufferedReader file = new BufferedReader(
				new InputStreamReader(in));
		HPMutableGraph<NodeType, EdgeType> g = (HPMutableGraph<NodeType, EdgeType>) factory
				.newGraph("Graph" + i).toHPGraph();
		// read file
		while ((lines = file.readLine()) != null) {
			final String line[] = lines.split("\\s");
			// node
			if ("v".equals(line[0])) {
				nodes.put(line[1], g.addNodeIndex(np.parse(line[2])));
				// edge, directed
			} else if ("e".equals(line[0]) || "d".equals(line[0])) {
				g.addEdgeIndex(nodes.get(line[1]), nodes.get(line[2]), ep
						.parse(line[3]), (Edge.OUTGOING));
				// edge undirected
			} else if ("u".equals(line[0])) {
				g.addEdgeIndex(nodes.get(line[1]), nodes.get(line[2]), ep
						.parse(line[3]), (Edge.UNDIRECTED));
				// newgraph
			} else if ("XP".equals(line[0]) || "XN".equals(line[0])) {
				// ignore possible empty graph
				if (g.getNodeCount() > 0) {
					graphs.add(g.toGraph());
					i++;
					g = (HPMutableGraph<NodeType, EdgeType>) factory.newGraph(
							"Graph" + i).toHPGraph();
				}
			}
		}
		return graphs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.parsers.GraphParser#serialize(src.de.pargra.graph.Graph)
	 */
	public String serialize(final Graph<NodeType, EdgeType> graph) {
		return null;
	}

	/**
	 * serialize the given graph and set its background color to the given color
	 * sting
	 * 
	 * @param graph
	 * @param bgcolor
	 * @return a serialized graph
	 */
	public String serialize(final Graph<NodeType, EdgeType> graph,
			final String bgcolor) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see src.de.pargra.parsers.GraphParser#serialize(java.io.OutputStream,
	 *      java.util.Collection)
	 */
	public void serialize(final OutputStream out,
			final Collection<Graph<NodeType, EdgeType>> graphs)
			throws IOException {
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
	}

}
