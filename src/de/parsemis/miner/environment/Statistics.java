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
package de.parsemis.miner.environment;

import java.io.PrintStream;
import java.io.Serializable;

import de.parsemis.algorithms.gSpan.GSpanEmbedding;
import de.parsemis.algorithms.gSpan.GSpanHPEmbedding_flat;
import de.parsemis.algorithms.gSpan.GSpanHPEmbedding_hierarchical;
import de.parsemis.algorithms.gSpan.RightMostExtension;

/**
 * This class is to store runtime statistic
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class Statistics implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Stores the overall runtime */
	public long completeTime;

	/** Stores the overall cpu runtime */
	public long completeTime2;

	/** Stores the time required for parsing the database */
	public long parseTime;

	/** Stores the time needed by the final filter */
	public long filteringTime;

	/** Stores the time required to search all frequent fragments */
	public long searchTime;

	/** Stores the cpu time required to search all frequent fragments */
	public long searchTime2;

	/** Stores the time of the distributed part of a distributed search */
	public long distributedTime;

	/** Stores the time for checking the Graph frequencies */
	public long checkTime;

	/** Stores the time for serialize the results */
	public long serializeTime;

	/** Stores the time used for spliting the worker stacks */
	public long splitTime;

	/** Stores the time used for syncing */
	public long[] syncTime = new long[10];

	/**
	 * Stores the maximal amount of heap that was requiered (just valid for
	 * --memoryStatistics=true)
	 */
	public int maximumHeapSize = -1;

	public int duplicateFragments = 0;

	public int unconnectedFragments = 0;

	public int newRoot, newNode, newLevel, newEdge;

	/**
	 * prints the gathered information
	 * 
	 * @param out
	 *            the stream used for the output
	 */
	public void printTo(final PrintStream out) {
		out.println("-------------------------------");
		if (parseTime > 0) {
			out.println("    parsing time: " + (parseTime / 1000.0) + "s");
		}
		if (searchTime > 0) {
			out.println("  searching time: " + (searchTime / 1000.0) + "s");
			out.println("search. CPU time: " + (searchTime2 / 1000.0) + "s");
		}
		if (distributedTime > 0) {
			out
					.println("distributed time: " + (distributedTime / 1000.0)
							+ "s");
		}
		if (splitTime > 0) {
			out.println("      split time: " + (splitTime / 1000.0) + "s");
		}
		for (int i = 0; i < syncTime.length; i++) {
			if (syncTime[i] > 0) {
				out.println("      sync" + i + " time: "
						+ (syncTime[i] / 1000.0) + "s");
			}
		}
		if (filteringTime > 0) {
			out.println("  filtering time: " + (filteringTime / 1000.0) + "s");
		}
		if (checkTime > 0) {
			out.println("   checking time: " + (checkTime / 1000.0) + "s");
		}
		if (serializeTime > 0) {
			out.println("  serialize time: " + (serializeTime / 1000.0) + "s");
		}
		if (completeTime > 0) {
			out.println("-------------------------------");
			out.println("   complete time: " + (completeTime / 1000.0) + "s");
			out.println(" compl. CPU time: " + (completeTime2 / 1000.0) + "s");
		}

		/*
		 * if (perfectExtensionPrunedExtensions > 0) out.println("perfect
		 * extension pruned fragments: " + perfectExtensionPrunedExtensions); if
		 * (earlyFilteredNonClosedFragments > 0) out.println("Early filtered
		 * non-closed fragments: " + earlyFilteredNonClosedFragments); if
		 * (duplicateFragments > 0) out.println("Duplicate fragments: " +
		 * duplicateFragments); if (movedFragments > 0) out.println("nodes
		 * moved: "+movedFragments);
		 */
		if (duplicateFragments > 0) {
			out.println("Duplicate fragments: " + duplicateFragments);
		}
		if (unconnectedFragments > 0) {
			out.println("Unconnected fragments: " + unconnectedFragments);
		}
		if (maximumHeapSize >= 0) {
			out.println("Maximum heap size: " + maximumHeapSize + "kB");
		}
		if (GSpanEmbedding.counter != 0) {
			out.println("created embeddings: " + GSpanEmbedding.counter);
		}
		if (GSpanHPEmbedding_flat.counter != 0) {
			out.println("created hpembeddings_impl: "
					+ GSpanHPEmbedding_flat.counter);
		}
		if (GSpanHPEmbedding_hierarchical.counter != 0) {
			out.println("created hpembeddings_hier: "
					+ GSpanHPEmbedding_hierarchical.counter);
		}
		if (RightMostExtension.counter != 0) {
			out.println("RightMostExtensions: " + RightMostExtension.counter);
		}
		if (newRoot > 0) {
			out.println("newRoot: " + newRoot);
		}
		if (newNode > 0) {
			out.println("newNode: " + newNode);
		}
		if (newEdge > 0) {
			out.println("newEdge: " + newEdge);
		}
		if (newLevel > 0) {
			out.println("newLevel: " + newLevel);
		}

		out.println("-------------------------------");

	}

	/** clears the stored values */
	public void reset() {
		this.maximumHeapSize = -1;
		this.completeTime = 0;
		this.parseTime = 0;
		this.filteringTime = 0;
		this.searchTime = 0;
		this.distributedTime = 0;
		this.checkTime = 0;
		this.serializeTime = 0;
		this.duplicateFragments = 0;
		this.unconnectedFragments = 0;
	}

}
