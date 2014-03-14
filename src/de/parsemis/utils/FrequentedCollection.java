/**
 * Created on Sep 15, 2006
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
package de.parsemis.utils;

import java.util.Collection;

/**
 * This interface describes a collection which stores frequented objects and has
 * an overall frequency
 * 
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 * @param <Type>
 *            frequented type that is stored in the Collection
 * 
 */
public interface FrequentedCollection<Type extends Frequented> extends
		Collection<Type>, Frequented {

}
