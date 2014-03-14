/**
 * created May 2, 2006
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
package de.parsemis;

import static de.parsemis.miner.environment.Debug.INFO;
import static de.parsemis.miner.environment.Debug.QUIET;
import static de.parsemis.miner.environment.Debug.VERBOSE;
import static de.parsemis.miner.environment.Debug.err;
import static de.parsemis.miner.environment.Debug.out;

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
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.environment.Settings;
import de.parsemis.miner.environment.Statistics;
import de.parsemis.miner.filter.FragmentFilter;
import de.parsemis.miner.general.Fragment;

/**
 * This class is the user friendly shell around the ParSeMiS algorithms.
 * 
 * You can read in databases and search for frequent discriminative fragments
 * within it.
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
public final class Miner {

	/**
	 * to start a search from console
	 * 
	 * @param args
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static void main(final String[] args) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		run(args);
	}

	/**
	 * starts a separate thread that regularly checks the memory consumption of
	 * the whole program
	 * 
	 * @param stats
	 * @return the thread
	 */
	private static final Thread memoryCheck(final Statistics stats) {
		return new Thread() {
			{
				this.setDaemon(true);
			}

			@Override
			public void run() {
				while (!isInterrupted()) {
					if (!QUIET) {
						out.print("Getting maximal heap size...");
					}
					System.gc();
					stats.maximumHeapSize = Math
							.max(stats.maximumHeapSize, (int) ((Runtime
									.getRuntime().totalMemory() - Runtime
									.getRuntime().freeMemory()) >> 10));
					if (!QUIET) {
						out.println(stats.maximumHeapSize + "kB");
					}

					try {
						sleep(10);
					} catch (final java.lang.InterruptedException e) {
						err.print(e);
					}
				}
			}
		};
	}

	/**
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 * @param graphs
	 * @param settings
	 * @return a collection of the found frequent fragments
	 */
	public static <NodeType, EdgeType> Collection<Fragment<NodeType, EdgeType>> mine(
			final Collection<Graph<NodeType, EdgeType>> graphs,
			final Settings<NodeType, EdgeType> settings) {


		final Statistics stats = settings.stats;

		// start memoryCheck, if necessary
		Thread t = null;
		if (settings.memoryStatistics) {
			t = memoryCheck(stats);
			t.start();
		}
		// search fragments
		if (INFO) {
			stats.searchTime -= System.currentTimeMillis();
			stats.searchTime2 -= LocalEnvironment.currentCPUMillis();
		}

		final Collection<Fragment<NodeType, EdgeType>> expectedFragments = settings.algorithm
				.initialize(graphs, settings.factory, settings);

		Collection<Fragment<NodeType, EdgeType>> ret = settings.strategy
				.search(settings.algorithm);

		ret.addAll(expectedFragments);

		if (INFO) {
			stats.searchTime += System.currentTimeMillis();
			stats.searchTime2 += LocalEnvironment.currentCPUMillis();
		}
		// filter fragments, if necessary
		final FragmentFilter<NodeType, EdgeType> filter = LocalEnvironment
				.env(settings.strategy).filter;
		if (filter != null) {
			if (INFO) {
				stats.filteringTime -= System.currentTimeMillis();
			}
			ret = filter.filter(ret);
			if (INFO) {
				stats.filteringTime += System.currentTimeMillis();
			}
		}

		// stop memoryCheck, if necessary
		if (t != null) {
			t.interrupt();
		}

		return ret;
	}

	public static final <NodeType, EdgeType> Collection<Graph<NodeType, EdgeType>> parseInput(
			final Settings<NodeType, EdgeType> settings) {
		InputStream in = null;
		if (settings.inputFileName != null) {
			if (settings.inputFileName.equals("-")) {
				in = System.in;
			} else {
				try {
					in = new FileInputStream(settings.inputFileName);
					if (settings.inputFileName.endsWith(".gz")) {
						in = new GZIPInputStream(in);
					}
				} catch (final FileNotFoundException ex) {
					err.println(ex);
				} catch (final IOException ex) {
					err.println(ex);
				}
			}
		}
		if (in == null) {
			err.println("No database is given!");
			System.exit(-1);
		}
		try {
			final Collection<Graph<NodeType, EdgeType>> ret = settings.parser
					.parse(in, settings.factory);
			if (settings.minFreq == null) {
				for (final Graph<NodeType, EdgeType> graph : ret) {
					if (settings.minFreq == null) {
						settings.minFreq = settings.getFrequency(graph);
					} else {
						settings.minFreq.add(settings.getFrequency(graph));
					}
				}
				settings.minFreq.smul(settings.minProzent);
			}
			if (settings.maxFreq == null && settings.maxProzent >= 0) {
				for (final Graph<NodeType, EdgeType> graph : ret) {
					if (settings.maxFreq == null) {
						settings.maxFreq = settings.getFrequency(graph);
					} else {
						settings.maxFreq.add(settings.getFrequency(graph));
					}
				}
				settings.maxFreq.smul(settings.maxProzent);
			}
			if (VERBOSE) {
				out.println("searching on "
						+ ret.size()
						+ " graphs with frequency "
						+ settings.minFreq
						+ (settings.maxFreq != null ? " (maximum "
								+ settings.maxFreq + ")" : ""));
			}
			return ret;
		} catch (final ParseException pe) {
			pe.printStackTrace();
			err.println(pe);
			System.exit(-1);
		} catch (final IOException io) {
			err.println(io);
			System.exit(-1);
		}
		return null; // shall never be reached
	}

	/**
	 * prints the given fragments to the configured output
	 * 
	 * @param <NodeType>
	 *            the type of the node labels (will be hashed and checked with
	 *            .equals(..))
	 * @param <EdgeType>
	 *            the type of the edge labels (will be hashed and checked with
	 *            .equals(..))
	 * @param fragments
	 * @param settings
	 */
	public static final <NodeType, EdgeType> void printOutput(
			final Collection<Fragment<NodeType, EdgeType>> fragments,
			final Settings<NodeType, EdgeType> settings) {
		OutputStream out = null;
		if (settings.outputFileName != null) {
			if (settings.outputFileName.equals("-")) {
				out = System.out;
			} else {
				try {
					out = new FileOutputStream(settings.outputFileName);
					if (settings.outputFileName.endsWith(".gz")) {
						out = new GZIPOutputStream(out);
					}
				} catch (final FileNotFoundException ex) {
					err.println(ex);
				} catch (final IOException ex) {
					err.println(ex);
				}
			}
		}
		if (out != null) {
			try {
				settings.serializer.serializeFragments(out, fragments);
				out.close();
			} catch (final IOException io) {
				err.println(io);
			}
		}
	}

	private static <NodeType, EdgeType> void run(
			final Settings<NodeType, EdgeType> settings) {

		final Statistics stats = settings.stats;
		if (!QUIET) {
			stats.completeTime -= System.currentTimeMillis();
			stats.completeTime2 -= LocalEnvironment.currentCPUMillis();
		}
		// parse graphs
		if (INFO) {
			stats.parseTime -= System.currentTimeMillis();
		}

		final Collection<Graph<NodeType, EdgeType>> graphs = parseInput(settings);
		if (INFO) {
			stats.parseTime += System.currentTimeMillis();
		}
		// mine them
		final Collection<Fragment<NodeType, EdgeType>> fragments = mine(graphs,
				settings);
		// write results
		if (INFO) {
			stats.serializeTime -= System.currentTimeMillis();
		}
		printOutput(fragments, settings);
		if (INFO) {
			stats.serializeTime += System.currentTimeMillis();
		}
		if (!QUIET) {
			stats.completeTime += System.currentTimeMillis();
			stats.completeTime2 += LocalEnvironment.currentCPUMillis();
		}

		if (INFO) {
			stats.printTo(out);
		}

		if (!QUIET) {
			if (settings.storeEmbeddings) {
				int numEmbeddings = 0;
				int maxNodes = 0;
				int maxEdges = 0;
				int cyclic = 0;
				for (final Fragment<NodeType, EdgeType> actFrag : fragments) {
					numEmbeddings += actFrag.size();
					final Graph<NodeType, EdgeType> g = actFrag.toGraph();
					if (g.getNodeCount() > maxNodes) {
						maxNodes = actFrag.toGraph().getNodeCount();
					}
					if (g.getEdgeCount() > maxEdges) {
						maxEdges = actFrag.toGraph().getEdgeCount();
					}
					if (g.getNodeCount() - 1 < g.getEdgeCount()) {
						cyclic++;
					}
				}
				out.println("Complete run took "
						+ (settings.stats.completeTime / 1000.0) + " seconds; "
						+ "found " + fragments.size() + " fragments and "
						+ numEmbeddings + " embeddings.");
				out.println("The biggest Fragment has " + maxNodes
						+ " nodes and " + maxEdges + " edge.");
				out.println(cyclic + " cyclic Fragments are found.");
			} else {
				out.println("Complete run took "
						+ (settings.stats.completeTime / 1000.0) + " seconds; "
						+ "found " + fragments.size() + " fragments");
			}
		}

	}

	/**
	 * starts a search with the given parameters
	 * 
	 * @param args
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public final static void run(final String[] args)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		final Settings settings = Settings.parse(args);
		if (settings == null) {
			Settings.printUsage(err);
		} else {
			if (INFO) {
				out.println("running: ");
				for (int i = 0; i < args.length; i++) {
					System.err.println("args[" + i + "]=" + args[i]);
				}
			}
			run(settings);
		}
	}
}
