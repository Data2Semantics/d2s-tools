# Property Prediction Library

This library allows for doing property prediction on RDF graphs using graph kernels and support vector machines.
Please have a look at Example.java for details on how to use it. 


- `proppred.kernels` contains the basic kernel interface
  - `proppred.kernels.graphkernels` contains graph kernels that take lists of JUNG graphs as input
  - `proppred.kernels.rdfgraphkernels` contains graph kernels that take an RDFDataSet and a list of resources as input, i.e. these kernels are implemented directly on an RDF dataset.
- `proppred.learners` contains wrappers for two support vector machine libraries, LibSVM and LibLINEAR.


## Note
The library is a maven project and is dependent on the d2s-tools project that is also available from this repository.
