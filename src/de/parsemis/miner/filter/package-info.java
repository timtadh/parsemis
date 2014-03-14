/**
 * created Aug 20, 2008
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

/**
 * Contains post-search fragment filter.
 * <p>
 * To achive compareable results as other mining algorithms a filtering of the found fragment set
 * after a mining run might be necessary. Removing unwanted fragments during the search is most times 
 * more efficient, but only possible if you can decide on the basis of the fragment and its direct children
 * if it is relevant.    
 * <p>
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
package de.parsemis.miner.filter;
