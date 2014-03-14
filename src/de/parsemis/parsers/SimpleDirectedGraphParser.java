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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPMutableGraph;

/**
 * This class is the parser of the simple directed graph (.sdg) format that
 * stores the graphs as adjacent matrices
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
public class SimpleDirectedGraphParser<NodeType, EdgeType> extends
		SimpleUndirectedGraphParser<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @return a default SDG-parser for Integer labels
	 */
	public static GraphParser<Integer, Integer> instance() {
		return new SimpleDirectedGraphParser<Integer, Integer>(
				new IntLabelParser(), new IntLabelParser());
	}

	/**
	 * creates a new parser for the given label types
	 * 
	 * @param np
	 * @param ep
	 */
	public SimpleDirectedGraphParser(final LabelParser<NodeType> np,
			final LabelParser<EdgeType> ep) {
		super(np, ep);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#parse(java.io.InputStream,
	 *      de.parsemis.graph.GraphFactory)
	 */
	@Override
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
				final ArrayList<Integer> nodes = new ArrayList<Integer>(
						cols.length);
				for (int k = 0; k < cols.length; ++k) {
					nodes.add(g.addNodeIndex(null));
				}

				int row = 0;
				while ((line != null) && !line.startsWith("#")) {
					cols = line.split(" ");
					g.setNodeLabel(nodes.get(row), getNodeParser().parse(
							cols[row]));
					for (int col = 0; col < cols.length; ++col) {
						if ((col != row) && !cols[col].startsWith("-")) {
							g.addEdgeIndex(nodes.get(row), nodes.get(col),
									getEdgeParser().parse(cols[col]),
									Edge.OUTGOING);
						}
					}

					++row;
					line = in.readLine();
				}
				if (row == cols.length) {
					graphs.add(g.toGraph());
				} else {
					throw new ParseException("couldn't parse graph: " + row
							+ " rows vs. " + cols.length + " cols", i);
				}
			} else {
				graphs.add(g.toGraph());
				System.err.println("WARNING: empty graph in file");
			}

		}

		return graphs;
	}

	@Override
	public Graph<NodeType, EdgeType> parse(final String input,
			final GraphFactory<NodeType, EdgeType> factory)
			throws ParseException {
		final String[] rows = input.split("\n");

		final HPMutableGraph<NodeType, EdgeType> g = (HPMutableGraph<NodeType, EdgeType>) factory
				.newGraph(rows[0]).toHPGraph();
		final ArrayList<Integer> nodes = new ArrayList<Integer>();

		for (int row = 0; row < rows.length - 1; row++) {
			final String[] cols = rows[row + 1].split(" ");
			nodes.add(g.addNodeIndex(getNodeParser().parse(cols[row])));
		}

		for (int row = 0; row < rows.length - 1; row++) {
			final String[] cols = rows[row + 1].split(" ");

			for (int col = cols.length - 1; col >= 0; col--) {
				if ((row != col) && (cols[col].charAt(0) != '-')) {
					g.addEdgeIndex(nodes.get(row), nodes.get(col),
							getEdgeParser().parse(cols[col]), Edge.OUTGOING);
				}
			}
		}

		return g.toGraph();
	}

}
