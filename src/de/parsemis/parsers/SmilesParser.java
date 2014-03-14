/**
 * created Jun 28, 2006
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
package de.parsemis.parsers;

import static de.parsemis.miner.environment.Debug.WARN;
import static de.parsemis.miner.environment.Debug.err;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.parsemis.chemical.Atom;
import de.parsemis.chemical.Bond;
import de.parsemis.graph.Edge;
import de.parsemis.graph.Graph;
import de.parsemis.graph.GraphFactory;
import de.parsemis.graph.HPGraph;
import de.parsemis.graph.HPMutableGraph;
import de.parsemis.graph.MutableGraph;
import de.parsemis.miner.general.DataBaseGraph;
import de.parsemis.miner.general.Fragment;

/**
 * This class is the parser of the smiles format (.smiles) that is for in
 * chemical databases
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class SmilesParser implements GraphParser<Atom, Bond> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean noH = true;

	private boolean lastWasReference;

	private char[] m;

	private int pos;

	private final BitSet ffv = new BitSet();

	private final BitSet arom = new BitSet();

	private final HashMap<Integer, Integer> marker = new HashMap<Integer, Integer>();

	/* parse the next atom */
	private int atom(final HPMutableGraph<Atom, Bond> g, final int lastNode)
			throws ParseException {
		boolean bracketFound = false;

		char c = nextChar();
		if (c == '[') {
			bracketFound = true;
			c = nextChar();
		}

		if (c == '*') { // keine Ahnung was das ist
			c = nextChar();
		}

		boolean aromaticAtom = false;
		lastWasReference = false;
		Atom label;
		switch (c) {
		// parse references
		case '%':
			c = (char) ((nextChar() - '0') * 10 + nextChar());
			final Integer nodeRef = marker.remove(c - '0');
			if (nodeRef != null) {
				this.lastWasReference = true;
				return nodeRef.intValue();
			} else {
				pos -= 2;
				final char b = m[pos - 2];
				if ((b == '-') || (b == '=') || (b == '#') || (b == ':')
						|| (b == '/') || (b == '\\')
						|| ((b >= '1') && (b <= '9'))) {
					marker.put(c - '0', lastNode);
					return HPGraph.NO_NODE;
				}
				throw new ParseException("Use of undefined ring marker: " + c
						+ " " + b, pos);
			}
		case '1':
			if (peekChar() == '0') {
				++pos;
				c = ('9' + 1); // ring markers up to 10 may be specified
				// without '%'
			}
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			final Integer nodeReference = marker.remove(c - '0');
			if (nodeReference != null) {
				this.lastWasReference = true;
				return nodeReference.intValue();
			} else {
				final char b = m[pos - 2];
				if ((b == '-') || (b == '=') || (b == '#') || (b == ':')
						|| (b == '/') || (b == '\\')
						|| ((b >= '1') && (b <= '9'))) {
					marker.put(c - '0', lastNode);
					// System.out.println("added Marker "+(c-'0')+" "+pos);
					return HPGraph.NO_NODE;
				}
				throw new ParseException("Use of undefined ring marker: " + c
						+ " " + b + " " + pos, pos);
			}
			// parse symbols
		case 'A':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'l':
				label = Atom.Al;
				break;
			case 'r':
				label = Atom.Ar;
				break;
			case 's':
				label = Atom.As;
				break;
			case 'g':
				label = Atom.Ag;
				break;
			case 'u':
				label = Atom.Au;
				break;
			case 't':
				label = Atom.At;
				break;
			case 'c':
				label = Atom.Ac;
				break;
			case 'm':
				label = Atom.Am;
				break;
			default:
				throw new ParseException("Unknown atom symbol: A" + c, pos);
			}
			break;
		case 'b':
			aromaticAtom = true;
		case 'B':
			c = nextChar();
			switch (c) {
			case 'e':
				label = Atom.Be;
				break;
			case 'r':
				label = Atom.Br;
				break;
			case 'a':
				label = Atom.Ba;
				break;
			case 'i':
				label = Atom.Bi;
				break;
			case 'h':
				label = Atom.Bh;
				break;
			case 'k':
				label = Atom.Bk;
				break;
			default:
				--pos;
				label = Atom.B;
			}
			break;
		case 'c':
			aromaticAtom = true;
		case 'C':
			c = nextChar();
			switch (c) {
			case 'l':
				label = Atom.Cl;
				break;
			case 'a':
				label = Atom.Ca;
				break;
			case 'r':
				label = Atom.Cr;
				break;
			case 'o':
				label = Atom.Co;
				break;
			case 'u':
				label = Atom.Cu;
				break;
			case 'd':
				label = Atom.Cd;
				break;
			case 's':
				label = Atom.Cs;
				break;
			case 'e':
				label = Atom.Ce;
				break;
			case 'f':
				label = Atom.Cf;
				break;
			default:
				--pos;
				label = Atom.C;
			}
			break;
		case 'D':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'b':
				label = Atom.Db;
				break;
			case 'y':
				label = Atom.Dy;
				break;
			case 's':
				label = Atom.Uun;
				break;
			default:
				throw new ParseException("Unknown atom symbol: D" + c, pos);
			}
			break;
		case 'E':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'u':
				label = Atom.Eu;
				break;
			case 'r':
				label = Atom.Er;
				break;
			case 's':
				label = Atom.Es;
				break;
			default:
				throw new ParseException("Unknown atom symbol: E" + c, pos);
			}
			break;
		case 'F':
			c = nextChar();
			switch (c) {
			case 'e':
				label = Atom.Fe;
				break;
			case 'r':
				label = Atom.Fr;
				break;
			default:
				--pos;
				label = Atom.F;
			}
			break;
		case 'G':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'a':
				label = Atom.Ga;
				break;
			case 'e':
				label = Atom.Ge;
				break;
			case 'd':
				label = Atom.Gd;
				break;
			default:
				throw new ParseException("Unknown atom symbol: G" + c, pos);
			}
			break;
		case 'H':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'e':
				label = Atom.He;
				break;
			case 'f':
				label = Atom.Hf;
				break;
			case 'g':
				label = Atom.Hg;
				break;
			case 's':
				label = Atom.Hs;
				break;
			case 'o':
				label = Atom.Ho;
				break;
			default:
				--pos;
				label = Atom.H;
			}
			break;
		case 'I':
			c = nextChar();
			switch (c) {
			case 'n':
				label = Atom.In;
				break;
			case 'r':
				label = Atom.Ir;
				break;
			default:
				--pos;
				label = Atom.I;
			}
			break;
		case 'K':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'r':
				label = Atom.Kr;
				break;
			default:
				--pos;
				label = Atom.K;
			}
			break;
		case 'L':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'i':
				label = Atom.Li;
				break;
			case 'u':
				label = Atom.Lu;
				break;
			case 'r':
				label = Atom.Lr;
				break;
			case 'a':
				label = Atom.La;
				break;
			default:
				throw new ParseException("Unknown atom symbol: L" + c, pos);
			}
			break;
		case 'M':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'g':
				label = Atom.Mg;
				break;
			case 'n':
				label = Atom.Mn;
				break;
			case 'o':
				label = Atom.Mo;
				break;
			case 't':
				label = Atom.Mt;
				break;
			case 'd':
				label = Atom.Md;
				break;
			default:
				throw new ParseException("Unknown atom symbol: M" + c, pos);
			}
			break;
		case 'n':
			aromaticAtom = true;
		case 'N':
			c = nextChar();
			switch (c) {
			case 'e':
				label = Atom.Ne;
				break;
			case 'a':
				label = Atom.Na;
				break;
			case 'i':
				label = Atom.Ni;
				break;
			case 'b':
				label = Atom.Nb;
				break;
			case 'd':
				label = Atom.Nd;
				break;
			case 'p':
				label = Atom.Np;
				break;
			case 'o':
				label = Atom.No;
				break;
			default:
				--pos;
				label = Atom.N;
			}
			break;
		case 'o':
			aromaticAtom = true;
		case 'O':
			c = nextChar();
			switch (c) {
			case 's':
				label = Atom.Os;
				break;
			default:
				--pos;
				label = Atom.O;
			}
			break;
		case 'p':
			aromaticAtom = true;
		case 'P':
			c = nextChar();
			switch (c) {
			case 'd':
				label = Atom.Pd;
				break;
			case 't':
				label = Atom.Pt;
				break;
			case 'b':
				label = Atom.Pb;
				break;
			case 'o':
				label = Atom.Po;
				break;
			case 'r':
				label = Atom.Pr;
				break;
			case 'm':
				label = Atom.Pm;
				break;
			case 'a':
				label = Atom.Pa;
				break;
			case 'u':
				label = Atom.Pu;
				break;
			default:
				--pos;
				label = Atom.P;
			}
			break;
		case 'R':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'b':
				label = Atom.Rb;
				break;
			case 'u':
				label = Atom.Ru;
				break;
			case 'h':
				label = Atom.Rh;
				break;
			case 'e':
				label = Atom.Re;
				break;
			case 'n':
				label = Atom.Rn;
				break;
			case 'a':
				label = Atom.Ra;
				break;
			default:
				throw new ParseException("Unknown atom symbol: R" + c, pos);
			}
			break;
		case 's':
			aromaticAtom = true;
		case 'S':
			c = nextChar();
			switch (c) {
			case 'i':
				label = Atom.Si;
				break;
			case 'c':
				label = Atom.Sc;
				break;
			case 'e':
				label = Atom.Se;
				break;
			case 'r':
				label = Atom.Sr;
				break;
			case 'n':
				label = Atom.Sn;
				break;
			case 'b':
				label = Atom.Sb;
				break;
			case 'g':
				label = Atom.Sg;
				break;
			case 'm':
				label = Atom.Sm;
				break;
			default:
				--pos;
				label = Atom.S;
			}
			break;
		case 'T':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'i':
				label = Atom.Ti;
				break;
			case 'c':
				label = Atom.Tc;
				break;
			case 'e':
				label = Atom.Te;
				break;
			case 'a':
				label = Atom.Ta;
				break;
			case 'l':
				label = Atom.Tl;
				break;
			case 'b':
				label = Atom.Tb;
				break;
			case 'm':
				label = Atom.Tm;
				break;
			case 'h':
				label = Atom.Th;
				break;
			default:
				throw new ParseException("Unknown atom symbol: T" + c, pos);
			}
			break;
		case 'U':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'u':
				c = nextChar();
				switch (c) {
				case 'n':
					label = Atom.Uun;
					break;
				case 'u':
					label = Atom.Uuu;
					break;
				case 'b':
					label = Atom.Uub;
					break;
				case 't':
					label = Atom.Uut;
					break;
				case 'q':
					label = Atom.Uuq;
					break;
				default:
					throw new ParseException("Unknown atom symbol: Uu" + c, pos);
				}
				break;
			default:
				--pos;
				label = Atom.U;
			}
			break;
		case 'V':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			label = Atom.V;
			break;
		case 'W':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			label = Atom.W;
			break;
		case 'X':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'e':
				label = Atom.Xe;
				break;
			default:
				throw new ParseException("Unknown atom symbol: X" + c, pos);
			}
			break;
		case 'Y':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'b':
				label = Atom.Yb;
				break;
			default:
				--pos;
				label = Atom.Y;
			}
			break;
		case 'Z':
			if (!bracketFound) {
				throw new ParseException("Expected '[' before atom symbol", pos);
			}
			c = nextChar();
			switch (c) {
			case 'n':
				label = Atom.Zn;
				break;
			case 'r':
				label = Atom.Zr;
				break;
			default:
				throw new ParseException("Unknown atom symbol: T" + c, pos);
			}
			break;
		default:
			throw new ParseException("Unknown atom symbol: " + c, pos);

		}
		final int node = g.addNodeIndex(label);
		if (aromaticAtom) {
			this.arom.set(node);
		}

		if (bracketFound) {
			// skip chiral specifications
			while (peekChar() == '@') {
				++pos;
			}
			// skip charges
			while ((peekChar() == '-') || (peekChar() == '+')) {
				++pos;
				if ((peekChar() >= '0') && (peekChar() <= '9')) {
					// charge
					// specification
					++pos;
				}
			}

			// check for explicit hydrogens
			if (peekChar() == 'H') {
				++pos;

				int hydrogens;
				if ((peekChar() >= '1') && (peekChar() <= '9')) {
					hydrogens = nextChar() - '0';
				} else {
					hydrogens = 1;
				}

				if (!noH) {
					for (int k = hydrogens - 1; k >= 0; k--) {
						g.addNodeAndEdgeIndex(node, Atom.H, Bond.singleB,
								Edge.UNDIRECTED);
					}
				}
				// skip charges
				while ((peekChar() == '-') || (peekChar() == '+')) {
					++pos;
					if ((peekChar() >= '0') && (peekChar() <= '9')) {
						// explicit
						// charge
						// specification
						++pos;
					}
				}
			}

			c = nextChar();
			// System.out.println("a. "+pos+" "+c+" "+bracketFound);
			if (!(c == ']')) {
				throw new ParseException("Expected ']', but found " + c, pos);
			}
		}

		c = nextChar();
		// System.out.println("b. "+pos+" "+c+" "+bracketFound);
		while ((c >= '1' && c <= '9') || c == '%') {
			if (c == '%') {
				c = (char) ((nextChar() - '0') * 10 + nextChar());
			}
			if (c == '1') {
				if (peekChar() == '0') { // markers up to 10 may be specified
					// without '%'
					++pos;
					c = ('9' + 1);
				}
				;
			}

			final Integer ref = marker.remove(c - '0');
			if (ref == null) {
				marker.put(c - '0', node);
				// System.out.println("added Marker "+(c-'0')+" "+pos);
			} else {
				g.addEdgeIndex(node, ref,
						arom.get(node) && arom.get(ref) ? Bond.aromaticB
								: Bond.singleB, Edge.UNDIRECTED);
			}
			c = nextChar();
		}
		--pos;

		if (!bracketFound && label.isOrganic()) {
			this.ffv.set(node);
		}

		return node;
	}

	/* parse the next bond */
	private Bond bond() throws ParseException {
		char c = nextChar();
		if ((c == '/') || (c == '\\')) {
			c = nextChar();
		}

		switch (c) {
		case '.':
			return Bond.noBond;
		case '=':
			return Bond.doubleB;
		case '#':
			return Bond.trippleB;
		case ':':
			return Bond.aromaticB;
		case '-':
			return Bond.singleB;
		default:
			--pos;
			return null;
		}
	}

	private int branch(final HPMutableGraph<Atom, Bond> g,
			final int previousNode) throws ParseException {
		char c;
		if ((c = nextChar()) != '(') {
			throw new ParseException("Expected '(' but found '" + c + "'", pos);
		}
		final int node = chain(g, previousNode);
		if ((c = nextChar()) != ')' && pos < m.length) {
			throw new ParseException("Expected ')' but found '" + c + "'", pos);
		}
		return node;
	}

	/**
	 * Recursively build a smiles.
	 * 
	 * @param g
	 *            the graph
	 * @param node
	 *            the current node in the DFS search through the molecule graph
	 * @param nodeVisited
	 *            an array where each visited atom has a <code>true</code>
	 *            entry at its index
	 * @param edgeVisited
	 *            an array where each visited edge has a <code>true</code>
	 *            entry at its index
	 * @param markers
	 *            an array where the marker number of each atom is stored; 0
	 *            means no marker
	 * @param buf
	 *            the StringBuffer that holds the SLN
	 */
	private void buildSmiles(final HPGraph<Atom, Bond> g, final int node,
			final boolean[] nodeVisited, final boolean[] edgeVisited,
			final int[] markers, final StringBuffer buf) {
		nodeVisited[node] = true;

		final Atom nLabel = g.getNodeLabel(node);

		if (!nLabel.equals(Atom.H)) {
			boolean needsBrackets = false;
			if (nLabel.isOrganic()) {
				int freeValences = nLabel.valenceElectrons();

				for (int k = g.getDegree(node) - 1; k >= 0; k--) {
					freeValences -= g.getEdgeLabel(g.getNodeEdge(node, k))
							.bindings();
				}

				boolean hydrogenFound = false;
				for (int i = g.getDegree(node) - 1; i >= 0; i--) {
					final int edge = g.getNodeEdge(node, i);

					if (g.getNodeLabel(g.getOtherNode(edge, node)).equals(
							Atom.H)) {
						hydrogenFound = true;
						break;
					}
				}
				if (hydrogenFound && (freeValences != 0)) {
					needsBrackets = true;
				}
			} else {
				needsBrackets = true;
			}

			if (needsBrackets) {
				buf.append('[');
				buf.append(nLabel.symbol());

				int hydrogens = 0;
				for (int i = g.getDegree(node) - 1; i >= 0; i--) {
					final int edge = g.getNodeEdge(node, i);
					if (g.getNodeLabel(g.getOtherNode(edge, node)).equals(
							Atom.H)) {
						hydrogens++;
					}
				}

				if (hydrogens == 1) {
					buf.append('H');
				} else if (hydrogens > 1) {
					buf.append('H').append(hydrogens);
				}

				buf.append(']');
			} else {
				boolean isAromatic = false;
				if (nLabel.isOrganic()) {
					for (int k = g.getDegree(node) - 1; k >= 0 && !isAromatic; k--) {
						isAromatic = (g.getEdgeLabel(g.getNodeEdge(node, k))
								.equals(Bond.aromaticB));
					}
				}

				if (isAromatic) {
					buf.append(nLabel.symbol().toLowerCase());
				} else {
					buf.append(nLabel.symbol());
				}
			}

			if (markers[node] != 0) { // atom needs a marker
				final int marker = markers[node];
				for (int k = 0; k < 32; k++) {
					if ((marker & (1 << k)) != 0) {
						if (k > 9) {
							buf.append('%').append(k + 1);
						} else {
							buf.append(k + 1);
						}
					}
				}
			}
		}

		// check the number of branches; if an atom has only one unvisited bond
		// it does not need a branch
		int branchCount = 0;
		for (int i = g.getDegree(node) - 1; i >= 0; i--) {
			final int edge = g.getNodeEdge(node, i);
			if (!edgeVisited[edge]) {
				if (!g.getNodeLabel(g.getOtherNode(edge, node)).equals(Atom.H)
						|| (g.getDegree(g.getOtherNode(edge, node)) > 1)) {
					branchCount++;
				}
			}
		}
		// if the atom has more than one unvisited bond but has a marker at
		// least one branch can be omitted
		if (markers[node] > 0) {
			branchCount--;
		}

		for (int i = g.getDegree(node) - 1; i >= 0; i--) {
			final int edge = g.getNodeEdge(node, i);

			if (!edgeVisited[edge]) {
				final int neighbour = g.getOtherNode(edge, node);

				if (g.getNodeLabel(neighbour).equals(Atom.H)
						&& (g.getDegree(neighbour) == 1)) {
					nodeVisited[neighbour] = true;
					continue;
				}

				boolean nextAtomIsReference = (nodeVisited[neighbour] && (markers[neighbour] != 0));
				if ((branchCount > 1) && !nextAtomIsReference) {
					buf.append('(');
				}
				if (!nLabel.equals(Atom.H)
						&& !g.getEdgeLabel(edge).equals(Bond.aromaticB)) {
					buf.append(g.getEdgeLabel(edge).symbol());
				}

				edgeVisited[edge] = true;
				if (nextAtomIsReference) {
					final int marker = markers[neighbour];

					for (int k = 0; k < 32; k++) {
						if ((marker & (1 << k)) != 0) {
							if (k > 9) {
								buf.append('%').append(k + 1);
							} else {
								buf.append(k + 1);
							}
							markers[neighbour] &= ~(1 << k);
							break;
						}
					}
				} else if (!nodeVisited[neighbour]) {
					buildSmiles(g, neighbour, nodeVisited, edgeVisited,
							markers, buf);
				}
				if ((branchCount > 1) && !nextAtomIsReference) {
					buf.append(')');
				}
			}
		}
	}

	private int chain(final HPMutableGraph<Atom, Bond> graph, int lastNodeIndex)
			throws ParseException {
		while (pos < m.length) {
			while (peekChar() == '(') { // new branch begins
				final int x = branch(graph, lastNodeIndex);
				if (lastNodeIndex == HPGraph.NO_NODE) {
					lastNodeIndex = x;
				}
				if (pos >= m.length) {
					return lastNodeIndex;
				}
			}

			if (peekChar() == ')') {
				break; // chain ends
			}

			Bond bondLabel = bond();
			final int node = atom(graph, lastNodeIndex);
			if (node != HPGraph.NO_NODE) {
				if (lastNodeIndex != HPGraph.NO_NODE
						&& bondLabel != Bond.noBond) {
					if (bondLabel == null) {
						bondLabel = (arom.get(node) && arom.get(lastNodeIndex) ? Bond.aromaticB
								: Bond.singleB);
					}
					graph.addEdgeIndex(lastNodeIndex, node, bondLabel,
							Edge.UNDIRECTED);
				}
				if (!lastWasReference) {
					lastNodeIndex = node;
				}
			}
		}

		return lastNodeIndex;
	}

	private final void fillupValences(final HPMutableGraph<Atom, Bond> graph) {
		for (int ack = ffv.nextSetBit(0); ack >= 0; ack = ffv
				.nextSetBit(ack + 1)) {
			int freeValences = graph.getNodeLabel(ack).valenceElectrons();
			for (int k = graph.getDegree(ack) - 1; k >= 0; k--) {
				freeValences -= graph.getEdgeLabel(graph.getEdge(ack, k))
						.bindings();
			}
			for (int k = freeValences - 1; k >= 0; k--) {
				graph.addNodeAndEdgeIndex(ack, Atom.H, Bond.singleB,
						Edge.UNDIRECTED);
			}

		}
	}

	/**
	 * Recursively identifies the atoms that need markers in a Smiles.
	 * 
	 * @param g
	 *            the graph
	 * @param node
	 *            the current node in the DFS search
	 * @param nodeVisited
	 *            an array where each visited atom has a <code>true</code>
	 *            entry at its index
	 * @param edgeVisited
	 *            an array where each visited edge has a <code>true</code>
	 *            entry at its index
	 * @param markerCount
	 *            an array where the number of needed markers for each atom is
	 *            counted
	 */
	private void findAtomMarkers(final HPGraph<Atom, Bond> g, final int node,
			final boolean[] nodeVisited, final boolean[] edgeVisited,
			final int[] markerCount) {
		nodeVisited[node] = true;

		for (int i = g.getDegree(node) - 1; i >= 0; i--) {
			final int edge = g.getNodeEdge(node, i);

			if (!edgeVisited[edge]) {
				final int neighbour = g.getOtherNode(edge, node);

				edgeVisited[edge] = true;
				if (nodeVisited[neighbour]) {
					markerCount[neighbour]++;
				} else {
					findAtomMarkers(g, neighbour, nodeVisited, edgeVisited,
							markerCount);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#getEdgeParser()
	 */
	public LabelParser<Bond> getEdgeParser() {
		return Bond.parser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#getNodeParser()
	 */
	public LabelParser<Atom> getNodeParser() {
		return Atom.parser();
	}

	private final char nextChar() throws ParseException {
		return (pos++ < m.length ? m[pos - 1] : '\0');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#parse(java.io.InputStream,
	 *      de.parsemis.graph.GraphFactory)
	 */
	public Collection<Graph<Atom, Bond>> parse(final InputStream io,
			final GraphFactory<Atom, Bond> factory) throws ParseException,
			IOException {
		final BufferedReader bin = new BufferedReader(new InputStreamReader(io));

		final LinkedList<Graph<Atom, Bond>> graphs = new LinkedList<Graph<Atom, Bond>>();
		String line;
		while ((line = bin.readLine()) != null) {
			if (line.startsWith("#")) {
				continue;
			}
			final int pos = line.indexOf(" => ");
			graphs.add(parse(line.substring(pos + " => ".length()), line
					.substring(0, pos), factory));
		}
		return graphs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#parse(java.lang.String,
	 *      de.parsemis.graph.GraphFactory)
	 */
	public Graph<Atom, Bond> parse(final String input,
			final GraphFactory<Atom, Bond> factory) throws ParseException {
		return parse(input, null, factory);
	}

	private Graph<Atom, Bond> parse(final String input, final String id,
			final GraphFactory<Atom, Bond> factory) throws ParseException {
		final MutableGraph<Atom, Bond> ret = (id == null ? factory.newGraph()
				: factory.newGraph(id));
		final HPMutableGraph<Atom, Bond> g = (HPMutableGraph<Atom, Bond>) ret
				.toHPGraph();
		m = input.toCharArray();
		pos = 0;
		ffv.clear();
		arom.clear();

		chain(g, HPGraph.NO_NODE);

		if (!noH) {
			fillupValences(g);
		}
		if (!marker.isEmpty()) {
			if (WARN) {
				err.println("unused markers in Graph " + id);
			}
			marker.clear();
		}
		return ret;
	}

	private final char peekChar() {
		return (pos < m.length ? m[pos] : '\0');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serialize(de.parsemis.graph.Graph)
	 */
	public String serialize(final Graph<Atom, Bond> graph) {
		final HPGraph<Atom, Bond> g = graph.toHPGraph();
		final boolean[] nodeVisited = new boolean[g.getMaxNodeIndex()];
		final boolean[] edgeVisited = new boolean[g.getMaxEdgeIndex()];
		final int[] markerCount = new int[g.getMaxNodeIndex()];

		// first identify atoms that need markers
		for (int node = g.getNodeCount() - 1; node >= 0; node--) {
			if (g.isValidNode(node) && !nodeVisited[node]) {
				findAtomMarkers(g, node, nodeVisited, edgeVisited, markerCount);
			}
		}

		// then build the Smiles
		for (int i = nodeVisited.length - 1; i >= 0; i--) {
			nodeVisited[i] = false;
		}
		for (int i = edgeVisited.length - 1; i >= 0; i--) {
			edgeVisited[i] = false;
		}

		final int[] markers = new int[g.getMaxNodeIndex()];
		int count = 0;
		for (int i = markerCount.length - 1; i >= 0; i--) {
			for (int k = markerCount[i] - 1; k >= 0; k--) {
				markers[i] |= (1 << count++);
			}
		}

		final StringBuffer buf = new StringBuffer(1024);

		count = 0;
		for (int node = g.getMaxNodeIndex() - 1; node >= 0; node--) {
			if (g.isValidNode(node) && !nodeVisited[node]) {
				if (count++ > 0) {
					buf.append('.');
				}
				buildSmiles(g, node, nodeVisited, edgeVisited, markers, buf);
			}
		}

		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serialize(java.io.OutputStream,
	 *      java.util.Collection)
	 */
	public void serialize(final OutputStream out,
			final Collection<Graph<Atom, Bond>> graphs) throws IOException {
		final BufferedOutputStream output = new BufferedOutputStream(out);

		for (final Graph<Atom, Bond> graph : graphs) {
			output.write(graph.getName().getBytes());
			output.write(" => ".getBytes());
			output.write(serialize(graph).getBytes());
			output.write("\n".getBytes());
		}
		output.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.parsers.GraphParser#serializeFragments(java.io.OutputStream,
	 *      java.util.Collection)
	 */
	public void serializeFragments(final OutputStream out,
			final Collection<Fragment<Atom, Bond>> frags) throws IOException {
		final BufferedOutputStream output = new BufferedOutputStream(out);

		for (final Fragment<Atom, Bond> frag : frags) {
			output.write(frag.toGraph().getName().getBytes());
			output.write(" => ".getBytes());
			output.write(serialize(frag.toGraph()).getBytes());
			output.write(("\n# => " + frag.frequency() + "[").getBytes());
			final Iterator<DataBaseGraph<Atom, Bond>> git = frag
					.graphIterator();
			if (git.hasNext()) {
				output.write(git.next().toGraph().getName().getBytes());
			}
			while (git.hasNext()) {
				output.write(" ,".getBytes());
				output.write(git.next().toGraph().getName().getBytes());
			}
			output.write("]\n".getBytes());
		}
		output.flush();
	}

}
