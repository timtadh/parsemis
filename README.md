# ParSeMiS

This is the [Parallel and Sequential Mining Suite (ParSeMiS)](https://www2.cs.fau.de/EN/research/zold/ParSeMiS/index.html), built 2008-12-01.

The ParSeMiS project (Parallel and Sequential Graph Mining Suite) searches for frequent, interesting substructures in graph databases. This task is becoming increasingly popular because science and commerce need to detect, store, and process complex relations in huge graph structures.

For huge data that cannot be worked on manually, algorithms are needed that detect interesting correlations. Since in general the problem is NP-hard and requires huge amounts of computation time and memory, parallel or specialized algorithms and heuristics are required that can perform the search within time boundaries and memory limits.

Our target is to provide an efficient and flexible tool for searching in arbitrary graph data, to improve the adaption to new application areas, and to simplify and unify the design of new mining algorithms.

In 2010, the distributed stack implementations have also been tested on other algorithms and data structures.

# Requirements

The ParSeMiS library requires three different external libraries:
- (Apache Ant](https://ant.apache.org/) to manage the build operations
- [antlr](http://antlr.org/download.html) 2.7.6 for parsing `.dot` files 
- [prefuse](https://sourceforge.net/projects/prefuse/) 2007.10.21 for the GUI

# Compiling

There are multiple Ant tasks available, the most useful being:
- `all`: clean the project and build it again
- `javadoc`: build the project documentation

Once the related `.jar` file is built, you may check the following section Usage or run the `.jar` to get the usage information:
```bash
java -jar parsemis.jar
```

To use the library in other projects, please add the corresponding `.jar` files to the related `lib/` directory.

## Usage
Available options:
```bash
General options:
	--graphFile=file
		The file from which the graphs should be read
	--outputFile=file (optional)
		The file to which the found frequent subgraphs should be written ('-' for stdout)
	--swapFile=file (optional)
		A file to temporarly to swap out temporary unused objects

	--minimumFrequency=freq (integer or percentage)
		The minimum frequency a fragment must have to get reported
	--maximumFrequency=freq (integer or percentage) (optional)
		The maximum frequency a fragment can have to get reported
	--minimumNodeCount=int (optional; default: 0)
		The minimum size in nodes a fragment must have to get reported
	--maximumNodeCount=int (optional; default: 0 = all)
		The maximum size in nodes a fragment can have to get reported
	--minimumEdgeCount=int (optional; default: 0)
		The minimum size in edges a fragment must have to get reported
	--maximumEdgeCount=int (optional; default: 0 = all)
		The maximum size in edges a fragment can have to get reported

	--findPathsOnly=true|false (optional; default: false)
		Specifies that only simple paths should be found (and no trees or arbitrary graphs)
	--findTreesOnly=true|false (optional; default: false)
		Specifies that only (undirected) trees (graphs without cycles) should be found
	--singleRooted=true|false (optional; default: false)
		Specifies for directed graph that only single rooted ones should be found
	--connectedFragments=true|false (optional; default: true)
		Specifies that only connected fragments should be found

	--storeEmbeddings=true|false (optional; default: false)
		Specifies that for each fragment all embeddings should be stored
	--storeHierarchicalEmbeddings=true|false (optional; default: false)
		Specifies that for each fragment all embeddings should be stored as a hierarchical structur
	--embeddingBased=true|false (optional; default: false)
		Specifies that the frequency should be calculated embedding based or graph based

	--algorithm=gspan|gaston|dagma (optional; default: gspan)
		Specifies the mining algorithm to be used
	--closeGraph=true|false (optional; default: false)
		Activates fast closed mining as described for CloseGraph
	--subdue=true|false (optional; default: false)
		Specifies fragment filtering as used in SubDue
	--zaretsky=true|false (optional; default: false)
		Specifies fragment filtering to detect fragments as the algorithm of zaretsky

Parallel options:
	--distribution=local|threads|threads_np (optional; default: local)
		The scheme for distribution
		local     : no distribution, whole serach is done in master thread
		threads   : distribution by local threads
		threads_np: distribution by local threads without pooling temporal objects
	--threads=int (optional; default: 1)
		The number of working threads to be used

Debug options:
	--memoryStatistics=true|false (optional; default: false)
		Starts debug thread for memory measurement (takes much time)
	--visualize=true|false (optional; default: false)
		Renders the database graphs
	--naturalOrdered=none|edges|nodes|boths (optional; default: none)
		Decides if node-/edge labels are order naturally or because of its frequency
	--reverseOrdered=none|edges|nodes|boths (optional; default: none)
		Decides if node-/edge labels order shall be inverted
	Java option -D(quiet|info|[v][v]verbose)
		For no, informational, or verbose (debug) messages
```

## File formats

The library includes a set of file parsers strictly based on the `graphFile` option file extension.

 - `*.lg` is managed by `src/de/parsemis/parsers/LineGraphParser.java`, and parses the following format where every graph is directed and must have incremental nodes starting form 0:
```
t # graph_id
v node_id node_label
...
e node_id_from node_id_to edge_label
...
```

 - `*.sug` and `*.eug` are managed by `src/de/parsemis/parsers/SimpleUndirectedGraphParser.java`, respectively expecting string and integer labels. The file must start with a single line listing all the nodes' labels separated by a space, followed by a symmetric adjacency matrix with the label of the edge if it exists or `-` if not.

 - `*.graphml`, following the GraphML standard, is managed by `src/de/parsemis/parsers/GraphmlParser.java`
 
More details are available in `src/de/parsemis/miner/environment/Settings.java` and `src/de/parsemis/parsers/`.

## Documentation

The technical documentation is available, after the building of the library, in the `javadoc/` folder.

## License

Code released under the [LGPL v2.1 and EPL Licenses](https://github.com/timtadh/parsemis/blob/master/LICENCE.txt).
