/**
 * created Oct 09, 2007
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
package de.parsemis.utils;

import static de.parsemis.miner.environment.Debug.err;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import de.parsemis.graph.Graph;
import de.parsemis.graph.HPGraph;
import de.parsemis.miner.environment.Settings;
import de.parsemis.parsers.GraphParser;
import de.parsemis.parsers.StringLabelParser;

/**
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
public class GraphInformation<NodeType, EdgeType> {

	public static <NodeType, EdgeType> GraphInformation<NodeType, EdgeType> getInfo(
			final Collection<Graph<NodeType, EdgeType>> g, final String name)
			throws IOException {
		return new GraphInformation<NodeType, EdgeType>(g, name);
	}

	private final Collection<Graph<NodeType, EdgeType>> g;

	private int[] degree;

	private double schnitt;

	private int maxdegree;

	private int mindegree;

	private int c;

	private double gesSchnitt;

	private int localMax;

	private int localMin;

	// private File file;

	private final FileOutputStream out;

	public GraphInformation(final Collection<Graph<NodeType, EdgeType>> g,
			final String name) throws IOException {
		this.g = g;
		schnitt = 0;
		maxdegree = 0;
		mindegree = 255;
		gesSchnitt = 0;
		out = new FileOutputStream(name);
		// file = new File(name);
		// deGrees(out);
		smallGraph(out, name);
	}

	public void deGrees(final FileOutputStream name) throws IOException {
		if (g.isEmpty()) {
			System.out.println("putt");
		} else {
			final StringBuffer buf = new StringBuffer(2048);
			int graphnummer = 1;
			for (final Graph<NodeType, EdgeType> graph : g) {

				localMin = 255;
				localMax = 0;
				final HPGraph<NodeType, EdgeType> ghp = graph.toHPGraph();
				final int cnt = ghp.getNodeCount();
				c = c + cnt;
				degree = new int[cnt];
				final IntIterator it = ghp.nodeIndexIterator();
				while (it.hasNext()) {
					final int i = it.next();
					degree[i] = ghp.getDegree(i);
				}
				buf.append("Graph: " + graphnummer + "\n");

				for (int j = 0; j < cnt; j++) {
					schnitt = schnitt + degree[j];
					if (degree[j] > localMax) {
						localMax = degree[j];
					}
					if (degree[j] < localMin) {
						localMin = degree[j];
					}
				}
				schnitt = schnitt / cnt;
				buf
						.append("maximaler Grad in diesem Graph: " + localMax
								+ "\n");
				buf
						.append("minimaler Grad in diesem Graph: " + localMin
								+ "\n");
				buf.append("durchschnittlicher Grad in diesem Graph: "
						+ schnitt + "\n");

				name.write(buf.toString().getBytes());

				gesSchnitt = gesSchnitt + schnitt;
				if (maxdegree < localMax) {
					maxdegree = localMax;
				}
				if (mindegree > localMin) {
					mindegree = localMin;
				}
				graphnummer++;
			}
			gesSchnitt = gesSchnitt / g.size();
			buf.append("\ndurchschnittlicher Grad in der gesamten Datei: "
					+ gesSchnitt);
			buf.append("\nmaximaler Grad in der gesamten Datei :" + maxdegree);
			buf.append("\nminimaler Grad in der gesamten Datei :" + mindegree);
			name.write(buf.toString().getBytes());
		}
		name.flush();

	}

	public int getMaxdegree() {
		return maxdegree;
	}

	public int getMindegree() {
		return mindegree;
	}

	public double getSchnitt() {
		return gesSchnitt;
	}

	@SuppressWarnings("unchecked")
	public void smallGraph(final FileOutputStream out, final String name)
			throws IOException {

		final GraphParser p2 = Settings.parseFileName(name,
				new StringLabelParser(), new StringLabelParser());
		if (p2 == null) {
			err.println("Unknown output database type!");
			System.exit(-1);
		}

		final int anz = g.size();
		final int cnt = anz / 1990;
		final Collection<Graph> small = new Vector<Graph>();
		int i = 1;
		for (final Graph graph : g) {
			if (i % cnt == 0) {
				small.add(graph);
			}
			i++;
		}

		p2.serialize(out, small);
		out.close();
	}
}
