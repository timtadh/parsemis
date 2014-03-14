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
 * Contains the gSpan Mining Algorithm.
 * <p>
 * This implementation is combines the basic ideas and the CloseSpan extension as other 
 * extensions like the search in directed graphs. It works on databases containing 
 * undirected or directed cyclic and acyclic graphs with arbitrary node and edge labels.
 * <p>
 * This algorithm bases on the right most path extension (see the 
 * {@link de.parsemis.algorithms.gSpan.RightMostExtension} class) and the resulting depth 
 * first search canonical code (see the {@link de.parsemis.algorithms.gSpan.DFSCode} class).
 * <p>  
 * @see <a target="_new" href="http://www.xifengyan.net/software/gSpan.htm">The original gSpan page</a>
 * <p>
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
package de.parsemis.algorithms.gSpan;
