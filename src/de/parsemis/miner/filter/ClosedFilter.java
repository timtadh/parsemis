/**
 * created Jul 6, 2006
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
package de.parsemis.miner.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import de.parsemis.graph.SubGraphComparator;
import de.parsemis.miner.general.Fragment;

/**
 * This class provide a post-filter to filter out non-closed fragments
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
public class ClosedFilter<NodeType, EdgeType> implements
		FragmentFilter<NodeType, EdgeType> {

	@SuppressWarnings("unchecked")
	private static Comparator<Fragment> freqSizeComp = new Comparator<Fragment>() {
		public int compare(Fragment arg0, Fragment arg1) {
			final int freq = arg0.frequency().compareTo(arg1.frequency());
			if (freq != 0) {
				return freq;
			}
			final int ns = arg0.toGraph().getNodeCount()
					- arg1.toGraph().getNodeCount();
			if (ns != 0) {
				return ns;
			}
			return arg0.toGraph().getEdgeCount()
					- arg1.toGraph().getEdgeCount();
		}
	};

	/**
	 * a static method to filter out non-closed fragments
	 * 
	 * @param <NodeType>
	 *            the type of the node labels
	 * @param <EdgeType>
	 *            the type of the edge labels
	 * @param frags
	 * @return a set of closed fragments
	 */
	@SuppressWarnings("unchecked")
	public static <NodeType, EdgeType> Collection<Fragment<NodeType, EdgeType>> filterFrag(
			final Collection<Fragment<NodeType, EdgeType>> frags) {
		final Fragment<NodeType, EdgeType>[] f = frags
				.toArray(new Fragment[frags.size()]);
		Arrays.sort(f, freqSizeComp);
		final SubGraphComparator<NodeType, EdgeType> comp = new SubGraphComparator<NodeType, EdgeType>();
		for (int i = f.length - 1; i >= 0; --i) {
			final Fragment<NodeType, EdgeType> fi = f[i];
			if (fi != null) {
				for (int k = i - 1; k >= 0; --k) {
					final Fragment<NodeType, EdgeType> fk = f[k];
					if (fk != null) {
						if (fk.frequency().compareTo(fi.frequency()) != 0) {
							break;
						}
						if (comp.compare(fk.toGraph(), fi.toGraph()) == 0) {
							frags.remove(fk);
							f[k] = null;
						}
					}
				}
			}
		}
		return frags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.general.FragmentFilter#filter(java.util.Collection)
	 */
	public Collection<Fragment<NodeType, EdgeType>> filter(
			final Collection<Fragment<NodeType, EdgeType>> frags) {
		return filterFrag(frags);
	}

}
