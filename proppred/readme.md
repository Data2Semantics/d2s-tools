# Graph Kernels for RDF Machine Learning Library

## Overview
This library allows for doing Machine Learning with RDF data using graph kernels. A number of graph kernels for RDF data are available. 
The most state-of-the-art of the these kernels are the RDFWLSubTreeKernel (for pure performance) and RDFIntersectionTreeEdgeVertexPathKernel (for speed).
All kernels can be used with the LibSVM  support vector machine library and some of the kernels are also suited for the LibLINEAR library. 
For both these libraries wrapper classes are provided.

Please have a look at Example.java, Example2.java and ExamplePair.java for details on how to use this library.
Example.java illustrates the use of the PropertyPredictor class. In Example2.java the use of the LibLINEAR library is shown. 
Finally, ExamplePair.java demonstrates a PairKernel to combine two graph kernels for link prediction.

This library was used in the paper that won the Open Science Award at ECML/PKDD 2013. See  http://www.data2semantics.org/publications/ecmlpkdd-2013/ for the winning website.

## Packages 
- `proppred.kernels` contains the basic kernel interface and some utility functions.
  - `proppred.kernels.graphkernels` contains graph kernels that take lists of JUNG graphs as input.
  - `proppred.kernels.rdfgraphkernels` contains graph kernels that take an RDFDataSet and a list of resources as input, i.e. these kernels are implemented directly on an RDF dataset. If possible, use these kernels for your learning problem, since they are specifically designed for RDF and usually faster.
- `proppred.learners` contains wrappers for two support vector machine libraries, LibSVM and LibLINEAR.
- `proppred.predictors`contains code for predictors, that try to hide the use of LibSVM/LibLINEAR libraries. This code is somewhat outdated, since it does not work with LibLINEAR. It remains to be decided whether a Predictor class is really necessary.


## Note
This library is a maven project. It is dependent on the d2s-tools project, which contains some utility functions for handling RDF datasets and graphs. The d2s-tools project is also available form this repository.


### Acknowledgments
This software was supported by the Dutch national program COMMIT.
