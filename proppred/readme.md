# Property Prediction Library

This library allows for doing property prediction on RDF graphs using graph kernels and support vector machines.
Please have a look at Example.java for details on how to use it. 

Currently 5 different graph kernels are implemented that can be used with RDF data. 
Using these kernels support vector machines for classification, regression and outlier detection (one-class SVM) can be trained.
The support vector machine code that is used comes from the LibSVM project. RDF  can be imported using the d2s-tools convience classes, which make use of SESAME.


## Note
The library is a maven project and is dependent on the d2s-tools project that is also available from this repository.
