/**
 * Created on Jun 3, 2005
 *
 * @by Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Copyright 2005 Olga Urzova
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
package de.parsemis.visualisation.chemicalVisualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import de.parsemis.chemical.Atom;
import de.parsemis.graph.Graph;
import de.parsemis.visualisation.GraphPanel;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Diese Klasse nimmt die berechneten Koordinaten entgegen, skaliert sie neu,
 * wenn die Groesse des Fensters geaendert wird und zeichnet das Molekuel.
 * 
 * 
 * @param <NodeType>
 *            the type of the node labels (will be hashed and checked with
 *            .equals(..))
 * @param <EdgeType>
 *            the type of the edge labels (will be hashed and checked with
 *            .equals(..))
 */
public class Demo<NodeType, EdgeType> extends JPanel implements GraphPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2154638693335761967L;

	// static FontMetrics metrics;

	// static Font fontForSingleLetter, fontForTwoLetters;

	static int font_size;

	static float bondsLength = 1;

	static float bondsWidth;

	protected float xdifferenz, ydifferenz;

	private float minx, miny;

	private float optimize_xfactor, optimize_yfactor;

	private BondAndLabelDrawing bondAndLabelDrawer = null;

	private Vector<VectorElement> mainData;

	private Color bondColor;

	private boolean isMoleculeColored = true;

	private boolean setCarbonLabels = true;

	private boolean setShortBonds = true;

	private Color backgroundColor;

	private final float lfactor = 1.0f / 3.0f;

	private final float rfactor = 2.0f / 3.0f;

	private boolean isFragment = false;

	private boolean isImagePropertyChanged = false;

	// private Dimension m_dimension = new Dimension();

	private Image mImage = null;

	public Demo(final DataAnalyser analyser, final boolean setCarbonLabels,
			final boolean setShortBonds, final boolean doColoredLabels,
			final boolean isFragment) {
		bondAndLabelDrawer = new BondAndLabelDrawing();
		this.xdifferenz = analyser.getXDifference();
		this.ydifferenz = analyser.getYDifference();
		this.minx = analyser.getMinimumOfXValue();
		this.miny = analyser.getMinimumOfYValue();
		this.mainData = analyser.getDrawingData();
		this.setCarbonLabels = setCarbonLabels;
		this.setShortBonds = setShortBonds;
		bondColor = Color.BLACK;
		isMoleculeColored = doColoredLabels;
		backgroundColor = Color.WHITE;
		this.setBackground(backgroundColor);
		this.isFragment = isFragment;
	}

	/**
	 * Constructor
	 * 
	 * @param analyser
	 *            enthaelt die berechneten Koordinaten
	 * @param setCarbonLabels
	 * @param setShortBonds
	 * @param doColoredLabels
	 * @param isFragment
	 * @param s
	 */
	public Demo(final DataAnalyser analyser, final boolean setCarbonLabels,
			final boolean setShortBonds, final boolean doColoredLabels,
			final boolean isFragment, final Dimension s) {
		bondAndLabelDrawer = new BondAndLabelDrawing();
		this.xdifferenz = analyser.getXDifference();
		this.ydifferenz = analyser.getYDifference();
		this.minx = analyser.getMinimumOfXValue();
		this.miny = analyser.getMinimumOfYValue();
		this.mainData = analyser.getDrawingData();
		this.setCarbonLabels = setCarbonLabels;
		this.setShortBonds = setShortBonds;
		bondColor = Color.BLACK;
		isMoleculeColored = doColoredLabels;
		backgroundColor = Color.WHITE;
		this.setBackground(backgroundColor);
		this.isFragment = isFragment;
		this.setSize(s);
		this.setPreferredSize(s);
	}

	public Demo(final Graph<NodeType, EdgeType> g, final Dimension d,
			final boolean isFragment) {
		// TODO: get default values out of settings
		this(new DataAnalyser(g.toHPGraph()),
				ChemicalVisualisationSettings.setCarbonLabels,
				ChemicalVisualisationSettings.setShortBonds,
				ChemicalVisualisationSettings.doColoredLabels, isFragment, d);
	}

	public void addToPropertyChangeListener(final JComponent propertyChanger) {
		if (propertyChanger != null) {
			propertyChanger.addPropertyChangeListener("colored", this);
			propertyChanger.addPropertyChangeListener("carbon labels", this);
		}
	}

	private boolean checkOffscreenImage(final Dimension d) {
		if (mImage == null || mImage.getWidth(null) != d.width
				|| mImage.getHeight(null) != d.height) {
			mImage = createImage(d.width, d.height);
			return true;
		}
		if (this.isImagePropertyChanged) {
			isImagePropertyChanged = false;
			mImage = createImage(d.width, d.height);
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Demo<NodeType, EdgeType> clone() throws CloneNotSupportedException {
		// Cloneable theClone = this;
		// Initialize theClone.
		return (Demo<NodeType, EdgeType>) super.clone();
	}

	/**
	 * Diese Funktion ist fuer das mehrfarbige Zeichnen verantwortlich.
	 * 
	 * @param height
	 */
	private void drawColoredMolecule(final float height) {
		float x1_kor, y1_kor, x2_kor, y2_kor;
		Color fromColor, toColor = null;
		int kindOfBond;
		double angleSizeInCycle;
		VectorElement d;
		// Die Kanten mit dem rechten Atomsymbol zeichnen
		for (int i = 1; i < mainData.size(); i++) {
			float newLength = bondsLength;
			d = mainData.elementAt(i);
			x1_kor = d.x1 * bondsLength - optimize_xfactor;
			y1_kor = d.y1 * bondsLength - optimize_yfactor;
			x2_kor = d.x2 * bondsLength - optimize_xfactor;
			y2_kor = d.y2 * bondsLength - optimize_yfactor;

			y1_kor = height - y1_kor;
			y2_kor = height - y2_kor;
			// Die Laenge der Endkanten verkuerzen,
			// um die Kollisionen der Atomsymbole zu vermeiden
			if ((d.rdegree == 1) && (setShortBonds)) {
				x2_kor = getShortBond(x1_kor, x2_kor, rfactor);
				y2_kor = getShortBond(y1_kor, y2_kor, rfactor);
				newLength = rfactor * bondsLength;
			}
			if ((d.ldegree == 1) && (setShortBonds)) {
				x1_kor = getShortBond(x1_kor, x2_kor, lfactor);
				y1_kor = getShortBond(y1_kor, y2_kor, lfactor);
				newLength = rfactor * bondsLength;
			}

			angleSizeInCycle = d.angleSize;
			kindOfBond = d.kindOfBond;
			fromColor = Atom.atoms[d.leftAtomIndex].getColor();
			toColor = Atom.atoms[d.rightAtomIndex].getColor();
			// Das erste Atomsymbolsetzen
			if (i == 1) {
				if ((setCarbonLabels) || (d.leftAtomIndex != 6)) {
					bondAndLabelDrawer.centerText(
							mainData.elementAt(0).neighborLabel, fromColor,
							x1_kor, y1_kor);
				}
			}

			switch (kindOfBond) {
			case 1:
				bondAndLabelDrawer.singleBondColored(x1_kor, y1_kor, x2_kor,
						y2_kor, d.getKindOfEnd(setCarbonLabels),
						d.neighborLabel, fromColor, toColor, newLength);

				break;
			case 2:
				if (angleSizeInCycle > 0) {
					bondAndLabelDrawer.aromaticBondDouble(x1_kor, y1_kor,
							x2_kor, y2_kor, d.getKindOfEnd(setCarbonLabels),
							d.neighborLabel, fromColor, toColor);
				} else {
					bondAndLabelDrawer.doubleBondColored(x1_kor, y1_kor,
							x2_kor, y2_kor, d.getKindOfEnd(setCarbonLabels),
							d.neighborLabel, fromColor, toColor, newLength);
				}
				break;
			case 3:

				bondAndLabelDrawer.tripleBondColored(x1_kor, y1_kor, x2_kor,
						y2_kor, d.getKindOfEnd(setCarbonLabels),
						d.neighborLabel, fromColor, toColor, newLength);
				break;
			case 4:
				bondAndLabelDrawer.singleBondColored(x1_kor, y1_kor, x2_kor,
						y2_kor, d.getKindOfEnd(setCarbonLabels),
						d.neighborLabel, fromColor, toColor, newLength);
				bondAndLabelDrawer.aromaticBondDashed(x1_kor, y1_kor, x2_kor,
						y2_kor, angleSizeInCycle, d.angleSizeR);
				break;
			// Der Ausnahmefall: nur die gestrichelte Linie fuer die aromatische
			// Bindung wird gezeichnet
			case 5:
				bondAndLabelDrawer.aromaticBondDashed(x1_kor, y1_kor, x2_kor,
						y2_kor, angleSizeInCycle, d.angleSizeR);
				break;
			}
		}
	}

	/**
	 * Diese Funktion wird von painComponent() aufgerufen, wenn keine
	 * mehrfarbige Darstellung des Molekuels erfolgen sollte
	 * 
	 * @param height
	 */
	private void drawNonColoredMolecule(final float height) {
		float x1_kor, y1_kor, x2_kor, y2_kor;
		int kindOfBond;
		double angleSizeInCycle;
		VectorElement d;

		// Die Kanten mit dem rechten Atomsymbol zeichnen
		for (int i = 1; i < mainData.size(); i++) {
			float newLength = bondsLength;
			d = mainData.elementAt(i);
			x1_kor = d.x1 * bondsLength - optimize_xfactor;
			y1_kor = d.y1 * bondsLength - optimize_yfactor;
			x2_kor = d.x2 * bondsLength - optimize_xfactor;
			y2_kor = d.y2 * bondsLength - optimize_yfactor;
			y1_kor = height - y1_kor;
			y2_kor = height - y2_kor;

			// Die Laenge der Endkanten verkuerzen,
			// um die Kollisionen der Atomsymbole zu vermeiden
			if ((d.rdegree == 1) && (setShortBonds)) {
				x2_kor = getShortBond(x1_kor, x2_kor, rfactor);
				y2_kor = getShortBond(y1_kor, y2_kor, rfactor);
				newLength = rfactor * bondsLength;
			}
			if ((d.ldegree == 1) && (setShortBonds)) {
				x1_kor = getShortBond(x1_kor, x2_kor, lfactor);
				y1_kor = getShortBond(y1_kor, y2_kor, lfactor);
				newLength = rfactor * bondsLength;
			}

			if (i == 1) {
				// Das erste AtomSymbol setzen
				if ((setCarbonLabels) || (d.leftAtomIndex != 6)) {
					bondAndLabelDrawer.centerText(
							mainData.elementAt(0).neighborLabel, bondColor,
							x1_kor, y1_kor);
				}

			}

			angleSizeInCycle = d.angleSize;
			kindOfBond = d.kindOfBond;
			switch (kindOfBond) {
			case 1:

				bondAndLabelDrawer.singleBond(x1_kor, y1_kor, x2_kor, y2_kor, d
						.getKindOfEnd(setCarbonLabels), d.neighborLabel,
						newLength);
				break;
			case 2:
				if (angleSizeInCycle > 0) {
					bondAndLabelDrawer.aromaticBondDouble(x1_kor, y1_kor,
							x2_kor, y2_kor, d.getKindOfEnd(setCarbonLabels),
							d.neighborLabel, bondColor, bondColor);
				} else {
					bondAndLabelDrawer.doubleBond(x1_kor, y1_kor, x2_kor,
							y2_kor, d.getKindOfEnd(setCarbonLabels),
							d.neighborLabel, newLength);
				}
				break;
			case 3:
				bondAndLabelDrawer.tripleBond(x1_kor, y1_kor, x2_kor, y2_kor, d
						.getKindOfEnd(setCarbonLabels), d.neighborLabel,
						newLength);
				break;
			case 4:
				bondAndLabelDrawer.singleBond(x1_kor, y1_kor, x2_kor, y2_kor, d
						.getKindOfEnd(setCarbonLabels), d.neighborLabel,
						newLength);
				bondAndLabelDrawer.aromaticBondDashed(x1_kor, y1_kor, x2_kor,
						y2_kor, angleSizeInCycle, d.angleSizeR);
				break;
			// Der Ausnahmefall: nur die gestrichelte Linie fuer die aromatische
			// Bindung wird gezeichnet
			case 5:
				bondAndLabelDrawer.aromaticBondDashed(x1_kor, y1_kor, x2_kor,
						y2_kor, angleSizeInCycle, d.angleSizeR);
				break;
			}
		}

	}

	public JComponent getComponent() {
		return this;
	}

	/**
	 * Diese Hilfsfunktion berechnet neue Koordinate fuer die verkuerzte Bindung
	 * 
	 * @param x1
	 * @param x2
	 * @param factor
	 *            Der Vekleinungsfaktor
	 * @return something
	 */
	private float getShortBond(final float x1, final float x2,
			final float factor) {
		return (x2 - x1) * factor + x1;
	}

	private void initFragmentFrameProperties(final Dimension s) {
		final float width_fact = s.width * 0.9f;
		final float height_fact = s.height * 0.9f;
		// Die Laenge der Verbindungslinie(Bond) festlegen
		if (s.width > s.height) {
			bondsLength = width_fact / 3;
			while ((xdifferenz * bondsLength > width_fact)
					|| (ydifferenz * bondsLength > height_fact)) {

				bondsLength /= 1.5;
			}

		} else {
			bondsLength = height_fact / 3;
			while ((xdifferenz * bondsLength > width_fact)
					|| (ydifferenz * bondsLength > height_fact)) {
				bondsLength /= 1.5;

			}
		}

		final float xkorrektur = s.width * 0.05f
				+ (width_fact - xdifferenz * bondsLength) / 2f;
		final float ykorrektur = s.height * 0.05f
				+ (height_fact - ydifferenz * bondsLength) / 2f;
		optimize_xfactor = minx * bondsLength - xkorrektur;
		optimize_yfactor = miny * bondsLength - ykorrektur;
		bondsWidth = bondsLength / 35f;
		font_size = (int) (bondsWidth * 12);
	}

	/**
	 * Diese Funktion rechnet alle fuer das Zeichnen benoetigten Groessen um in
	 * Abhaengigkeit von der Groesse der Swing-Componente.
	 * 
	 * @param s
	 *            die Groesse der Swing-Komponente
	 */
	public void initFramePropeties(final Dimension s) {
		// Die Flaeche zum Zeichnen betraegt 90% von
		// der Gesamtflaeche
		final float width_fact = s.width * 0.9f;
		final float height_fact = s.height * 0.9f;
		// Die Laenge der Verbindungslinie(Bond) festlegen
		if (s.width > s.height) {
			bondsLength = width_fact / 10;
			if ((xdifferenz * bondsLength > width_fact)
					|| (ydifferenz * bondsLength > height_fact)) {
				if (((xdifferenz * bondsLength) - width_fact) > (ydifferenz
						* bondsLength - height_fact)) {
					bondsLength /= (xdifferenz * bondsLength) / width_fact;
				} else {
					bondsLength /= (ydifferenz * bondsLength) / height_fact;
				}
			}
		} else {
			bondsLength = height_fact / 10;

			if (((xdifferenz * bondsLength) > width_fact)
					|| ((ydifferenz * bondsLength) > height_fact)) {
				if (((xdifferenz * bondsLength) - width_fact) > (ydifferenz
						* bondsLength - height_fact)) {
					bondsLength /= (xdifferenz * bondsLength) / width_fact;
				} else {
					bondsLength /= (ydifferenz * bondsLength) / height_fact;
				}
			}
		}
		// Der Rand ist entsprechend jeweils 5%
		// von der Laenge in jede Richtung breit + das Bild zentrieren
		final float xkorrektur = s.width * 0.05f
				+ (width_fact - xdifferenz * bondsLength) / 2f;
		final float ykorrektur = s.height * 0.05f
				+ (height_fact - ydifferenz * bondsLength) / 2f;
		// Um die Koordinatenumrechnung zu optimieren
		// statt z.B. x1_kor = (d.x1 - minx) * bondsLength + xkorrektur;
		// x1_kor = d.x1 * bondsLength - optimize_xfactor;
		optimize_xfactor = minx * bondsLength - xkorrektur;
		optimize_yfactor = miny * bondsLength - ykorrektur;
		// Die Groesse des Schriftes hangt von der Breitw der Linie ab,
		// die Breite der Linie von ihrer eigenen Laenge
		bondsWidth = bondsLength / 35f;
		font_size = (int) (bondsWidth * 12);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		final Dimension d = getSize();
		// Bei jedem Aufruf von paintComponent() zuerst das alte Bild zeichnen
		if (mImage != null) {
			g.drawImage(mImage, 0, 0, null);
		}
		// Wenn die Groesse des Bildes sich geaendert hat oder
		// paintComponent() zum ersten Mal aufgerufen ist, dann
		// das Molekuel neu zeichnen.
		if (checkOffscreenImage(d)) {

			final Graphics offG = mImage.getGraphics();
			paintOffscreen(offG, d);
			this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
			g.drawImage(mImage, 0, 0, null);
			offG.dispose();
		}
	}

	/**
	 * Diese Funktion zeichnet das Molekuel auf der Swing-Komponente
	 * 
	 * @param g
	 * @param s
	 *            die aktuelle Bildgroesse
	 */
	public void paintOffscreen(final Graphics g, final Dimension s) {
		super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		final float height = s.height;
		if (isFragment) {
			initFragmentFrameProperties(s);
		} else {
			initFramePropeties(s);
		}
		bondAndLabelDrawer.setProperties(bondsLength, bondsWidth, font_size,
				g2, bondColor);

		if (isMoleculeColored) {
			drawColoredMolecule(height);
		} else {
			drawNonColoredMolecule(height);
		}
		g2.setStroke(new BasicStroke());
	}

	public void paintOffscreen2(final Graphics g, final Dimension s) {
		// super.paintComponent(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		final float height = s.height;
		if (isFragment) {
			initFragmentFrameProperties(s);
		} else {
			initFramePropeties(s);
		}
		bondAndLabelDrawer.setProperties(bondsLength, bondsWidth, font_size,
				g2, bondColor);
		if (isMoleculeColored) {
			drawColoredMolecule(height);
		} else {
			drawNonColoredMolecule(height);
		}
		g2.setStroke(new BasicStroke());
	}

	public void propertyChange(final PropertyChangeEvent event) {
		final String propertyName = event.getPropertyName();
		if (propertyName.equals("colored")) {
			this.isMoleculeColored = ((Boolean) event.getNewValue())
					.booleanValue();
			this.isImagePropertyChanged = true;
			this.repaint();
			return;
		}
		if (propertyName.equals("carbon labels")) {
			this.setCarbonLabels = ((Boolean) event.getNewValue())
					.booleanValue();
			this.isImagePropertyChanged = true;
			this.repaint();
		}

	}

}