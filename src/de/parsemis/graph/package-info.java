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
 * Contains the graph representations of ParSeMiS.
 * <p>
 * The databases graphs and internal search fragments of each algorithm
 * use these interfaces and classes.
 * <p>
 * Two kinds of graphs interfaces are declared:<br/>
 * <ul>
 *   <li> object orientated graphs (see the {@link de.parsemis.graph.Graph}, 
 *   {@link de.parsemis.graph.Node}, and {@link de.parsemis.graph.Edge} interfaces)</li>
 *   <li> high performance graphs represented by int-arrays (see the 
 *   {@link de.parsemis.graph.HPGraph} interface)</li>
 * </ul> 
 * Both have similare functionality. The first one is more comfortable and used
 * during the design and test phase. The second one is more efficient in runtime.
 * A straight forward step by step replacement after the testing to improve runtime 
 * is possible thanks to the corresponding wrapper classes.
 * <p>
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
package de.parsemis.graph;
