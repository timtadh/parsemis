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

import de.parsemis.chemical.Atom;

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Diese Klasse verwaltet die fuer das Zeichnen verwendeten Daten.
 * 
 */
public class VectorElement {
	/**
	 * Die Art der Bindung: 1 ist die einfache Bindung. 2 ist die Doppelbindung.
	 * 3 ist die Dreifachbindung. 4 ist die aromatische Bindung in einem Ring
	 * die aus zwei Linien besteht, wobei die Linie innerhalb des Ringes
	 * gestrichelt ist. 5 steht fuer eine gestrichelte Linie. Diese Art wird nur
	 * bei dem Zeichnen der Bloecken verwendet.
	 * 
	 */
	int kindOfBond;

	/**
	 * Der Index des Atoms rechts von der Kante
	 */
	int rightAtomIndex;

	/**
	 * Der Index des Atoms links von der Kante
	 */
	int leftAtomIndex;

	/**
	 * Der Grad der beiden Knoten. Dieser Wert wird bei dem Zeichnen verkuerzter
	 * Bindungen am Ende einer Kette gebraucht.
	 */
	int ldegree, rdegree;

	/**
	 * Die berechneten Koordinaten
	 */
	float x1, y1, x2, y2;

	/**
	 * Die Beschriftung des Atoms rechts von der Kante
	 */
	String neighborLabel;

	/**
	 * Der Winkel in dem Ring and beiden Seiten der Kante. Diese Werte werden
	 * bei dem Zeichnen der nichtregulaeren und regulaeren Ringen gebraucht.
	 */
	double angleSize = 0;

	double angleSizeR = 0;

	// boolean isInFragment = false;

	VectorElement(final float x_begin, final float y_begin, final float x_end,
			final float y_end, final int b, final int lindex, final int rindex,
			final int ldegree, final int rdegree, final double lsize,
			final double rsize) {
		x1 = x_begin;
		y1 = y_begin;
		x2 = x_end;
		y2 = y_end;
		this.ldegree = ldegree;
		this.rdegree = rdegree;
		neighborLabel = Atom.atoms[rindex].symbol();
		kindOfBond = b;
		leftAtomIndex = lindex;
		rightAtomIndex = rindex;
		angleSize = lsize;
		angleSizeR = rsize;
	}

	/**
	 * Diese Funktion bestimmt wie die Bindung gezeichnet wird (in der
	 * Abhaengigkeit davon, ob die Kohlenstoffatomsymbole ausgeschrieben sind
	 * oder nicht).
	 * 
	 * @param isCarbonLabelSet
	 * @return something
	 */
	public int getKindOfEnd(final boolean isCarbonLabelSet) {
		if (isCarbonLabelSet) {
			return 3;
		}
		if ((leftAtomIndex != 6) && (rightAtomIndex != 6)) {
			return 3;
		}
		if ((leftAtomIndex == 6) && (rightAtomIndex == 6)) {
			return 0;
		} else {
			if (leftAtomIndex != 6) {
				if ((kindOfBond == 2) && (rdegree > 2) && (angleSize == 0)) {
					return 5;
				}

				return 2;
			}
			if ((kindOfBond == 2) && (ldegree > 2) && (angleSize == 0)) {
				return 4;
			}
			return 1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PunktA (" + x1 + ", " + y1 + ") mit index " + leftAtomIndex
				+ ", PunktB (" + x2 + ", " + y2 + ") mit index "
				+ rightAtomIndex + " \n";
	}
}