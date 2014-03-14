/**
 * created Dec 4, 2004
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
package de.parsemis.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;

import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPListGraph;
import de.parsemis.miner.environment.Relabler;
import de.parsemis.miner.environment.Settings;
import de.parsemis.miner.environment.SimpleRelabler;
import de.parsemis.parsers.GraphParser;
import de.parsemis.parsers.StringLabelParser;
import de.parsemis.utils.GraphSet;

/**
 * This ...
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
public class GraphDiff {

	private static GraphParser<String, String> parser;

	public static <NodeType, EdgeType> Collection<Graph<NodeType, EdgeType>>[][] createDiff(
			final Collection<Graph<NodeType, EdgeType>>[] input) {
		final int len = input.length;
		@SuppressWarnings("unchecked")
		final Collection<Graph<NodeType, EdgeType>>[][] ret = new Collection[len][len];
		@SuppressWarnings("unchecked")
		final GraphSet<NodeType, EdgeType>[] sets = new GraphSet[len];
		final Relabler<NodeType, EdgeType> rel = new SimpleRelabler<NodeType, EdgeType>();
		for (int i = 0; i < len; i++) {
			// fill sets and detect duplicates
			sets[i] = new GraphSet<NodeType, EdgeType>(rel);
			ret[i][i] = new HashSet<Graph<NodeType, EdgeType>>();
			for (final Graph<NodeType, EdgeType> g : input[i]) {
				if (!sets[i].add(g.toHPGraph())) {
					ret[i][i].add(g);
				}
			}
		}
		for (int i = 0; i < len; i++) {
			for (int j = i + 1; j < len; j++) {
				// detect missing between i and j
				ret[i][j] = new HashSet<Graph<NodeType, EdgeType>>();
				ret[j][i] = new HashSet<Graph<NodeType, EdgeType>>();
				for (final Graph<NodeType, EdgeType> g : input[j]) {
					if (!sets[i].contains(g.toHPGraph())) {
						ret[i][j].add(g);
					}
				}
				for (final Graph<NodeType, EdgeType> g : input[i]) {
					if (!sets[j].contains(g.toHPGraph())) {
						ret[j][i].add(g);
					}
				}
			}
		}

		return ret;
	}

	private static <NodeType, EdgeType> Graph<NodeType, EdgeType> min(
			final Graph<NodeType, EdgeType> g1,
			final Graph<NodeType, EdgeType> g2) {
		if (g1 == null
				|| g2.getNodeCount() < g1.getNodeCount()
				|| (g2.getNodeCount() == g1.getNodeCount() && g2.getEdgeCount() < g1
						.getEdgeCount())) {
			return g2;
		}
		return g1;
	}

	public static void main(final String[] args) throws IOException,
			ParseException {

		if ((args.length < 1) || (args[0].equals("--help"))) {
			System.out.println("Usage: " + FileConverter.class.getName()
					+ " [-o outFile] inputFiles");
			System.exit(1);
		}
		int first = 0;
		PrintStream out = System.out;
		if (args[0].equals("-o")) {
			out = new PrintStream(new FileOutputStream(args[1]));
			first = 2;
		}
		final int count = args.length - first;
		final long starttime = System.currentTimeMillis();

		@SuppressWarnings("unchecked")
		final Collection<Graph<String, String>>[] all = new Collection[count];
		@SuppressWarnings("unchecked")
		final Graph<String, String>[][] min = new Graph[count][count];

		out.println("#--- PARSING ---");
		for (int i = 0; i < count; i++) {
			all[i] = parseOutput(args[i] = args[first + i]);
		}
		out.print("#--- FOUND " + args[0] + "[" + all[0].size() + "]");
		for (int i = 1; i < count; i++) {
			out.print(" " + args[i] + "[" + all[i].size() + "]");
		}
		out.println(" ("
				+ (((System.currentTimeMillis() - starttime) / 100) / 10.0)
				+ "s)---");

		out.println("#--- CREATE DIFF ---");
		final long time3 = System.currentTimeMillis();
		final Collection<Graph<String, String>> diff[][] = createDiff(all);
		out.println("#--- READY ("
				+ (((System.currentTimeMillis() - time3) / 100) / 10.0)
				+ "s) ---");

		out.println("#--- RESULTS ---");
		for (int i = 0; i < count; i++) {
			if (diff[i][i].size() > 0) {
				out.println("doppelte Graphen in " + args[i]);
				for (final Graph<String, String> graph : diff[i][i]) {
					out.println("  " + graph.getID() + ": "
							+ parser.serialize(graph));
					min[i][i] = min(min[i][i], graph);
				}
			}
			for (int j = 0; j < count; j++) {
				if (j != i && diff[i][j].size() > 0) {
					out.println("unbekannte Graphen in " + args[i] + " aus "
							+ args[j]);
					for (final Graph<String, String> graph : diff[i][j]) {
						out.println("  " + graph.getID() + ": "
								+ parser.serialize(graph));
						min[i][j] = min(min[i][j], graph);
					}
				}
			}
		}

		out.println("#--- MINIMUM ---");
		for (int i = 0; i < count; i++) {
			if (min[i][i] != null) {
				out.println("minimaler doppelter Graph in " + args[i] + ": "
						+ parser.serialize(min[i][i]));
			}
			for (int j = 0; j < count; j++) {
				if (j != i && min[i][j] != null) {
					out.println("minimaler unbekannter Graphe in " + args[i]
							+ " aus " + args[j] + ": "
							+ parser.serialize(min[i][j]));
				}
			}
		}
		out.println("#--- DONE ("
				+ (((System.currentTimeMillis() - starttime) / 100) / 10.0)
				+ "s)---");
		System.out.println("#--- DONE ---");
	}

	@SuppressWarnings("unchecked")
	private static Collection<Graph<String, String>> parseOutput(
			final String file) throws IOException, ParseException {
		parser = Settings.parseFileName(file, new StringLabelParser(),
				new StringLabelParser());
		final GraphFactory<String, String> factory = new HPListGraph.Factory<String, String>(
				parser.getNodeParser(), parser.getEdgeParser());
		return parser.parse(new FileInputStream(file), factory);
	}
}
