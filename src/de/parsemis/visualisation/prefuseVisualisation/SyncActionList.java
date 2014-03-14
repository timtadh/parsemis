/**
 * Created Aug 24, 2007
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
package de.parsemis.visualisation.prefuseVisualisation;

import prefuse.action.ActionList;

/**
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * 
 */
public class SyncActionList extends ActionList {
	/*
	 * (non-Javadoc)
	 * 
	 * @see prefuse.action.ActionList#run(double)
	 */
	@Override
	public synchronized void run(final double frac) {
		super.run(frac);
	}

}
