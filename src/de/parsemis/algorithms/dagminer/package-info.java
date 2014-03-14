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
 * Contains the DAG Mining Algorithm.
 * <p>
 * It works on databases containing directed acyclic graphs (DAGs) with arbitrary node labels and a single edge label. 
 * <p>
 * This algorithm bases on a topological node insertion order represented by its canonical 
 * form and four extension rules (see the {@link de.parsemis.algorithms.dagminer.DAGmNewRootExtension}, 
 * {@link de.parsemis.algorithms.dagminer.DAGmNewLevelExtension},
 * {@link de.parsemis.algorithms.dagminer.DAGmNewNodeExtension}, and 
 * {@link de.parsemis.algorithms.dagminer.DAGmNewEdgeExtension} class).
 * <p>
 * Example for the canonical form:<br/>
 * <img src="doc-files/canonical.png">
 * <p>
 * @author Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 */
package de.parsemis.algorithms.dagminer;

