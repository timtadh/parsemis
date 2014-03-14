/**
 * created May 14, 2007
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

import static de.parsemis.miner.environment.Debug.err;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.parsemis.graph.Graph;
import de.parsemis.graph.ListGraph;
import de.parsemis.miner.environment.Settings;
import de.parsemis.parsers.GraphParser;
import de.parsemis.parsers.StringLabelParser;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class FileConverter {

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args) {
		// TODO Auto-generated method stub
		if ((args.length != 2) || (args[0].equals("--help"))) {
			System.out.println("Usage: " + FileConverter.class.getName()
					+ " inputFile outputFile");
			System.exit(1);
		}
		final String inputFileName = args[0];
		final String outputFileName = args[1];
		final GraphParser p1 = Settings.parseFileName(inputFileName,
				new StringLabelParser(), new StringLabelParser());
		if (p1 == null) {
			err.println("Unknown input database type!");
			System.exit(-1);
		}
		final GraphParser p2 = Settings.parseFileName(outputFileName,
				new StringLabelParser(), new StringLabelParser());
		if (p2 == null) {
			err.println("Unknown output database type!");
			System.exit(-1);
		}
		InputStream in = null;
		if (inputFileName.equals("-")) {
			in = System.in;
		} else {
			try {
				in = new FileInputStream(inputFileName);
				if (inputFileName.endsWith(".gz")) {
					in = new GZIPInputStream(in);
				}
			} catch (final FileNotFoundException ex) {
				err.println(ex);
			} catch (final IOException ex) {
				err.println(ex);
			}
		}
		if (in == null) {
			err.println("No input database is given!");
			System.exit(-1);
		}
		OutputStream out = null;
		if (outputFileName != null) {
			if (outputFileName.equals("-")) {
				out = System.out;
			} else {
				try {
					out = new FileOutputStream(outputFileName);
					if (outputFileName.endsWith(".gz")) {
						out = new GZIPOutputStream(out);
					}
				} catch (final FileNotFoundException ex) {
					err.println(ex);
				} catch (final IOException ex) {
					err.println(ex);
				}
			}
		}
		if (out == null) {
			err.println("No output database is given!");
			System.exit(-1);
		}
		try {
			final Collection<Graph> graphs = p1.parse(in,
					new ListGraph.Factory(p1.getNodeParser(), p1
							.getEdgeParser()));
			if (graphs == null) {
				err.println("Database cannot be loaded!");
				System.exit(-1);
			}
			System.out.println(graphs.size() + " graphen eingelesen");
			p2.serialize(out, graphs);
			out.close();
		} catch (final ParseException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

}
