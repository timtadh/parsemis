/**
 * created May 29, 2006
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphComparator;
import de.parsemis.graph.ListGraph;
import de.parsemis.graph.Node;
import de.parsemis.parsers.DotGraphParser;
import de.parsemis.parsers.GraphParser;
import de.parsemis.parsers.SimpleDirectedGraphParser;
import de.parsemis.parsers.SimpleUndirectedGraphParser;
import de.parsemis.parsers.StringLabelParser;

/**
 * This class ... TODO
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
@SuppressWarnings("unchecked")
public class OutputComparator {

	private final static class GraphHashTable {
		/**
		 * @param g
		 * @return a simple HashCode for the given Graph
		 */
		public static int getHashCode(final Graph g) {
			int hash = 1;
			for (final Iterator<Node> nit = g.nodeIterator(); nit.hasNext();) {
				hash *= (nit.next().getDegree() + 3);
			}
			hash *= 1023;
			hash += g.getNodeCount() * 255 + g.getEdgeCount();
			return hash;
		}

		private final GraphComparator gc;
		private final Map<Integer, Collection<Graph>> map;

		/**
		 * 
		 */
		public GraphHashTable() {
			gc = new GraphComparator();
			map = new HashMap<Integer, Collection<Graph>>();
		}

		/**
		 * inserts the given graph in the table
		 * 
		 * @param graph
		 * @return a structural identivcal graph, if one is still added
		 */
		public Graph insert(final Graph graph) {
			Graph old = null;
			List<Graph> list;
			if ((list = (List) map.get(getHashCode(graph))) == null) {
				list = new ArrayList<Graph>();
				map.put(getHashCode(graph), list);
				list.add((Graph) graph.clone());
				return null;
			}
			final Iterator it = list.iterator();
			while (it.hasNext()) {
				old = (Graph) it.next();
				if (gc.compare(graph, old) == 0) {
					return old;
				}
			}
			list.add((Graph) graph.clone());
			return null;
		}

		/**
		 * inserts the given graph in the table
		 * 
		 * @param graph
		 * @return true, if no structural identical graph was added before
		 */
		public boolean insertFirstTime(final Graph graph) {
			return (insert(graph) == null);
		}

	}

	private static GraphParser gp;

	/**
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(final String[] args) throws FileNotFoundException,
			IOException, ParseException {
		// TODO Auto-generated method stub
		int count = 0;
		boolean output = false;
		GraphParser serializer = new SimpleDirectedGraphParser<String, String>(
				new StringLabelParser(), new StringLabelParser());
		FileOutputStream out = null;

		if (args[count].startsWith("--")) {
			final int index = args[count].indexOf('=');
			String[] temp = null;
			if (index >= 0) {
				final String[] tmp = { args[count].substring(0, index),
						args[count].substring(index + 1) };
				temp = tmp;
			} else {
				final String[] tmp = { args[count] };
				temp = tmp;
			}
			if (temp[0].equals("--outputFile")) {
				output = true;
				final String outputFilename = temp[1];
				if (outputFilename.endsWith(".edg")) {
					serializer = new SimpleDirectedGraphParser<String, String>(
							new StringLabelParser(), new StringLabelParser());
				}
				if (outputFilename.endsWith(".sdg")) {
					serializer = SimpleDirectedGraphParser.instance();
				}
				if (outputFilename.endsWith(".sug")) {
					serializer = SimpleUndirectedGraphParser.instance();
				}
				if (outputFilename.endsWith(".dot")) {
					serializer = DotGraphParser.instance();
				}

				out = new FileOutputStream(outputFilename);
			} else {
				throw new RuntimeException("Unknown Option: " + temp[0]);
			}

			count++;
		}

		final Collection<Graph> graphs1 = parseFile(args[count]);
		args[0] = args[count];
		count++;
		final Collection<Graph> graphs2 = parseFile(args[count]);
		args[1] = args[count];
		count++;

		GraphHashTable ght = new GraphHashTable();
		GraphHashTable ght2 = new GraphHashTable();

		int unknownGraphs0 = 0;
		int unknownGraphs1 = 0;
		int duplicateGraphs0 = 0;
		int duplicateGraphs1 = 0;

		final ArrayList<Graph> unknownGraphColl0 = new ArrayList<Graph>();
		final ArrayList<Graph> unknownGraphColl1 = new ArrayList<Graph>();
		final ArrayList<Graph> duplicateGraphColl0 = new ArrayList<Graph>();
		final ArrayList<Graph> duplicateGraphColl1 = new ArrayList<Graph>();

		System.out.println("#--- START " + args[0] + " - " + args[1] + " ---");
		for (final Graph g : graphs1) {
			ght.insertFirstTime(g);
		}
		for (final Graph g : graphs2) {

			if (ght.insertFirstTime(g)) {
				System.out.println("unbekannter Graph in " + args[1] + " "
						+ GraphHashTable.getHashCode(g));
				System.out.println("  " + g.getID() + ": " + gp.serialize(g));
				// if (min2==null) min2=g2[i];
				// if (g2[i].getNodeCount()<min2.getNodeCount()) min2=g2[i];
				unknownGraphs1++;
				if (output) {
					unknownGraphColl1.add(g);
				}
			}

			Graph old;
			if ((old = ght2.insert(g)) != null) {
				System.out.println("doppelter Graph in " + args[1]);
				System.out.println("  " + old.getID() + ": "
						+ gp.serialize(old));
				System.out.println("  " + g.getID() + ": " + g.getName());
				duplicateGraphs1++;
				if (output) {
					duplicateGraphColl1.add(g);
				}
			}
		}

		System.out.println("#--- START " + args[1] + " - " + args[0] + " ---");
		ght = new GraphHashTable();
		ght2 = new GraphHashTable();
		for (final Graph g : graphs2) {
			ght.insertFirstTime(g);
		}
		for (final Graph g : graphs1) {

			if (ght.insertFirstTime(g)) {
				System.out.println("unbekannter Graph in " + args[0] + " "
						+ GraphHashTable.getHashCode(g));
				System.out.println("  " + g.getID() + ": " + gp.serialize(g));
				// if (min2==null) min2=g2[i];
				// if (g2[i].getNodeCount()<min2.getNodeCount()) min2=g2[i];
				unknownGraphs0++;
				if (output) {
					unknownGraphColl0.add(g);
				}
			}

			Graph old;
			if ((old = ght2.insert(g)) != null) {
				System.out.println("doppelter Graph in " + args[0]);
				System.out.println("  " + old.getID() + ": "
						+ gp.serialize(old));
				System.out.println("  " + g.getID() + ": " + g.getName());
				duplicateGraphs0++;
				if (output) {
					duplicateGraphColl0.add(g);
				}
			}
		}
		if (unknownGraphs0 == 0 && unknownGraphs1 == 0 && duplicateGraphs0 == 0
				&& duplicateGraphs1 == 0) {
			System.out.println("--> equivalent databases");
		} else {
			System.out.println("\n\nSummary:\n--------");
			System.out.println(args[0] + ":");
			System.out.println("unknown graphs:   " + unknownGraphs0);
			System.out.println("duplicate graphs: " + duplicateGraphs0);
			System.out.println("total graphs:     " + graphs1.size());
			System.out.println(args[1] + ":");
			System.out.println("unknown graphs:   " + unknownGraphs1);
			System.out.println("duplicate graphs: " + duplicateGraphs1);
			System.out.println("total graphs:     " + graphs2.size());
		}

		if (output) {
			System.out.println("#--- SERIALIZING ---");
			out
					.write(((unknownGraphs0 + unknownGraphs1 + duplicateGraphs0 + duplicateGraphs1) + "\n")
							.getBytes());
			for (int i = 0; i < unknownGraphColl0.size(); i++) {
				out.write(serializer.serialize(unknownGraphColl0.get(i))
						.getBytes());
				out.write(("#\n").getBytes());
			}
			for (int i = 0; i < unknownGraphColl1.size(); i++) {
				out.write(serializer.serialize(unknownGraphColl1.get(i))
						.getBytes());
				out.write(("#\n").getBytes());
			}
			for (int i = 0; i < duplicateGraphColl0.size(); i++) {
				out.write(serializer.serialize(duplicateGraphColl0.get(i))
						.getBytes());
				out.write(("#\n").getBytes());
			}
			for (int i = 0; i < duplicateGraphColl1.size(); i++) {
				out.write(serializer.serialize(duplicateGraphColl1.get(i))
						.getBytes());
				out.write(("#\n").getBytes());
			}
			out.close();
		}

		System.out.println("#--- DONE ---");

	}

	private static Collection<Graph> parseFile(final String filename)
			throws FileNotFoundException, IOException, ParseException {
		InputStream in = new FileInputStream(filename);
		if (filename.endsWith(".gz")) {
			in = new GZIPInputStream(in);
		}
		if (filename.endsWith(".edg") || filename.endsWith(".edg.gz")) {
			gp = new SimpleDirectedGraphParser<String, String>(
					new StringLabelParser(), new StringLabelParser());
			return gp.parse(in, new ListGraph.Factory(gp.getNodeParser(), gp
					.getEdgeParser()));
		}
		if (filename.endsWith(".sdg") || filename.endsWith(".sdg.gz")) {
			gp = SimpleDirectedGraphParser.instance();
			return gp.parse(in, new ListGraph.Factory(gp.getNodeParser(), gp
					.getEdgeParser()));
		}
		if (filename.endsWith(".sug") || filename.endsWith(".sug.gz")) {
			gp = SimpleUndirectedGraphParser.instance();
			return gp.parse(in, new ListGraph.Factory(gp.getNodeParser(), gp
					.getEdgeParser()));
		}
		if (filename.endsWith(".dot") || filename.endsWith(".dot.gz")) {
			gp = DotGraphParser.instance();
			return gp.parse(in, new ListGraph.Factory(gp.getNodeParser(), gp
					.getEdgeParser()));
		}
		return new HashSet<Graph>();
	}

}
