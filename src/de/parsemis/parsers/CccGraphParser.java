/**
 * Created Jun 04, 2007
 * 
 * @by Sebastian Lenz (siselenz@stud.informatik.uni-erlangen.de)
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
 * This class implements a (simple and not complete) parser of the CCC-graph
 * format This format is used by the research team of Prof Gasteiger. (See
 * http://www2.ccc.uni-erlangen.de for more information) I simply added .ccc so
 * the converter can decide which parser should be used.
 * 
 * @author Sebastian Lenz (siselenz@stud.informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */

public class CccGraphParser<NodeType, EdgeType> implements
		GraphParser<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @return a default CCC-parser for String labels
	 */
	public static final GraphParser<String, String> instance() {
		return new CccGraphParser<String, String>(new StringLabelParser(),
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
	public CccGraphParser(final LabelParser<NodeType> np,
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
		int h = 0;
		String lines = null;
		final LinkedList<Graph<NodeType, EdgeType>> graphs = new LinkedList<Graph<NodeType, EdgeType>>();
		Map<String, Integer> nodes = new HashMap<String, Integer>();

		// in -> filename
		final BufferedReader file = new BufferedReader(
				new InputStreamReader(in));
		HPMutableGraph<NodeType, EdgeType> g;
		file.readLine();
		while (file.readLine() != null) {

			while (lines != "$$$$") {
				g = (HPMutableGraph<NodeType, EdgeType>) factory.newGraph(
						"Graph" + i).toHPGraph();
				nodes = new HashMap<String, Integer>();
				// skip 3 lines
				file.readLine();

				lines = file.readLine();
				String line[];

				String line1 = lines.substring(0, 3);
				String line2 = lines.substring(3, 6);
				line1 = line1.trim();
				line2 = line2.trim();
				final int nodecnt = Integer.parseInt(line1);
				final int edgecnt = Integer.parseInt(line2);

				// nodes
				for (int j = 1; j <= nodecnt; j++) {
					lines = file.readLine();
					lines = lines.trim();
					line = lines.split("\\s+");
					if ("H".equals(line[3])) {
						h++;
					} else {
						nodes.put(Integer.toString(j), g.addNodeIndex(np
								.parse(line[3])));
					}

				}

				// edges
				// only undirected
				for (int k = 1; k <= edgecnt; k++) {
					lines = file.readLine();
					final String edge1 = (lines.substring(0, 3)).trim();
					final String edge2 = (lines.substring(3, 6)).trim();
					final String label = (lines.substring(6, 9)).trim();
					if (nodes.containsKey(edge1) && nodes.containsKey(edge2)) {
						g.addEdgeIndex(nodes.get(edge1), nodes.get(edge2), ep
								.parse(label), (Edge.UNDIRECTED));
					}
				}
				graphs.add(g.toGraph());
				i++;
				while (true) {
					if ("$$$$".equals(file.readLine())) {
						break;
					}

				}
				file.readLine();
				if (file.readLine() == null) {
					return graphs;
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