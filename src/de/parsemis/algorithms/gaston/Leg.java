/**
 * Created Jan 04, 2008
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 * Copyright 2008 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.algorithms.gaston;

import de.parsemis.graph.Edge;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.Frequency;
import de.parsemis.utils.FrequentedComparable;
import de.parsemis.utils.Generic;

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
public class Leg<NodeType, EdgeType> implements Generic<NodeType, EdgeType>,
		FrequentedComparable<Leg<NodeType, EdgeType>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final protected Refinement ref;

	final protected GastonFragment<NodeType, EdgeType> frag;

	public Leg(final Refinement ref,
			final GastonFragment<NodeType, EdgeType> frag) {
		this.ref = ref;
		this.frag = frag;
	}

	/**
	 * creates a leg for the given refinement
	 * 
	 * @param ref
	 * @param sub
	 * @param correspondingNode
	 */
	public Leg(final Refinement ref, final HPGraph<NodeType, EdgeType> sub,
			final int correspondingNode) {
		this.ref = ref;
		this.frag = new GastonFragment<NodeType, EdgeType>(sub,
				correspondingNode);
	}

	public int compareTo(final Leg<NodeType, EdgeType> o) {
		return ref.compareTo(o.ref);
	}

	private Leg<NodeType, EdgeType> extend(final Leg<NodeType, EdgeType> l) {
		final Leg<NodeType, EdgeType> o = l;
		final Leg<NodeType, EdgeType> s = this;
		if (s.ref.isCycleRefinement()) {
			final HPMutableGraph<NodeType, EdgeType> next = (HPMutableGraph<NodeType, EdgeType>) o.frag.subgraph
					.clone();
			next.addEdgeIndex(s.getNodeA(), s.getNodeB(), s.getEdgeLabel(),
					Edge.UNDIRECTED);
			return new Leg<NodeType, EdgeType>(s.ref.clone(HPGraph.NO_NODE),
					new GastonFragment<NodeType, EdgeType>(next,
							HPGraph.NO_NODE));
		} else {
			final HPMutableGraph<NodeType, EdgeType> next = (HPMutableGraph<NodeType, EdgeType>) o.frag.subgraph
					.clone();
			final int nextNode = next.addNodeAndEdgeIndex(s.getNodeA(), s
					.getToLabel(), s.getEdgeLabel(), Edge.UNDIRECTED);
			return new Leg<NodeType, EdgeType>(s.ref.clone(nextNode),
					new GastonFragment<NodeType, EdgeType>(next, nextNode));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.utils.Frequented#frequency()
	 */
	public Frequency frequency() {
		return frag.frequency();
	}

	private final GastonEmbedding<NodeType, EdgeType> get(final int index) {
		return (GastonEmbedding<NodeType, EdgeType>) frag.get(index);
	}

	public int getDepth() {
		return ref.getDepth();
	}

	public EdgeType getEdgeLabel() {
		return LocalEnvironment.env(this).getEdgeLabel(ref.getEdgeLabel());
	}

	public int getNodeA() {
		return ref.getNodeA();
	}

	public int getNodeB() {
		return ref.getNodeB();
	}

	private NodeType getToLabel() {
		return LocalEnvironment.env(this).getNodeLabel(ref.getToLabel());
	}

	/**
	 * joins this leg with the given
	 * 
	 * @param leg1
	 * @param tenv
	 * @return the new leg, resulting from the join
	 */
	public Leg<NodeType, EdgeType> join(final Leg<NodeType, EdgeType> leg1,
			final GastonEnvironment<NodeType, EdgeType> tenv) {
		/*
		 * the optimized joining algorithm of the original implementation
		 * (invariant test are separated)
		 */
		final Leg<NodeType, EdgeType> leg2 = this;
		final Leg<NodeType, EdgeType> newLeg = extend(leg1);

		final int end1 = leg1.frag.size();
		final int end2 = this.frag.size();
		int a1 = 0;
		int a2 = 0;

		// merge all embeddings of leg1 with the embeddings of leg2 that has the
		// same parent embeddings

		if (ref.isCycleRefinement() && (this != leg1)) {

			while (a1 < end1 && a2 < end2) {
				final int id2 = leg2.get(a2).getParentId();
				// skip embeddings with a lesser parent id than the one of leg2
				while (a1 < end1 && leg1.get(a1).getParentId() < id2) {
					a1++;
				}
				if (a1 < end1) {
					final int id1 = leg1.get(a1).getParentId();
					// skip embeddings with a lesser parent id than the one of
					// leg1
					while (a2 < end2 && leg2.get(a2).getParentId() < id1) {
						a2++;
					}

					final int m2 = a2;
					{
						final GastonEmbedding<NodeType, EdgeType> e1 = leg1
								.get(a1);
						while (a2 < end2) {
							// merge embedding a1 with all embeddings from leg2
							// with the same parent id as a1 (if available)
							final GastonEmbedding<NodeType, EdgeType> e2 = leg2
									.get(a2);
							final int id3 = e2.getParentId();
							if (id3 != id1) {
								break;
							}
							newLeg.frag.add(tenv.createEmbedding(e1, e2
									.getSuperNode()));
							a2++;
						}
					}
					// there are equals parents, than check the next ones in
					// leg1
					if (a2 != m2) {
						for (a1++; a1 < end1
								&& leg1.get(a1).getParentId() == id1; a1++) {
							final GastonEmbedding<NodeType, EdgeType> e1 = leg1
									.get(a1);
							for (int e = m2; e < a2; e++) {
								final GastonEmbedding<NodeType, EdgeType> e2 = leg2
										.get(e);
								newLeg.frag.add(tenv.createEmbedding(e1, e2
										.getSuperNode()));
							}
						}
					}
				}
			} // do until end of one leg
		} else {
			// same as above, but just merge embeddings with different nodes
			while (a1 < end1 && a2 < end2) {
				final int id2 = leg2.get(a2).getParentId();
				// skip embeddings with a lesser parent id than the one of leg2
				while (a1 < end1 && leg1.get(a1).getParentId() < id2) {
					a1++;
				}
				if (a1 < end1) {
					final int id1 = leg1.get(a1).getParentId();
					// skip embeddings with a lesser parent id than the one of
					// leg1
					while (a2 < end2 && leg2.get(a2).getParentId() < id1) {
						a2++;
					}

					final int m2 = a2;
					{
						final GastonEmbedding<NodeType, EdgeType> e1 = leg1
								.get(a1);
						final int node1 = e1.getSuperNode();
						while (a2 < end2) {
							// merge embedding a1 with all embeddings from leg2
							// with the same parent id as a1 (if available)
							final GastonEmbedding<NodeType, EdgeType> e2 = leg2
									.get(a2);
							final int id3 = e2.getParentId();
							if (id3 != id1) {
								break;
							}
							if (node1 != e2.getSuperNode()) {
								newLeg.frag.add(tenv.createEmbedding(e1, e2
										.getSuperNode()));
							}
							a2++;
						}
					}
					// there are equals parents, than check the next ones in
					// leg1
					if (a2 != m2) {
						for (a1++; a1 < end1
								&& leg1.get(a1).getParentId() == id1; a1++) {
							final GastonEmbedding<NodeType, EdgeType> e1 = leg1
									.get(a1);
							final int node1 = e1.getSuperNode();

							for (int e = m2; e < a2; e++) {
								final GastonEmbedding<NodeType, EdgeType> e2 = leg2
										.get(e);
								if (node1 != e2.getSuperNode()) {
									newLeg.frag.add(tenv.createEmbedding(e1, e2
											.getSuperNode()));
								}
							}
						}
					}
				}
			} // do until end of one leg
		}
		return newLeg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ref.toString();
	}

}
