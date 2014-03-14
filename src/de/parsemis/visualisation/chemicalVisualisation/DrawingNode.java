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

/**
 * 
 * @author Olga Urzova (siolurzo@stud.informatik.uni-erlangen.de)
 * 
 * Diese Klasse enthaelt Informationen ueber einen Knoten aus dem Graph
 * 
 */

public class DrawingNode {
	int value;

	float x, y;

	double angle;

	double directionAngle;

	int mainDirection;

	/**
	 * Constructor
	 * 
	 * @param index
	 * @param x_new
	 * @param y_new
	 * @param angle
	 * @param directionAngle
	 * @param mainDirection
	 *            die Richtung, in welche das Molekuel gezeichnet wird
	 */
	DrawingNode(final int index, final float x_new, final float y_new,
			final double angle, final double directionAngle,
			final int mainDirection) {
		this.value = index;
		this.x = x_new;
		this.y = y_new;
		this.angle = angle;
		this.directionAngle = directionAngle;
		this.mainDirection = mainDirection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + value + ", " + x + ", " + y + ", " + angle + "]";
	}
}