/**
 * Created Jan 05, 2008
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
package de.parsemis.algorithms.gaston;

import java.util.ArrayList;
import java.util.Collection;

import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.ExtensionSet;
import de.parsemis.miner.environment.LocalEnvironment;

/**
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class GastonCycle<NodeType, EdgeType> extends
		GastonNode<NodeType, EdgeType> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static <NodeType, EdgeType> GastonCycle<NodeType, EdgeType> create(
			final GastonTree<NodeType, EdgeType> tree,
			final Leg<NodeType, EdgeType> leg,
			final Collection<Leg<NodeType, EdgeType>> siblings) {

		final Collection<Leg<NodeType, EdgeType>> newSiblings = new ArrayList<Leg<NodeType, EdgeType>>();
		for (final Leg<NodeType, EdgeType> l : siblings) {
			if (l.ref.isCycleRefinement() && leg.compareTo(l) >= 0) {
				newSiblings.add(l);
			}
		}

		return new GastonCycle<NodeType, EdgeType>(tree.getLevel() + 1, leg,
				newSiblings, tree.getThreadNumber());
	}

	public GastonCycle(final int level, final Leg<NodeType, EdgeType> leg,
			final Collection<Leg<NodeType, EdgeType>> siblings,
			final int threadIdx) {
		super(level, leg, siblings, threadIdx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonNode#extend(de.parsemis.algorithms.gaston.Leg,
	 *      java.util.Collection)
	 */
	@Override
	public GastonNode<NodeType, EdgeType> extend(
			final Extension<NodeType, EdgeType> ext) {
		final ExtensionSet.Ext<NodeType, EdgeType, Leg<NodeType, EdgeType>> e = (ExtensionSet.Ext<NodeType, EdgeType, Leg<NodeType, EdgeType>>) ext;
		final Leg<NodeType, EdgeType> leg = e.getVal();
		final Collection<Leg<NodeType, EdgeType>> extensions = e.getSiblings();

		return new GastonCycle<NodeType, EdgeType>(getLevel() + 1, leg,
				extensions, getThreadNumber());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.algorithms.gaston.GastonNode#getExtensions()
	 */
	@Override
	public Collection<Extension<NodeType, EdgeType>> getExtensions() {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(this);
			final GastonEnvironment<NodeType, EdgeType> tenv = (GastonEnvironment<NodeType, EdgeType>) env
					.getThreadEnv(threadIdx);
			// rings have no extension
			final ExtensionSet<NodeType, EdgeType, Leg<NodeType, EdgeType>> newLegs = new ExtensionSet<NodeType, EdgeType, Leg<NodeType, EdgeType>>();
			for (final Leg<NodeType, EdgeType> l : siblings) {
				if (me.compareTo(l) > 0) {
					final Leg<NodeType, EdgeType> next = l.join(me, tenv);
					newLegs.add(next);
				}
			}
			return newLegs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return LocalEnvironment.env(this).serializer.serialize(me.frag.subgraph
				.toGraph());
	}
}
