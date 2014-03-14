/**
 * Created Jan 03, 2008
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

import de.parsemis.graph.HPGraph;
import de.parsemis.miner.chain.Extension;
import de.parsemis.miner.chain.ExtensionSet;
import de.parsemis.miner.environment.LocalEnvironment;
import de.parsemis.miner.general.HPEmbedding;

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
public class GastonPath<NodeType, EdgeType> extends
		GastonNode<NodeType, EdgeType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	private static final Collection empty = new ArrayList();

	final static int UNKNOWN = -2;

	int frontNode, backNode;

	int frontSym = UNKNOWN, backSym = UNKNOWN, totalSym = UNKNOWN;

	private GastonPath(final int level, final Leg<NodeType, EdgeType> leg,
			final Collection<Leg<NodeType, EdgeType>> siblings,
			final int frontNode, final int backNode, final int ts,
			final int fs, final int bs, final int threadIdx) {
		super(level, leg, siblings, threadIdx);
		this.frontNode = frontNode;
		this.backNode = backNode;
		this.frontSym = fs;
		this.backSym = bs;
		this.totalSym = ts;
	}

	public GastonPath(final Leg<NodeType, EdgeType> leg,
			final Collection<Leg<NodeType, EdgeType>> siblings,
			final int threadIdx) {
		super(0, leg, siblings, threadIdx);
		frontNode = backNode = leg.frag.correspondingNode;
		frontSym = backSym = totalSym = 0;
	}

	/**
	 * calculates (if necessary) the backSymmetry for the given path
	 * 
	 * @return the backSymmetry
	 */
	private int backSymmetry() {
		if (backSym == UNKNOWN) {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(this);
			final HPGraph<NodeType, EdgeType> path = me.frag.subgraph;
			int tmp, c;
			int fn = frontNode, fe = path.getNodeEdge(fn, 0);
			int bn = backNode, be = path.getNodeEdge(bn, 0);
			// step front node
			fn = path.getOtherNode(fe, fn);
			c = 0;
			while ((tmp = path.getNodeEdge(fn, c++)) == fe) {
				// step
			}
			fe = tmp;
			do {
				c = path.getNodeLabelIndex(bn, env)
						- path.getNodeLabelIndex(fn, env);
				if (c > 0) {
					return backSym = 1;
				}
				if (c < 0) {
					return backSym = -1;
				}
				c = path.getEdgeLabelIndex(be, env)
						- path.getEdgeLabelIndex(fe, env);
				if (c > 0) {
					return backSym = 1;
				}
				if (c < 0) {
					return backSym = -1;
				}
				// step nodes
				fn = path.getOtherNode(fe, fn);
				if (fn == bn) {
					return backSym = 0;
				}
				bn = path.getOtherNode(be, bn);
				if (fn == bn) {
					return backSym = 0;
				}
				// step edges
				c = 0;
				while ((tmp = path.getNodeEdge(fn, c++)) == fe) {
					// step
				}
				fe = tmp;
				c = 0;
				while ((tmp = path.getNodeEdge(bn, c++)) == be) {
					// step
				}
				be = tmp;
			} while (backSym == UNKNOWN);
		}
		return backSym;
	}

	@Override
	public GastonNode<NodeType, EdgeType> extend(
			final Extension<NodeType, EdgeType> ext) {
		final ExtensionSet.Ext<NodeType, EdgeType, Leg<NodeType, EdgeType>> e = (ExtensionSet.Ext<NodeType, EdgeType, Leg<NodeType, EdgeType>>) ext;
		final Leg<NodeType, EdgeType> leg = e.getVal();
		final Collection<Leg<NodeType, EdgeType>> extensions = e.getSiblings();
		if (leg.ref.isCycleRefinement()) {
			final int nodeA = leg.getNodeA();
			final int nodeB = leg.getNodeB();
			// check for ring
			if (((nodeA == frontNode) && (nodeB == backNode))
					|| ((nodeB == frontNode) && (nodeA == backNode))) {

				// no extension is required, so siblings are irrelevant
				@SuppressWarnings("unchecked")
				final Collection<Leg<NodeType, EdgeType>> myEmpty = empty;
				return new GastonCycle<NodeType, EdgeType>(this.getLevel() + 1,
						leg, myEmpty, getThreadNumber());
			}
		} else if (leg.getNodeA() == frontNode) {

			final int nodeLabel = leg.ref.getToLabel();
			final int edgeLabel = leg.ref.getEdgeLabel();
			final HPGraph<NodeType, EdgeType> path = me.frag.subgraph;
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(this);
			if (frontNode == backNode) { // adding first edge
				if (nodeLabel >= path.getNodeLabelIndex(backNode, env)) {
					final int backNodeLabel = path.getNodeLabelIndex(backNode,
							env);
					return new GastonPath<NodeType, EdgeType>(
							this.getLevel() + 1, leg, extensions,
							leg.frag.correspondingNode, backNode,
							backNodeLabel == nodeLabel ? 0 : -1, 0, 0,
							getThreadNumber());
				}
			} else { // enlarge front

				final int backNodeLabel = path.getNodeLabelIndex(backNode, env);
				final int backEdgeLabel = path.getEdgeLabelIndex(path
						.getNodeEdge(backNode, 0), env);
				if ((nodeLabel > backNodeLabel)
						|| ((nodeLabel == backNodeLabel) && (edgeLabel > backEdgeLabel))
						|| ((nodeLabel == backNodeLabel)
								&& (edgeLabel == backEdgeLabel) && (frontSymmetry() <= 0))) {
					final boolean sym = ((nodeLabel == backNodeLabel)
							&& (edgeLabel == backEdgeLabel) && (frontSymmetry()) == 0);
					return new GastonPath<NodeType, EdgeType>(
							this.getLevel() + 1, leg, extensions,
							leg.frag.correspondingNode, backNode, sym ? 0 : -1,
							UNKNOWN, totalSym, getThreadNumber());
				}
			}

		} else if (leg.getNodeA() == backNode) {
			if (totalSym != 0) { // enlarge back

				final int nodeLabel = leg.ref.getToLabel();
				final int edgeLabel = leg.ref.getEdgeLabel();
				final HPGraph<NodeType, EdgeType> path = me.frag.subgraph;
				final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
						.env(this);
				final int frontNodeLabel = path.getNodeLabelIndex(frontNode,
						env);
				final int frontEdgeLabel = path.getEdgeLabelIndex(path
						.getNodeEdge(frontNode, 0), env);
				if ((nodeLabel > frontNodeLabel)
						|| ((nodeLabel == frontNodeLabel) && (edgeLabel > frontEdgeLabel))
						|| ((nodeLabel == frontNodeLabel)
								&& (edgeLabel == frontEdgeLabel) && (backSymmetry() >= 0))) {
					final boolean sym = ((nodeLabel == frontNodeLabel)
							&& (edgeLabel == frontEdgeLabel) && (backSymmetry() == 0));
					return new GastonPath<NodeType, EdgeType>(
							this.getLevel() + 1, leg, extensions, frontNode,
							leg.frag.correspondingNode, sym ? 0 : 1, totalSym,
							UNKNOWN, getThreadNumber());
				}
			}
		} else { // tree

			final GastonTree<NodeType, EdgeType> t = GastonTree.create(this,
					leg, extensions);

			return t;
		}

		// otherwise: this extension is not allow
		return null;
	}

	/**
	 * calculates (if necessary) the frontSymmetry for the given path
	 * 
	 * @return the frontSymmetry
	 */
	private int frontSymmetry() {
		if (frontSym == UNKNOWN) {
			final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
					.env(this);
			final HPGraph<NodeType, EdgeType> path = me.frag.subgraph;
			int tmp, c;
			int fn = frontNode, fe = path.getNodeEdge(fn, 0);
			int bn = backNode, be = path.getNodeEdge(bn, 0);
			// step backnode
			bn = path.getOtherNode(be, bn);
			c = 0;
			while ((tmp = path.getNodeEdge(bn, c++)) == be) {
				// step
			}
			be = tmp;
			do {
				c = path.getNodeLabelIndex(bn, env)
						- path.getNodeLabelIndex(fn, env);
				if (c > 0) {
					return frontSym = 1;
				}
				if (c < 0) {
					return frontSym = -1;
				}
				c = path.getEdgeLabelIndex(be, env)
						- path.getEdgeLabelIndex(fe, env);
				if (c > 0) {
					return frontSym = 1;
				}
				if (c < 0) {
					return frontSym = -1;
				}
				// step nodes
				fn = path.getOtherNode(fe, fn);
				if (fn == bn) {
					return frontSym = 0;
				}
				bn = path.getOtherNode(be, bn);
				if (fn == bn) {
					return frontSym = 0;
				}
				// step edges
				c = 0;
				while ((tmp = path.getNodeEdge(fn, c++)) == fe) {
					// step
				}
				fe = tmp;
				c = 0;
				while ((tmp = path.getNodeEdge(bn, c++)) == be) {
					// step
				}
				be = tmp;
			} while (frontSym == UNKNOWN);

		}
		return frontSym;
	}

	@Override
	public Collection<Extension<NodeType, EdgeType>> getExtensions() {

		final LocalEnvironment<NodeType, EdgeType> env = LocalEnvironment
				.env(this);
		final GastonEnvironment<NodeType, EdgeType> tenv = (GastonEnvironment<NodeType, EdgeType>) env
				.getThreadEnv(threadIdx);
		final HPGraph<NodeType, EdgeType> subGraph = toHPFragment().toHPGraph();

		final ExtensionSet<NodeType, EdgeType, Leg<NodeType, EdgeType>> newLegs = new ExtensionSet<NodeType, EdgeType, Leg<NodeType, EdgeType>>();
		final int fromNode = me.frag.correspondingNode;

		assert tenv.check(threadIdx): "URGS! " + Thread.currentThread() + " tenv:"
					+ tenv.threadIdx + " shall:" + threadIdx;

		if (me.getNodeA() == HPGraph.NO_NODE) {
			// initial single noded path, so just new node extensions are
			// possible
			for (final HPEmbedding<NodeType, EdgeType> emb : me.frag) {
				final int superNode = ((GastonEmbedding<NodeType, EdgeType>) emb)
						.getSuperNode();
				final HPGraph<NodeType, EdgeType> g = emb.getSuperGraph();

				for (int i = 0; i < g.getDegree(superNode); i++) {
					final int edge = g.getNodeEdge(superNode, i);
					final int oNode = g.getOtherNode(edge, superNode);
					final int nli = g.getNodeLabelIndex(oNode, env);
					final int eli = g.getEdgeLabelIndex(edge, env);
					if (nli >= 0 && eli >= 0) {
						final Leg<NodeType, EdgeType> n = tenv.getNode(
								fromNode, eli, nli, me.frag.subgraph);
						n.frag.add(tenv.createEmbedding(emb, oNode));
					}
				}
			}
		} else {
			for (final Leg<NodeType, EdgeType> s : siblings) {
				final int d = subGraph.getDegree(s.ref.getNodeA());
				if (!env.findPathsOnly || d <= 1) {
					final Leg<NodeType, EdgeType> l = s.join(me, tenv);
					newLegs.add(l);
				}
			}

			for (final HPEmbedding<NodeType, EdgeType> emb : me.frag) {
				final int superNode = ((GastonEmbedding<NodeType, EdgeType>) emb)
						.getSuperNode();
				final HPGraph<NodeType, EdgeType> g = emb.getSuperGraph();
				final int d = g.getDegree(superNode);

				for (int i = 0; i < d; i++) {
					final int edge = g.getNodeEdge(superNode, i);
					final int oNode = g.getOtherNode(edge, superNode);
					final int cNode = emb.getSubGraphNode(oNode);
					final int nli = g.getNodeLabelIndex(oNode, env);
					final int eli = g.getEdgeLabelIndex(edge, env);
					if (nli >= 0 && eli >= 0) {
						if (cNode == HPGraph.NO_NODE) {
							final Leg<NodeType, EdgeType> n = tenv.getNode(
									fromNode, eli, nli, me.frag.subgraph);
							n.frag.add(tenv.createEmbedding(emb, oNode));
						} else if (tenv.doCycles
								&& subGraph.getEdge(fromNode, cNode) == HPGraph.NO_EDGE) {
							final Leg<NodeType, EdgeType> n = tenv.getCycle(
									fromNode, eli, cNode, me.frag.subgraph);
							n.frag.add(tenv.createEmbedding(emb,
									HPGraph.NO_NODE));
						}
					}
				}
			}
		}
		return tenv.clearAndAddExtensions(newLegs);
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
