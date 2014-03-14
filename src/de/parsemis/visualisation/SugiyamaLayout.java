/**
 * Created Aug 23, 2007
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
package de.parsemis.visualisation;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import prefuse.action.layout.graph.TreeLayout;
import prefuse.data.Graph;
import prefuse.util.collections.IntIterator;
import prefuse.visual.NodeItem;

/**
 * 
 * @author Sebastian Lenz (siselenz@stud.informatik.uni-erlangen.de)
 */
public class SugiyamaLayout extends TreeLayout {

	// public static String sync = "sync";

	public static class Co {
		private double x;

		private final int y;

		final private int id;

		public Co(final double x, final int y, final int id) {
			this.x = x;
			this.y = y;
			this.id = id;
		}

		public void changeX(final double i) {
			this.x = i;
		}

		public int getId() {
			return this.id;
		}

		public double getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}

	}

	private double m_bspace = 30; // the spacing between sibling nodes

	private double m_tspace = 25; // the spacing between subtrees

	private double m_dspace = 50; // the spacing between depth levels

	private double m_offset = 50; // pixel offset for root node position

	private final double[] m_depths = new double[10];

	private double m_ax, m_ay; // for holding anchor co-ordinates

	private int[] degree;

	private final Map<Integer, Collection<Double>> calc;

	private Co[] coord;

	private int maxLen;

	// ------------------------------------------------------------------------

	boolean ready = false;

	/**
	 * 
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 */
	public SugiyamaLayout(final String group) {
		super(group);
		calc = new HashMap<Integer, Collection<Double>>();
	}

	private void computeX(final Graph g) {

		Arrays.sort(coord, new Comparator<Co>() {
			public int compare(final Co co1, final Co co2) {
				final Double x1 = co1.getX();
				final Integer y1 = co1.getY();
				final Integer id1 = co1.getId();
				final Double x2 = co2.getX();
				final Integer y2 = co2.getY();
				final Integer id2 = co2.getId();

				if (!(y1.equals(y2))) {
					return y1.compareTo(y2);
				} else if (!(x1.equals(x2))) {
					return x1.compareTo(x2);
				} else {
					return id1.compareTo(id2);
				}
			}
		});

		double fix = 0;
		int i = 0;

		int layer = 0;
		int state = 0;
		int arraypos = 0;
		int layerstart = 0;
		double istSum = 0;
		double sollSum = 0;

		while (i < coord.length) {
			switch (state) {
			case 0: // layer = 0, x wird gesetzt
				coord[arraypos].changeX(fix);
				fix++;
				arraypos++;
				if (arraypos == coord.length) { // keine knoten mehr uebrig,
					// abbruchbedingung
					i = arraypos;
					break;
				}
				if (coord[arraypos].getY() != layer) { // ende aktueller layer
					state = 2;
				}

				break;
			case 1: // layer != 0, x muss berechnet werden

				double erg = 0;
				if (calc.containsKey(coord[arraypos].getId())) {
					final Collection<Double> test = calc.get(coord[arraypos]
							.getId());
					final Iterator<Double> values = test.iterator();
					while (values.hasNext()) {
						erg = erg + values.next();
					}
					erg = erg / test.size();
				} else {
					assert (false);
				}
				coord[arraypos].changeX(erg);
				arraypos++;

				if (arraypos == coord.length) {// keine knoten mehr uebrig
					state = 3;// kein abbruch, da noch ueberpruefung auf
					// ueberschneidung noetig
					break;
				}

				if (coord[arraypos].getY() != layer) {// ende aktueller layer
					state = 3;
					break;
				}
				break;
			case 2: // vorberechnung der nachfolger
				if (arraypos == coord.length) {// keine knoten mehr uebrig,
					// abbruchbedingung
					i = arraypos;
					break;
				}
				for (int j = layerstart; j < arraypos; j++) {
					double f = 0;
					final IntIterator it = g.outEdgeRows(coord[j].getId());
					final double n = g.getOutDegree(coord[j].getId());
					while (it.hasNext()) {
						final int e = it.nextInt();
						final int t = g.getTargetNode(e);
						Collection<Double> temp = calc.get(t);
						if (temp == null) {
							temp = new Vector<Double>();
						}
						temp.add(-((n - 1) / 2) + f + coord[j].getX());
						calc.put(t, temp);
						f = f + 1.0;
					}
				}
				state = 1;
				layer++;
				layerstart = arraypos;
				i = arraypos;
				break;
			case 3: // test auf eventuelle ueberschneidungen
				sollSum = 0;
				istSum = 0;
				Arrays.sort(coord, layerstart, arraypos, new Comparator<Co>() {// aktueller
							// layer
							// nach
							// x
							// sortieren
							public int compare(final Co co1, final Co co2) { // um
								// feste
								// reihefolge
								// fuer
								// schleife
								// zu haben
								final Double x1 = co1.getX();
								final Integer id1 = co1.getId();
								final Double x2 = co2.getX();
								final Integer id2 = co2.getId();

								if (!(x1.equals(x2))) {
									return x1.compareTo(x2);
								} else {
									return id1.compareTo(id2);
								}
							}
						});
				for (int j = layerstart; j < arraypos - 1; j++) {
					final double diff = Math.abs(coord[j].getX()
							- coord[j + 1].getX());
					if (diff < 1) {
						sollSum++;

					} else {
						sollSum = sollSum + diff;
					}
					istSum = istSum + diff;
				}
				if (Math.abs(istSum - sollSum) < .0000001) {
					// keine ueberschneidung
					// state = 2; //weiter mit vorberechnung
					state = 5; // weiter mit Kantenkreuzungen bereinigen
				} else {// ueberschneidung, korrigieren ...
					state = 4;
				}
				break;
			case 4: // korrigieren der ueberschneidungen
				double oldPosX = coord[layerstart].getX();
				coord[layerstart].changeX(oldPosX - (sollSum - istSum) / 2);
				for (int j = layerstart; j < arraypos - 2; j++) {
					final double diff = Math.abs(oldPosX - coord[j + 1].getX());
					oldPosX = coord[j + 1].getX();
					if (diff < 1) {
						coord[j + 1].changeX(coord[j].getX() + 1);
					} else {
						coord[j + 1].changeX(coord[j].getX() + diff);
					}
				}
				coord[arraypos - 1].changeX(coord[arraypos - 1].getX()
						+ (sollSum - istSum) / 2);
				// state = 2; //weiter mit vorberechnung
				state = 5; // weiter mit Kantenkreuzungen bereinigen
				break;
			case 5: // entfernen der kantenkreuzungen
				for (int j = layerstart; j < arraypos - 1; j++) {

					final IntIterator it = g.inEdgeRows(coord[j].getId());// vorgaenger
					// knoten 1
					// finden
					Co[] test1 = null;
					int y = 0;
					while (it.hasNext()) {
						final int e = it.nextInt();
						final int t = g.getSourceNode(e);
						if (test1 == null) {
							test1 = new Co[g.getInDegree(coord[j].getId())];
						}
						int z = 0;
						while (coord[z].getId() != t) {
							z++;
						}
						test1[y] = coord[z];
						y++;
					}

					final IntIterator et = g.inEdgeRows(coord[j + 1].getId());// vorgaenger
					// knoten
					// 2
					// finden
					Co[] test2 = null;
					y = 0;
					while (et.hasNext()) {
						final int e = et.nextInt();
						final int t = g.getSourceNode(e);
						if (test2 == null) {
							test2 = new Co[g.getInDegree(coord[j + 1].getId())];
						}
						int z = 0;
						while (coord[z].getId() != t) {
							z++;
						}
						test2[y] = coord[z];
						y++;
					}
					if (test1 == null | test2 == null) {
						break;
					}
					// vorgaenger nach x sortieren
					if (test1.length > 1) {
						Arrays.sort(test1, new Comparator<Co>() {
							public int compare(final Co co1, final Co co2) {
								final Double x1 = co1.getX();
								final Double x2 = co2.getX();

								return x1.compareTo(x2);
							}
						});
					}
					if (test2.length > 1) {
						Arrays.sort(test2, new Comparator<Co>() {
							public int compare(final Co co1, final Co co2) {
								final Double x1 = co1.getX();
								final Double x2 = co2.getX();

								return x1.compareTo(x2);
							}
						});
					}
					// wenn der linkeste vorgaenger des rechten knoten < der
					// rechteste vorgaenger des linken knotens
					if (test2[0].getX() < test1[test1.length - 1].getX()) {
						final double sw = coord[j].getX();
						coord[j].changeX(coord[j + 1].getX());
						coord[j + 1].changeX(sw);
					}

				}
				state = 2;
				break;
			default:
				break;
			}// end-switch
		}// end-while

	}

	private void computeY(final Graph g) {
		int r = 0;
		int c = 0;
		final int cnt = g.getNodeCount();
		degree = new int[cnt];
		final IntIterator it = g.nodeRows();
		while (it.hasNext()) {
			final int i = it.nextInt();
			degree[i] = g.getInDegree(i);
		}
		while (c != cnt) {
			c = rank(r, c, g);
			r++;
		}
	}

	/**
	 * Get the spacing between neighbor nodes.
	 * 
	 * @return the breadth spacing
	 */
	public double getBreadthSpacing() {
		return m_bspace;
	}

	/**
	 * Get the spacing between depth levels.
	 * 
	 * @return the depth spacing
	 */
	public double getDepthSpacing() {
		return m_dspace;
	}

	/**
	 * Get the offset value for placing the root node of the tree.
	 * 
	 * @return the value by which the root node of the tree is offset
	 */
	public double getRootNodeOffset() {
		return m_offset;
	}

	/**
	 * Get the spacing between neighboring subtrees.
	 * 
	 * @return the subtree spacing
	 */
	public double getSubtreeSpacing() {
		return m_tspace;
	}

	// -----------------------------------------------------------------------

	private int rank(final int r, int c, final Graph g) {
		final int cnt = g.getNodeCount();
		for (int j = 0; j < cnt; j++) {
			if (degree[j] == 0) {
				coord[j] = new Co(0.0, r, j);
				maxLen = Math.max(maxLen, g.getNode(j).getString("name")
						.length());
				degree[j] = -1;
			}
		}
		for (int j = 0; j < cnt; j++) {
			if (degree[j] == -1) {
				final IntIterator ed = g.outEdgeRows(j);
				while (ed.hasNext()) {
					final int e = ed.nextInt();
					degree[g.getTargetNode(e)]--;
				}
				degree[j] = -2;
				c++;
			}
		}
		return c;
	}

	public synchronized boolean ready() {
		return ready;
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public synchronized void run(final double frac) {

		maxLen = 0;

		final Graph g = (Graph) m_vis.getGroup(m_group);

		Arrays.fill(m_depths, 0);

		final Point2D a = getLayoutAnchor();
		m_ax = a.getX();
		m_ay = a.getY();

		// -----
		coord = new Co[g.getNodeCount()];
		computeY(g);
		computeX(g);

		for (int i = 0; i < g.getNodeCount(); i++) {
			final NodeItem n = (NodeItem) g.getNode(coord[i].getId());
			double hoch = coord[i].getY();
			double breit = coord[i].getX();

			breit = breit * m_bspace * Math.sqrt(maxLen);
			hoch = hoch * m_dspace;

			setBreadth(n, null, breit);
			setDepth(n, null, hoch);
		}

		ready = true;
	}

	private void setBreadth(final NodeItem n, final NodeItem p, final double b) {
		setX(n, p, m_ax + b);
	}

	/**
	 * Set the spacing between neighbor nodes.
	 * 
	 * @param b
	 *            the breadth spacing to use
	 */
	public void setBreadthSpacing(final double b) {
		m_bspace = b;
	}

	private void setDepth(final NodeItem n, final NodeItem p, final double d) {
		setY(n, p, m_ay + d);
	}

	/**
	 * Set the spacing between depth levels.
	 * 
	 * @param d
	 *            the depth spacing to use
	 */
	public void setDepthSpacing(final double d) {
		m_dspace = d;
	}

	/**
	 * Set the offset value for placing the root node of the tree. The dimension
	 * in which this offset is applied is dependent upon the orientation of the
	 * tree. For example, in a left-to-right orientation, the offset will a
	 * horizontal offset from the left edge of the layout bounds.
	 * 
	 * @param o
	 *            the value by which to offset the root node of the tree
	 */
	public void setRootNodeOffset(final double o) {
		m_offset = o;
	}

	/**
	 * Set the spacing between neighboring subtrees.
	 * 
	 * @param s
	 *            the subtree spacing to use
	 */
	public void setSubtreeSpacing(final double s) {
		m_tspace = s;
	}

}
