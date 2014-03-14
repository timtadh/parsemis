/**
 * created Jun 19, 2006
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.HPListGraph;
import de.parsemis.graph.Node;
import de.parsemis.miner.environment.Settings;
import de.parsemis.parsers.LabelParser;
import de.parsemis.parsers.StringLabelParser;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class EdgeRelabler {

	private static InputStream getFileInStream(final String filename)
			throws IOException {
		if (filename == null) {
			return null;
		}
		if (filename.equals("-")) {
			return System.in;
		}
		InputStream in = new FileInputStream(filename);
		if (filename.endsWith(".gz")) {
			in = new GZIPInputStream(in);
		}
		return in;
	}

	private static OutputStream getFileOutStream(final String filename)
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
	public static void main(final String[] args) throws Exception {
		if (args.length == 2) {
			final LabelParser<String> lp = new StringLabelParser();
			// read Database
			final Collection<Graph<String, String>> graphs = Settings
					.parseFileName(args[1], lp, lp).parse(
							getFileInStream(args[0]),
							new HPListGraph.Factory<String, String>(lp, lp));
			// relabel edges
			for (final Graph<String, String> graph : graphs) {
				for (final Iterator<Edge<String, String>> eit = graph
						.edgeIterator(); eit.hasNext();) {
					final Edge<String, String> edge = eit.next();
					final String randLabel = GraphGenerator.randElement(
							new String[] { "0", "1" },
							new double[] { 0.5, 1.0 });
					edge.setLabel(randLabel);
				}
			}
			// relabel Nodes
			for (final Graph<String, String> graph : graphs) {
				for (final Iterator<Node<String, String>> eit = graph
						.nodeIterator(); eit.hasNext();) {
					final Node<String, String> node = eit.next();
					final String randLabel = GraphGenerator.randElement(
							new String[] { "A", "B", "C", "D", "E", "F", "G",
									"H", "I", "J", "K", "L", "M", "N", "O",
									"P", "Q", "R", "S", "T" }, new double[] {
									0.05, 0.1, 0.05, 0.2, 0.25, 0.3, 0.35, 0.4,
									0.45, 0.5, 0.55, 0.6, 0.65, 0.7, 0.75, 0.8,
									0.85, 0.9, 0.95, 1.0 });
					node.setLabel(randLabel);
				}
			}
			// write Database
			Settings.parseFileName(args[0], lp, lp).serialize(
					getFileOutStream(args[1]), graphs);

		} else {
			System.err
					.println("Usage: java -cp ... EdgeRelabler sourceFile destFile");
			System.exit(-1);

		}
	}

}
